package com.eventboard.exception;

/**
 * Signals that user input violates application validation rules.
 */
public class ValidationException extends RuntimeException {

    /**
     * Creates a validation exception with a user-facing message.
     *
     * @param message validation error message
     */
    public ValidationException(String message) {
        super(message);
    }
}
