package com.eventboard.repository.jdbc;

import com.eventboard.config.DatabaseConnectionFactory;
import com.eventboard.repository.ParticipantRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class JdbcParticipantRepository implements ParticipantRepository {
    private static final String COUNT_BY_EVENT_ID_SQL = """
        SELECT COUNT(*) AS participant_count
        FROM participants
        WHERE event_id = ?
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
}
