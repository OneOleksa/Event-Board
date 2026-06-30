package com.eventboard.dto;

import com.eventboard.model.Event;
import com.eventboard.model.Participant;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EventDetailsDtoTest {

    @Test
    void constructor_whenParticipantsAreNull_usesEmptyList() {
        EventDetailsDto dto = new EventDetailsDto(event(2), null);

        assertEquals(0, dto.getRegisteredCount());
        assertEquals(2, dto.getAvailableSeats());
        assertEquals(List.of(), dto.getParticipants());
    }

    @Test
    void constructor_copiesParticipantsList() {
        Participant participant = participant(1L, "Ivan Sirko");
        List<Participant> participants = new ArrayList<>();
        participants.add(participant);

        EventDetailsDto dto = new EventDetailsDto(event(2), participants);
        participants.clear();

        assertEquals(1, dto.getRegisteredCount());
        assertEquals(List.of(participant), dto.getParticipants());
    }

    @Test
    void constructor_whenEventIsNull_throwsException() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new EventDetailsDto(null, List.of())
        );

        assertEquals("event cannot be null", exception.getMessage());
    }

    @Test
    void getAvailableSeats_whenEventIsFull_returnsZero() {
        EventDetailsDto dto = new EventDetailsDto(
                event(1),
                List.of(participant(1L, "Ivan Sirko"), participant(2L, "Olena Koval"))
        );

        assertEquals(2, dto.getRegisteredCount());
        assertEquals(0, dto.getAvailableSeats());
    }

    private Event event(int maxSeats) {
        Event event = new Event();
        event.setId(1L);
        event.setTitle("Java Workshop");
        event.setEventDate(LocalDate.now().plusDays(1));
        event.setMaxSeats(maxSeats);
        return event;
    }

    private Participant participant(Long id, String studentName) {
        Participant participant = new Participant();
        participant.setId(id);
        participant.setEventId(1L);
        participant.setStudentName(studentName);
        participant.setStudentEmail(studentName.toLowerCase().replace(" ", ".") + "@gmail.com");
        return participant;
    }
}
