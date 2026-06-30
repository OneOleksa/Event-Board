package com.eventboard.exception;

/**
 * Signals that the requested event does not exist.
 */
public class EventNotFoundException extends RuntimeException {

    /**
     * Creates an exception with a user-facing not found message.
     *
     * @param message error message
     */
    public EventNotFoundException(String message) {
        super(message);
    }
}
