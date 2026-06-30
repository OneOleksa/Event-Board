package com.eventboard.service;

import com.eventboard.dto.CreateEventRequest;
import com.eventboard.dto.EventDetailsDto;
import com.eventboard.dto.EventListItemDto;
import com.eventboard.dto.RegisterParticipantRequest;
import com.eventboard.exception.EventNotFoundException;
import com.eventboard.exception.ValidationException;
import com.eventboard.model.Participant;
import com.eventboard.repository.EventRepository;
import com.eventboard.repository.ParticipantRepository;
import com.eventboard.model.Event;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Default implementation of {@link EventService}.
 * Handles validation rules and delegates persistence to repositories.
 */
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final ParticipantRepository participantRepository;
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile(
                    "^[A-Za-z0-9._%+-]+@(stud\\.duikt\\.edu\\.ua|gmail\\.com|hotmail\\.com|outlook\\.com)$",
                    Pattern.CASE_INSENSITIVE
            );

    public EventServiceImpl(EventRepository eventRepository,
                            ParticipantRepository participantRepository) {
        this.eventRepository = Objects.requireNonNull(eventRepository, "EventRepository cannot be null");
        this.participantRepository = Objects.requireNonNull(participantRepository, "ParticipantRepository cannot be null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<EventListItemDto> getUpcomingEvents() {
        List<Event> events = eventRepository.findUpcomingEvents();

        if (events.isEmpty()) {
            return List.of();
        }

        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .toList();

        Map<Long, Integer> registeredCounts = participantRepository.countByEventIds(eventIds);

        return events.stream()
                .map(event -> {
                    int registeredCount = registeredCounts.getOrDefault(event.getId(), 0);

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

    /**
     * {@inheritDoc}
     */
    @Override
    public void createEvent(CreateEventRequest request) {
        Objects.requireNonNull(request, "CreateEventRequest cannot be null");
        validateCreateEventRequest(request);
        Event event = new Event();
        event.setTitle(request.getTitle().trim());
        event.setEventDate(request.getEventDate());
        event.setMaxSeats(request.getMaxSeats());
        eventRepository.save(event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EventDetailsDto getEventDetails(Long eventId) {
        Objects.requireNonNull(eventId, "Event id cannot be null");

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Захід не знайдено"));

        List<Participant> participants = participantRepository.findByEventId(event.getId());
        return new EventDetailsDto(event, participants);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerParticipant(RegisterParticipantRequest request) {
        Objects.requireNonNull(request, "RegisterParticipantRequest cannot be null");

        validateRegisterParticipantRequest(request);

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new EventNotFoundException("Захід не знайдено"));

        boolean alreadyRegistered = participantRepository.existsByEventIdAndEmail(
                event.getId(),
                request.getStudentEmail().trim()
        );

        if (alreadyRegistered) {
            throw new ValidationException("Студент з таким email уже зареєстрований на цей захід");
        }

        Participant participant = new Participant();
        participant.setEventId(event.getId());
        participant.setStudentName(request.getStudentName().trim());
        participant.setStudentEmail(request.getStudentEmail().trim());

        boolean saved = participantRepository.saveIfFreeSeats(participant, event.getMaxSeats());

        if (!saved) {
            throw new ValidationException("На цей захід більше немає вільних місць");
        }
    }

    private void validateCreateEventRequest(CreateEventRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new ValidationException("Назва заходу не може бути порожньою");
        }
        if (request.getEventDate() == null) {
            throw new ValidationException("Дата заходу є обов'язковою");
        }
        if (request.getEventDate().isBefore(LocalDate.now())) {
            throw new ValidationException("Дата заходу не може бути в минулому");
        }
        if (request.getEventDate().isAfter(LocalDate.now().plusYears(1))) {
            throw new ValidationException("Дата заходу не може бути більше ніж через 1 рік");
        }
        if (request.getMaxSeats() <= 0) {
            throw new ValidationException("Кількість місць має бути більшою за нуль");
        }
    }
    private void validateRegisterParticipantRequest(RegisterParticipantRequest request) {
        if (request.getEventId() == null) {
            throw new ValidationException("ID заходу є обов'язковим");
        }
        if (request.getStudentName() == null || request.getStudentName().isBlank()) {
            throw new ValidationException("Ім'я студента не може бути порожнім");
        }
        if (request.getStudentEmail() == null || request.getStudentEmail().isBlank()) {
            throw new ValidationException("Email студента є обов'язковим");
        }
        if (!EMAIL_PATTERN.matcher(request.getStudentEmail().trim()).matches()) {
            throw new ValidationException("Email має бути у дозволеному форматі: stud.duikt.edu.ua, gmail.com, hotmail.com або outlook.com");
        }
    }
}
