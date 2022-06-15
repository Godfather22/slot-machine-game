package com.amusnet.exception;

/**
 * Indicates that a constraint was violated during configuration initialization.
 * The XML containing configuration properties should be checked for valid content.
 */
public class ConfigurationInitializationException extends Exception {
    @SuppressWarnings("unused")
    public ConfigurationInitializationException() {
        super();
    }

    @SuppressWarnings("unused")
    public ConfigurationInitializationException(String message) {
        super(message);
    }

    @SuppressWarnings("unused")
    public ConfigurationInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    @SuppressWarnings("unused")
    public ConfigurationInitializationException(Throwable cause) {
        super(cause);
    }
}
