package com.eventboard.service;

import com.eventboard.dto.EventDetailsDto;
import com.eventboard.dto.EventListItemDto;
import com.eventboard.dto.CreateEventRequest;
import com.eventboard.dto.RegisterParticipantRequest;

import java.util.List;

/**
 * Business service for event listing, event creation, and participant registration.
 */
public interface EventService {

    /**
     * Loads upcoming events with calculated registration counters.
     *
     * @return upcoming events for the main page
     */
    List<EventListItemDto> getUpcomingEvents();

    /**
     * Validates and creates a new event.
     *
     * @param request event creation request
     */
    void createEvent(CreateEventRequest request);

    /**
     * Loads one event with its registered participants.
     *
     * @param eventId event id
     * @return event details for the details page
     */
    EventDetailsDto getEventDetails(Long eventId);

    /**
     * Validates and registers a participant for an event.
     *
     * @param request participant registration request
     */
    void registerParticipant(RegisterParticipantRequest request);
}
