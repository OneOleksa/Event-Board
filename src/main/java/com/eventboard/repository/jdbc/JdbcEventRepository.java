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
import java.util.Objects;
import java.util.Optional;


public class JdbcEventRepository implements EventRepository {
    private static final String FIND_UPCOMING_EVENTS_SQL = """
        SELECT id, title, event_date, max_seats
        FROM events
        WHERE event_date >= CURRENT_DATE
        ORDER BY event_date
        """;

    private static final String SAVE_EVENT_SQL = """
        INSERT INTO events (title, event_date, max_seats)
        VALUES (?, ?, ?)
        """;

    private static final String FIND_BY_ID_SQL = """
        SELECT id, title, event_date, max_seats
        FROM events
        WHERE id = ?
        """;

    @Override
    public List<Event> findUpcomingEvents() {

        List<Event> events = new ArrayList<>();
        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(FIND_UPCOMING_EVENTS_SQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                events.add(mapRowToEvent(resultSet));
            }
        } catch (SQLException e){
            throw new RuntimeException("Cannot find upcoming events", e);
        }
       return events;
    }

    @Override
    public void save(Event event) {
        Objects.requireNonNull(event, "Event cannot be null");

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SAVE_EVENT_SQL)) {
            preparedStatement.setString(1, event.getTitle());
            preparedStatement.setDate(2, java.sql.Date.valueOf(event.getEventDate()));
            preparedStatement.setInt(3, event.getMaxSeats());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Cannot save event", e);
        }
    }

    @Override
    public Optional<Event> findById(Long id) {
        Objects.requireNonNull(id, "Event id cannot be null");

        try (Connection connection = DatabaseConnectionFactory.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL)) {

            preparedStatement.setLong(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRowToEvent(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Cannot find event by id", e);
        }

        return Optional.empty();
    }
    private Event mapRowToEvent(ResultSet resultSet) throws SQLException {
        Event event = new Event();

        event.setId(resultSet.getLong("id"));
        event.setTitle(resultSet.getString("title"));
        event.setEventDate(resultSet.getDate("event_date").toLocalDate());
        event.setMaxSeats(resultSet.getInt("max_seats"));

        return event;
    }
}
