package com.eventboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class CreateEventRequest {
    private String title;
    private LocalDate eventDate;
    private int maxSeats;
}