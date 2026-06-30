package com.eventboard.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventListItemDtoTest {

    @Test
    void getAvailableSeats_whenThereAreFreeSeats_returnsDifference() {
        EventListItemDto dto = new EventListItemDto(
                1L,
                "Java Workshop",
                LocalDate.now().plusDays(1),
                10,
                3
        );

        assertEquals(7, dto.getAvailableSeats());
    }

    @Test
    void getAvailableSeats_whenRegisteredCountIsGreaterThanMaxSeats_returnsZero() {
        EventListItemDto dto = new EventListItemDto(
                1L,
                "Java Workshop",
                LocalDate.now().plusDays(1),
                10,
                12
        );

        assertEquals(0, dto.getAvailableSeats());
    }
}
