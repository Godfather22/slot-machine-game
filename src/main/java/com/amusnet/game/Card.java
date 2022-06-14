package com.amusnet.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Card {

    @NonNull
    private int value;
    private boolean isScatter;

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
