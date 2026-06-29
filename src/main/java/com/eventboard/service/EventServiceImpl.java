package com.eventboard.service;

import com.eventboard.dto.CreateEventRequest;
import com.eventboard.dto.EventDetailsDto;
import com.eventboard.dto.EventListItemDto;
import com.eventboard.exception.EventNotFoundException;
import com.eventboard.exception.ValidationException;
import com.eventboard.model.Participant;
import com.eventboard.repository.EventRepository;
import com.eventboard.repository.ParticipantRepository;
import com.eventboard.model.Event;

import java.time.LocalDate;
import java.util.List;
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
        return events.stream()
                .map(event -> {
                    int registeredCount = participantRepository.countByEventId(event.getId());

                    return new EventListItemDto(
                            event.getId(),
                            event.getTitle(),
                            event.getEventDate(),
                            event.getMaxSeats(),
                            registeredCount
                    );
                })
                .toList();
    }

    @Override
    public void createEvent(CreateEventRequest request) {
        Objects.requireNonNull(request, "CreateEventRequest cannot be null");
        validateCreateEventRequest(request);
        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setEventDate(request.getEventDate());
        event.setMaxSeats(request.getMaxSeats());
        eventRepository.save(event);
    }

    @Override
    public EventDetailsDto getEventDetails(Long eventId) {
        Objects.requireNonNull(eventId, "Event id cannot be null");

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Захід не знайдено"));

        List<Participant> participants = participantRepository.findByEventId(event.getId());
        return new EventDetailsDto(event, participants);
    }

    private void validateCreateEventRequest(CreateEventRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new ValidationException("Назва заходу не може бути порожньою");
        }
        if (request.getEventDate() == null) {
            throw new ValidationException("Дата заходу є обов'язковою");
        }
        if (request.getEventDate().isBefore(LocalDate.now())) {
            throw new ValidationException("Event date is before current date");
        }
        if (request.getMaxSeats() <= 0) {
            throw new ValidationException("Кількість місць має бути більшою за нуль");
        }
    }
}