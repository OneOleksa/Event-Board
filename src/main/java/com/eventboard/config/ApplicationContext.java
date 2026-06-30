package com.eventboard.config;

import com.eventboard.repository.EventRepository;
import com.eventboard.repository.ParticipantRepository;
import com.eventboard.repository.jdbc.JdbcEventRepository;
import com.eventboard.repository.jdbc.JdbcParticipantRepository;
import com.eventboard.service.EventService;
import com.eventboard.service.EventServiceImpl;

/**
 * Simple manual dependency container for the application.
 */
public class ApplicationContext {

    private static final EventRepository EVENT_REPOSITORY =
            new JdbcEventRepository();

    private static final ParticipantRepository PARTICIPANT_REPOSITORY =
            new JdbcParticipantRepository();

    private static final EventService EVENT_SERVICE =
            new EventServiceImpl(EVENT_REPOSITORY, PARTICIPANT_REPOSITORY);

    private ApplicationContext() {
    }

    /**
     * Provides the shared event service instance used by servlets.
     *
     * @return configured event service
     */
    public static EventService getEventService() {
        return EVENT_SERVICE;
    }
}
