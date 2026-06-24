package com.eventboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegisterParticipantRequest {
    private Long eventId;
    private String studentName;
    private String studentEmail;
}