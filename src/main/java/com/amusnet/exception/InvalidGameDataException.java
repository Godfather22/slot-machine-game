package com.amusnet.exception;

/**
 * Signals that a crucial part of the game configuration is missing or is in an invalid state.
 *
 * @deprecated Configuration validation is now done in GameConfig
 * @see com.amusnet.config.GameConfig
 * @see ConfigurationInitializationException
 */
@Deprecated
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
