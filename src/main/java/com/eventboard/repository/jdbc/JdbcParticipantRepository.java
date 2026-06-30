package com.eventboard.repository.jdbc;

import com.eventboard.config.DatabaseConnectionFactory;
import com.eventboard.model.Participant;
import com.eventboard.repository.ParticipantRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JdbcParticipantRepository implements ParticipantRepository {
    private static final String COUNT_BY_EVENT_ID_SQL = """
        SELECT COUNT(*) AS participant_count
        FROM participants
        WHERE event_id = ?
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

    @Override
    public int countByEventId(Long eventId) {
        Objects.requireNonNull(eventId, "Event id cannot be null");
        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(COUNT_BY_EVENT_ID_SQL)) {

            preparedStatement.setLong(1, eventId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("participant_count");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Cannot count participants by event id", e);
        }
        return 0;
    }

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

    @Override
    public void save(Participant participant) {
        Objects.requireNonNull(participant, "Participant cannot be null");
        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SAVE_PARTICIPANT_SQL)) {
            preparedStatement.setLong(1, participant.getEventId());
            preparedStatement.setString(2, participant.getStudentName());
            preparedStatement.setString(3, participant.getStudentEmail());
            preparedStatement.executeUpdate();
        }catch (SQLException e) {
            throw new RuntimeException("Cannot save participant", e);
        }
    }

    private Participant mapRowToParticipant(ResultSet resultSet) throws SQLException {
        Participant participant = new Participant();
        participant.setId(resultSet.getLong("id"));
        participant.setEventId(resultSet.getLong("event_id"));
        participant.setStudentName(resultSet.getString("student_name"));
        participant.setStudentEmail(resultSet.getString("student_email"));
        return participant;
    }
}
