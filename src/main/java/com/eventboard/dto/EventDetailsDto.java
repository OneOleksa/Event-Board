package com.eventboard.dto;

import com.eventboard.model.Event;
import com.eventboard.model.Participant;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

/**
 * Read model for the event details page.
 */
@Getter
public class EventDetailsDto {
    private final Event event;
    private final List<Participant> participants;

    /**
     * Creates details for one event and safely copies the participants list.
     *
     * @param event event to display
     * @param participants participants registered for the event
     */
    public EventDetailsDto(Event event, List<Participant> participants) {
        this.event = Objects.requireNonNull(event, "event cannot be null");
        this.participants = participants == null ? List.of() : List.copyOf(participants);
    }

    /**
     * Counts registered participants shown on the details page.
     *
     * @return number of registered participants
     */
    public int getRegisteredCount() {
        return participants.size();
    }

    /**
     * Calculates available seats for the details page.
     *
     * @return available seats, never less than zero
     */
    public int getAvailableSeats() {
        return Math.max(0, event.getMaxSeats() - getRegisteredCount());
    }
}

