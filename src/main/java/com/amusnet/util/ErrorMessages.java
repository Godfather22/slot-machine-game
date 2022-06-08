package com.amusnet.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ErrorMessages {
    private final Map<String, ErrorMessage> messages;

    // private constructor, because ErrorMessages class is a thread-safe singleton
    private ErrorMessages() {
        this.messages = new ConcurrentHashMap<>();
    }

    private static volatile ErrorMessages instance;

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

    public String message(String title) {
        return message(title, "");
    }

    public String message(String title, String message) {
        if (this.messages.containsKey(title))
            return this.messages.get(title).getMessage();

        this.messages.put(title, new ErrorMessage(message));
        return message;
    }

    public static class DefaultMessageTitles {
        public static final String TITLE_EMSG_INVALID_LINES_INPUT = "Invalid lines input";
        public static final String TITLE_EMSG_INVALID_BET_INPUT = "Invalid bet input";
        public static final String TITLE_EMSG_INCORRECT_LINES_INPUT = "Incorrect number of lines";
        public static final String TITLE_EMSG_INCORRECT_BET_INPUT = "Incorrect bet amount";
    }

}
