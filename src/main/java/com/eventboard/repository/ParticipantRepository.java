package com.eventboard.repository;


public interface ParticipantRepository {
    int countByEventId(Long eventId);
}
