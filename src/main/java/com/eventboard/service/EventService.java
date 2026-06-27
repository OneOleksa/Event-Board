package com.eventboard.service;

import com.eventboard.dto.EventListItemDto;

import java.util.List;

public interface EventService {
    List<EventListItemDto> getUpcomingEvents();
}
