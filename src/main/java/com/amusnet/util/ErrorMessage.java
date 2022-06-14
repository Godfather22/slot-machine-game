package com.amusnet.util;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * A simple class for holding error message strings.
 *
 * @since 1.0
 */
@AllArgsConstructor
@EqualsAndHashCode
public class ErrorMessage {
    @Getter
    private String message;

    @Override
    public String toString() {
        return message;
    }

}
