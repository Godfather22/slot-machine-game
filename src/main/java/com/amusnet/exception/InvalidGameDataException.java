package com.amusnet.exception;

public class InvalidGameDataException extends Exception {
    @SuppressWarnings("unused")
    public InvalidGameDataException() {
        super();
    }

    @SuppressWarnings("unused")
    public InvalidGameDataException(String message) {
        super(message);
    }

    @SuppressWarnings("unused")
    public InvalidGameDataException(String message, Throwable cause) {
        super(message, cause);
    }

    @SuppressWarnings("unused")
    public InvalidGameDataException(Throwable cause) {
        super(cause);
    }
}
