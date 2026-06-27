package com.eventboard.repository.jdbc;

import com.eventboard.config.DatabaseConnectionFactory;
import com.eventboard.model.Event;
import com.eventboard.repository.EventRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class JdbcEventRepository implements EventRepository {
    private static final String FIND_UPCOMING_EVENTS_SQL = """
        SELECT id, title, event_date, max_seats
        FROM events
        WHERE event_date >= CURRENT_DATE
        ORDER BY event_date
        """;
    @Override
    public List<Event> findUpcomingEvents() {
       List<Event> events = new ArrayList<>();
        try (Connection connection = DatabaseConnectionFactory.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(FIND_UPCOMING_EVENTS_SQL);
        ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                Event event = new Event();
                event.setId(resultSet.getLong("id"));
                event.setTitle(resultSet.getString("title"));
                event.setEventDate(resultSet.getDate("event_date").toLocalDate());
                event.setMaxSeats(resultSet.getInt("max_seats"));
                events.add(event);
            }
        } catch (SQLException e){
            throw new RuntimeException("Cannot find upcoming events", e);
        }
       return events;
    }
}
