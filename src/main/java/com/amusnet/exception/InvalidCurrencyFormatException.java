package com.amusnet.exception;

@Deprecated
public class InvalidCurrencyFormatException extends Exception {
    public InvalidCurrencyFormatException() {
        super();
    }

    public InvalidCurrencyFormatException(String message) {
        super(message);
    }

    public InvalidCurrencyFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidCurrencyFormatException(Throwable cause) {
        super(cause);
    }
}
