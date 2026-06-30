package com.eventboard.repository;


import com.eventboard.model.Participant;

import java.util.List;

public interface ParticipantRepository {
    int countByEventId(Long eventId);
    List<Participant> findByEventId(Long eventId);
    void save(Participant participant);
}
