package com.eventboard.dto;

import com.eventboard.model.Event;
import com.eventboard.model.Participant;
import lombok.Getter;


import java.util.List;
import java.util.Objects;

@Getter
public class EventDetailsDto {
    private final Event event;
    private final List<Participant> participants;

    public EventDetailsDto(Event event, List<Participant> participants) {
        this.event = Objects.requireNonNull(event, "event cannot be null");
        this.participants = participants == null ? List.of() : List.copyOf(participants);
    }
    public int getRegisteredCount(){
        return participants.size();
    }
    public int getAvailableSeats(){
        return Math.max(0, event.getMaxSeats() - getRegisteredCount());
    }
}

