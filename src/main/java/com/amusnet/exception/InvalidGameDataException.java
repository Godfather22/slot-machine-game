package com.amusnet.exception;

public class InvalidGameDataException extends Exception {
    public InvalidGameDataException() {
        super();
    }

    public InvalidGameDataException(String message) {
        super(message);
    }

    public InvalidGameDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidGameDataException(Throwable cause) {
        super(cause);
    }
}
