package com.eventboard.service;

import com.eventboard.dto.EventListItemDto;
import com.eventboard.dto.CreateEventRequest;

import java.util.List;

public interface EventService {
    List<EventListItemDto> getUpcomingEvents();
    void createEvent(CreateEventRequest request);
}
