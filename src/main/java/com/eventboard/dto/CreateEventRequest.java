package com.eventboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

/**
 * Carries form data required to create a new event.
 */
@Getter
@AllArgsConstructor
public class CreateEventRequest {
    private String title;
    private LocalDate eventDate;
    private int maxSeats;
}
