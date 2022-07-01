package com.amusnet.exception;

/**
 * Indicates that an element/value is missing in some part of the Multipliers table.
 */
public class MissingTableElementException extends Exception {
    @SuppressWarnings("unused")
    public MissingTableElementException() {
        super();
    }

    @SuppressWarnings("unused")
    public MissingTableElementException(String message) {
        super(message);
    }

    @SuppressWarnings("unused")
    public MissingTableElementException(String message, Throwable cause) {
        super(message, cause);
    }

    @SuppressWarnings("unused")
    public MissingTableElementException(Throwable cause) {
        super(cause);
    }
}
