package com.eventboard.repository;
import com.eventboard.model.Event;

import java.util.List;

public interface EventRepository {
    List<Event> findUpcomingEvents();
    void save(Event event);
}
