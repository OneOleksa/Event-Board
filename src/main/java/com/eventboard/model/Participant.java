package com.eventboard.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.Getter;
import lombok.Builder;

/**
 * Represents a student registration for a specific event.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Participant {
    private String studentName;
    private String studentEmail;
    private Long id;
    private Long eventId;
}
