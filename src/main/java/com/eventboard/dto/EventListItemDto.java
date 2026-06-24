package com.eventboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;


@AllArgsConstructor
@Getter
public class EventListItemDto {
    private Long id;
    private String title;
    private LocalDate eventDate;
    private int maxSeats;
    private int registeredCount;

    public int getAvailableSeats() {
        return Math.max(0, maxSeats - registeredCount);
    }
}
