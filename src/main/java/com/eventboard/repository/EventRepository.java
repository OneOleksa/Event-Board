package com.eventboard.repository;
import com.eventboard.model.Event;

import java.util.List;
import java.util.Optional;

/**
 * Data access contract for event records.
 */
public interface EventRepository {

    /**
     * Finds events whose date is today or in the future.
     *
     * @return upcoming events ordered by date
     */
    List<Event> findUpcomingEvents();

    /**
     * Persists a new event.
     *
     * @param event event to save
     */
    void save(Event event);

    /**
     * Finds one event by its primary key.
     *
     * @param id event id
     * @return event when it exists
     */
    Optional<Event> findById(Long id);
}
