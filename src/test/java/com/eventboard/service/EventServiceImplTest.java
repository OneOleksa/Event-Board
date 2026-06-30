package com.eventboard.service;

import com.eventboard.dto.CreateEventRequest;
import com.eventboard.dto.EventDetailsDto;
import com.eventboard.dto.EventListItemDto;
import com.eventboard.dto.RegisterParticipantRequest;
import com.eventboard.exception.EventNotFoundException;
import com.eventboard.exception.ValidationException;
import com.eventboard.model.Event;
import com.eventboard.model.Participant;
import com.eventboard.repository.EventRepository;
import com.eventboard.repository.ParticipantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ParticipantRepository participantRepository;

    private EventServiceImpl eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventServiceImpl(eventRepository, participantRepository);
    }

    @Test
    void constructor_whenEventRepositoryIsNull_throwsException() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new EventServiceImpl(null, participantRepository)
        );

        assertEquals("EventRepository cannot be null", exception.getMessage());
    }

    @Test
    void constructor_whenParticipantRepositoryIsNull_throwsException() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new EventServiceImpl(eventRepository, null)
        );

        assertEquals("ParticipantRepository cannot be null", exception.getMessage());
    }

    @Test
    void getUpcomingEvents_usesBulkParticipantCount() {
        Event javaWorkshop = event(1L, "Java Workshop", 10);
        Event jdbcLecture = event(2L, "JDBC Lecture", 5);

        when(eventRepository.findUpcomingEvents()).thenReturn(List.of(javaWorkshop, jdbcLecture));
        when(participantRepository.countByEventIds(List.of(1L, 2L))).thenReturn(Map.of(1L, 3));

        List<EventListItemDto> result = eventService.getUpcomingEvents();

        assertEquals(2, result.size());
        assertEquals(3, result.get(0).getRegisteredCount());
        assertEquals(7, result.get(0).getAvailableSeats());
        assertEquals(0, result.get(1).getRegisteredCount());
        assertEquals(5, result.get(1).getAvailableSeats());
        verify(participantRepository, never()).countByEventId(anyLong());
    }

    @Test
    void getUpcomingEvents_whenNoEvents_doesNotQueryParticipants() {
        when(eventRepository.findUpcomingEvents()).thenReturn(List.of());

        List<EventListItemDto> result = eventService.getUpcomingEvents();

        assertTrue(result.isEmpty());
        verifyNoInteractions(participantRepository);
    }

    @Test
    void createEvent_trimsTitleBeforeSaving() {
        CreateEventRequest request = new CreateEventRequest(
                "  Java Workshop  ",
                LocalDate.now().plusDays(1),
                20
        );

        eventService.createEvent(request);

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(eventCaptor.capture());
        assertEquals("Java Workshop", eventCaptor.getValue().getTitle());
    }

    @Test
    void createEvent_whenTitleIsBlank_throwsValidationException() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> eventService.createEvent(new CreateEventRequest(
                        "   ",
                        LocalDate.now().plusDays(1),
                        20
                ))
        );

        assertEquals("Назва заходу не може бути порожньою", exception.getMessage());
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void createEvent_whenDateIsMissing_throwsValidationException() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> eventService.createEvent(new CreateEventRequest(
                        "Java Workshop",
                        null,
                        20
                ))
        );

        assertEquals("Дата заходу є обов'язковою", exception.getMessage());
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void createEvent_whenDateIsInPast_throwsValidationException() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> eventService.createEvent(new CreateEventRequest(
                        "Java Workshop",
                        LocalDate.now().minusDays(1),
                        20
                ))
        );

        assertEquals("Дата заходу не може бути в минулому", exception.getMessage());
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void createEvent_whenDateIsTooFar_throwsValidationException() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> eventService.createEvent(new CreateEventRequest(
                        "Java Workshop",
                        LocalDate.now().plusYears(1).plusDays(1),
                        20
                ))
        );

        assertEquals("Дата заходу не може бути більше ніж через 1 рік", exception.getMessage());
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void createEvent_whenMaxSeatsIsNotPositive_throwsValidationException() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> eventService.createEvent(new CreateEventRequest(
                        "Java Workshop",
                        LocalDate.now().plusDays(1),
                        0
                ))
        );

        assertEquals("Кількість місць має бути більшою за нуль", exception.getMessage());
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void getEventDetails_whenEventExists_returnsEventWithParticipants() {
        Event event = event(1L, "Java Workshop", 3);
        Participant participant = participant(10L, 1L, "Ivan Sirko", "ivan@gmail.com");

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(participantRepository.findByEventId(1L)).thenReturn(List.of(participant));

        EventDetailsDto result = eventService.getEventDetails(1L);

        assertEquals(event, result.getEvent());
        assertEquals(1, result.getRegisteredCount());
        assertEquals(2, result.getAvailableSeats());
        assertEquals(List.of(participant), result.getParticipants());
    }

    @Test
    void getEventDetails_whenEventDoesNotExist_throwsEventNotFoundException() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        EventNotFoundException exception = assertThrows(
                EventNotFoundException.class,
                () -> eventService.getEventDetails(99L)
        );

        assertEquals("Захід не знайдено", exception.getMessage());
        verify(participantRepository, never()).findByEventId(anyLong());
    }

    @Test
    void registerParticipant_whenSeatsAreAvailable_savesTrimmedParticipantAtomically() {
        Event event = event(1L, "Java Workshop", 2);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(participantRepository.existsByEventIdAndEmail(1L, "student@gmail.com")).thenReturn(false);
        when(participantRepository.saveIfFreeSeats(any(Participant.class), eq(2))).thenReturn(true);

        eventService.registerParticipant(new RegisterParticipantRequest(
                1L,
                "  Ivan Sirko  ",
                "  student@gmail.com  "
        ));

        ArgumentCaptor<Participant> participantCaptor = ArgumentCaptor.forClass(Participant.class);
        verify(participantRepository).saveIfFreeSeats(participantCaptor.capture(), eq(2));

        Participant participant = participantCaptor.getValue();
        assertEquals(1L, participant.getEventId());
        assertEquals("Ivan Sirko", participant.getStudentName());
        assertEquals("student@gmail.com", participant.getStudentEmail());
        verify(participantRepository, never()).countByEventId(anyLong());
        verify(participantRepository, never()).save(any(Participant.class));
    }

    @Test
    void registerParticipant_whenNoFreeSeats_throwsValidationExceptionAndDoesNotSaveDirectly() {
        Event event = event(1L, "Java Workshop", 2);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(participantRepository.existsByEventIdAndEmail(1L, "student@gmail.com")).thenReturn(false);
        when(participantRepository.saveIfFreeSeats(any(Participant.class), eq(2))).thenReturn(false);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> eventService.registerParticipant(new RegisterParticipantRequest(
                        1L,
                        "Ivan Sirko",
                        "student@gmail.com"
                ))
        );

        assertEquals("На цей захід більше немає вільних місць", exception.getMessage());
        verify(participantRepository, never()).countByEventId(anyLong());
        verify(participantRepository, never()).save(any(Participant.class));
    }

    @Test
    void registerParticipant_whenEmailAlreadyRegistered_doesNotTryToSave() {
        Event event = event(1L, "Java Workshop", 2);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(participantRepository.existsByEventIdAndEmail(1L, "student@gmail.com")).thenReturn(true);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> eventService.registerParticipant(new RegisterParticipantRequest(
                        1L,
                        "Ivan Sirko",
                        "student@gmail.com"
                ))
        );

        assertEquals("Студент з таким email уже зареєстрований на цей захід", exception.getMessage());
        verify(participantRepository, never()).saveIfFreeSeats(any(Participant.class), eq(2));
        verify(participantRepository, never()).save(any(Participant.class));
    }

    @Test
    void registerParticipant_whenEventIdIsMissing_throwsValidationException() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> eventService.registerParticipant(new RegisterParticipantRequest(
                        null,
                        "Ivan Sirko",
                        "student@gmail.com"
                ))
        );

        assertEquals("ID заходу є обов'язковим", exception.getMessage());
        verifyNoInteractions(eventRepository, participantRepository);
    }

    @Test
    void registerParticipant_whenNameIsBlank_throwsValidationException() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> eventService.registerParticipant(new RegisterParticipantRequest(
                        1L,
                        "   ",
                        "student@gmail.com"
                ))
        );

        assertEquals("Ім'я студента не може бути порожнім", exception.getMessage());
        verifyNoInteractions(eventRepository, participantRepository);
    }

    @Test
    void registerParticipant_whenEmailDomainIsNotAllowed_throwsValidationException() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> eventService.registerParticipant(new RegisterParticipantRequest(
                        1L,
                        "Ivan Sirko",
                        "student@example.com"
                ))
        );

        assertEquals(
                "Email має бути у дозволеному форматі: stud.duikt.edu.ua, gmail.com, hotmail.com або outlook.com",
                exception.getMessage()
        );
        verifyNoInteractions(eventRepository, participantRepository);
    }

    @Test
    void registerParticipant_whenEventDoesNotExist_throwsEventNotFoundException() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        EventNotFoundException exception = assertThrows(
                EventNotFoundException.class,
                () -> eventService.registerParticipant(new RegisterParticipantRequest(
                        99L,
                        "Ivan Sirko",
                        "student@gmail.com"
                ))
        );

        assertEquals("Захід не знайдено", exception.getMessage());
        verify(participantRepository, never()).existsByEventIdAndEmail(anyLong(), any());
        verify(participantRepository, never()).saveIfFreeSeats(any(Participant.class), anyInt());
    }

    private Event event(Long id, String title, int maxSeats) {
        Event event = new Event();
        event.setId(id);
        event.setTitle(title);
        event.setEventDate(LocalDate.now().plusDays(1));
        event.setMaxSeats(maxSeats);
        return event;
    }

    private Participant participant(Long id, Long eventId, String studentName, String studentEmail) {
        Participant participant = new Participant();
        participant.setId(id);
        participant.setEventId(eventId);
        participant.setStudentName(studentName);
        participant.setStudentEmail(studentEmail);
        return participant;
    }
}
