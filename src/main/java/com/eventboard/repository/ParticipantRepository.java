package com.eventboard.repository;


import com.eventboard.model.Participant;

import java.util.List;
import java.util.Map;

/**
 * Data access contract for participant registrations.
 */
public interface ParticipantRepository {

    /**
     * Counts participants registered for one event.
     *
     * @param eventId event id
     * @return registered participants count
     */
    int countByEventId(Long eventId);

    /**
     * Counts participants for several events in one repository call.
     *
     * @param eventIds event ids to count
     * @return map where key is event id and value is registered participants count
     */
    Map<Long, Integer> countByEventIds(List<Long> eventIds);

    /**
     * Finds participants registered for one event.
     *
     * @param eventId event id
     * @return participants ordered by registration id
     */
    List<Participant> findByEventId(Long eventId);

    /**
     * Saves a participant without checking seat availability.
     *
     * @param participant participant to save
     */
    void save(Participant participant);

    /**
     * Saves a participant only when the event still has free seats.
     *
     * @param participant participant to save
     * @param maxSeats maximum seats allowed for the event
     * @return true when participant was saved, false when the event is already full
     */
    boolean saveIfFreeSeats(Participant participant, int maxSeats);

    /**
     * Checks whether an email is already registered for an event.
     *
     * @param eventId event id
     * @param studentEmail participant email
     * @return true when this email already exists for the event
     */
    boolean existsByEventIdAndEmail(Long eventId, String studentEmail);
}
