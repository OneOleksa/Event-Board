package com.eventboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Carries form data required to register a participant for an event.
 */
@Getter
@AllArgsConstructor
public class RegisterParticipantRequest {
    private Long eventId;
    private String studentName;
    private String studentEmail;
}
