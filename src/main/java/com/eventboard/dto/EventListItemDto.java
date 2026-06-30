package com.eventboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;


/**
 * Read model for the events list page.
 */
@AllArgsConstructor
@Getter
public class EventListItemDto {
    private Long id;
    private String title;
    private LocalDate eventDate;
    private int maxSeats;
    private int registeredCount;

    /**
     * Calculates how many seats are still available for this event.
     *
     * @return available seats, never less than zero
     */
    public int getAvailableSeats() {
        return Math.max(0, maxSeats - registeredCount);
    }
}
