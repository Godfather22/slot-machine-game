package com.amusnet.util;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

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
