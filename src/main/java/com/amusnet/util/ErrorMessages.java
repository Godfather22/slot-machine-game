package com.amusnet.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A container for error messages. Messages can be retrieved by their given title. Pattern used is
 * <a href="https://refactoring.guru/design-patterns/singleton/java/example#example-2">thread-safe singleton</a>.
 *
 * @since 1.0
 */
public class ErrorMessages {
    private final Map<String, ErrorMessage> messages;

    // private constructor, because ErrorMessages class is a thread-safe singleton
    private ErrorMessages() {
        this.messages = new ConcurrentHashMap<>();
    }

    private static volatile ErrorMessages instance;

    /**
     * Get the instance of the container in a thread-safe manner.
     *
     * @return The instance of the container.
     */
    // instance is retrieved using double-checked locking (DCL)
    public static ErrorMessages getInstance() {
        ErrorMessages result = instance;
        if (result != null)
            return result;
        synchronized (ErrorMessages.class) {
            if (instance == null)
                instance = new ErrorMessages();
            return instance;
        }
    }

    /**
     * Returns the error message associated with title, or null if no such exists.
     *
     * @param title The given title of the error message to return. Acts as a key.
     * @return The error message associated with title, or null if no such exists.
     */
    public String message(String title) {
        if (this.messages.containsKey(title))
            return this.messages.get(title).getMessage();
        else
            return "";
    }

    /**
     * Creates title-message key-value pair. If such a title already exists,
     * the method overwrites its error message.
     *
     * @param title The title associated with message. Acts as a key.
     * @param message The error message associated with title. Acts as a value.
     * @return The error message, now saved in container.
     */
    public String message(String title, String message) {
        this.messages.put(title, new ErrorMessage(message));
        return message;
    }

    /**
     * Default titles for common error messages in application.
     */
    public static class DefaultMessageTitles {
        public static final String TITLE_EMSG_INVALID_LINES_INPUT = "Invalid lines input";
        public static final String TITLE_EMSG_INVALID_BET_INPUT = "Invalid bet input";
        public static final String TITLE_EMSG_INCORRECT_LINES_INPUT = "Incorrect number of lines";
        public static final String TITLE_EMSG_INCORRECT_BET_INPUT = "Incorrect bet amount";
    }

}
