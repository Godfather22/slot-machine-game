package com.amusnet.exception;

/**
 * Indicates that a constraint was violated during configuration initialization.
 * The XML containing configuration properties should be checked for valid content.
 */
public class ConfigurationInitializationException extends Exception {
    public ConfigurationInitializationException() {
        super();
    }

    public ConfigurationInitializationException(String message) {
        super(message);
    }

    public ConfigurationInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigurationInitializationException(Throwable cause) {
        super(cause);
    }
}
