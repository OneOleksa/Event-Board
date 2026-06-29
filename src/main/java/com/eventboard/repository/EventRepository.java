package com.eventboard.repository;
import com.eventboard.model.Event;

import java.util.List;
import java.util.Optional;

public interface EventRepository {
    List<Event> findUpcomingEvents();
    void save(Event event);
    Optional<Event> findById(Long id);
}
