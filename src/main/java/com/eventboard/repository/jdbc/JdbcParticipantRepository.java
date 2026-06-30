package com.eventboard.repository.jdbc;

import com.eventboard.config.DatabaseConnectionFactory;
import com.eventboard.model.Participant;
import com.eventboard.repository.ParticipantRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * JDBC implementation of {@link ParticipantRepository}.
 */
public class JdbcParticipantRepository implements ParticipantRepository {
    private static final String COUNT_BY_EVENT_ID_SQL = """
        SELECT COUNT(*) AS participant_count
        FROM participants
        WHERE event_id = ?
        """;

    private static final String COUNT_BY_EVENT_IDS_SQL = """
        SELECT event_id, COUNT(*) AS participant_count
        FROM participants
        WHERE event_id IN (%s)
        GROUP BY event_id
        """;

    private static final String FIND_BY_EVENT_ID_SQL = """
        SELECT id, event_id, student_name, student_email
        FROM participants
        WHERE event_id = ?
        ORDER BY id
        """;

    private static final String SAVE_PARTICIPANT_SQL = """
        INSERT INTO participants (event_id, student_name, student_email)
        VALUES (?, ?, ?)
        """;

    private static final String LOCK_EVENT_SQL = """
        SELECT id
        FROM events
        WHERE id = ?
        FOR UPDATE
        """;

    private static final String EXISTS_BY_EVENT_ID_AND_EMAIL_SQL = """
        SELECT EXISTS (
            SELECT 1
            FROM participants
            WHERE event_id = ?
              AND LOWER(student_email) = LOWER(?)
        )
        """;

    /**
     * {@inheritDoc}
     */
    @Override
    public int countByEventId(Long eventId) {
        Objects.requireNonNull(eventId, "Event id cannot be null");
        try (Connection connection = DatabaseConnectionFactory.getConnection()) {
            return countByEventId(connection, eventId);
        } catch (SQLException e) {
            throw new RuntimeException("Cannot count participants by event id", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Long, Integer> countByEventIds(List<Long> eventIds) {
        Objects.requireNonNull(eventIds, "Event ids cannot be null");

        if (eventIds.isEmpty()) {
            return Map.of();
        }

        List<Long> safeEventIds = eventIds.stream()
                .map(eventId -> Objects.requireNonNull(eventId, "Event id cannot be null"))
                .toList();

        String placeholders = safeEventIds.stream()
                .map(eventId -> "?")
                .collect(Collectors.joining(", "));
        String sql = COUNT_BY_EVENT_IDS_SQL.formatted(placeholders);
        Map<Long, Integer> counts = new HashMap<>();

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            bindEventIds(preparedStatement, safeEventIds);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    counts.put(
                            resultSet.getLong("event_id"),
                            resultSet.getInt("participant_count")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Cannot count participants by event ids", e);
        }

        return counts;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Participant> findByEventId(Long eventId) {
        Objects.requireNonNull(eventId, "Event id cannot be null");

        List<Participant> participants = new ArrayList<>();

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_EVENT_ID_SQL)) {
            preparedStatement.setLong(1, eventId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    participants.add(mapRowToParticipant(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Cannot find participants by event id", e);
        }
        return participants;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save(Participant participant) {
        Objects.requireNonNull(participant, "Participant cannot be null");
        try (Connection connection = DatabaseConnectionFactory.getConnection()) {
            insertParticipant(connection, participant);
        } catch (SQLException e) {
            throw new RuntimeException("Cannot save participant", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean saveIfFreeSeats(Participant participant, int maxSeats) {
        Objects.requireNonNull(participant, "Participant cannot be null");
        Objects.requireNonNull(participant.getEventId(), "Event id cannot be null");

        if (maxSeats <= 0) {
            throw new IllegalArgumentException("Max seats must be greater than zero");
        }

        try (Connection connection = DatabaseConnectionFactory.getConnection()) {
            connection.setAutoCommit(false);

            try {
                lockEvent(connection, participant.getEventId());

                int registeredCount = countByEventId(connection, participant.getEventId());

                if (registeredCount >= maxSeats) {
                    connection.rollback();
                    return false;
                }

                insertParticipant(connection, participant);
                connection.commit();
                return true;
            } catch (SQLException e) {
                rollback(connection);
                throw new RuntimeException("Cannot save participant if seats are free", e);
            } finally {
                restoreAutoCommit(connection);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Cannot save participant if seats are free", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsByEventIdAndEmail(Long eventId, String studentEmail) {
        Objects.requireNonNull(eventId, "Event id cannot be null");
        Objects.requireNonNull(studentEmail, "Student email cannot be null");

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(EXISTS_BY_EVENT_ID_AND_EMAIL_SQL)) {
            preparedStatement.setLong(1, eventId);
            preparedStatement.setString(2, studentEmail.trim());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBoolean(1);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Cannot check participant email", e);
        }
        return false;
    }

    private Participant mapRowToParticipant(ResultSet resultSet) throws SQLException {
        Participant participant = new Participant();
        participant.setId(resultSet.getLong("id"));
        participant.setEventId(resultSet.getLong("event_id"));
        participant.setStudentName(resultSet.getString("student_name"));
        participant.setStudentEmail(resultSet.getString("student_email"));
        return participant;
    }

    private int countByEventId(Connection connection, Long eventId) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(COUNT_BY_EVENT_ID_SQL)) {
            preparedStatement.setLong(1, eventId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("participant_count");
                }
            }
        }

        return 0;
    }

    private void insertParticipant(Connection connection, Participant participant) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(SAVE_PARTICIPANT_SQL)) {
            preparedStatement.setLong(1, participant.getEventId());
            preparedStatement.setString(2, participant.getStudentName());
            preparedStatement.setString(3, participant.getStudentEmail());
            preparedStatement.executeUpdate();
        }
    }

    private void bindEventIds(PreparedStatement preparedStatement, List<Long> eventIds) throws SQLException {
        for (int i = 0; i < eventIds.size(); i++) {
            preparedStatement.setLong(i + 1, eventIds.get(i));
        }
    }

    private void lockEvent(Connection connection, Long eventId) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(LOCK_EVENT_SQL)) {
            preparedStatement.setLong(1, eventId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("Event not found: " + eventId);
                }
            }
        }
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
        }
    }

    private void restoreAutoCommit(Connection connection) {
        try {
            connection.setAutoCommit(true);
        } catch (SQLException ignored) {
        }
    }
}
