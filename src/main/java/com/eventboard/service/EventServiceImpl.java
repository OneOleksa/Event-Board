package com.eventboard.service;

import com.eventboard.dto.EventListItemDto;
import com.eventboard.repository.EventRepository;
import com.eventboard.repository.ParticipantRepository;
import com.eventboard.model.Event;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final ParticipantRepository participantRepository;

    public EventServiceImpl(EventRepository eventRepository,
                            ParticipantRepository participantRepository) {
        this.eventRepository = Objects.requireNonNull(eventRepository, "EventRepository cannot be null");
        this.participantRepository = Objects.requireNonNull(participantRepository, "ParticipantRepository cannot be null");
    }

    @Override
    public List<EventListItemDto> getUpcomingEvents() {
        List<Event> events = eventRepository.findUpcomingEvents();

        List<EventListItemDto> result = new ArrayList<>();

        for (Event event : events) {
            int registeredCount = participantRepository.countByEventId(event.getId());
            EventListItemDto dto = new EventListItemDto(
                    event.getId(),
                    event.getTitle(),
                    event.getEventDate(),
                    event.getMaxSeats(),
                    registeredCount
            );
            result.add(dto);
        }
        return result;
    }
}