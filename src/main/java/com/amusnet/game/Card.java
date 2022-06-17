package com.amusnet.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * A simple class to represent a card of integer values.
 *
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Card<T> {

    @NonNull
    private T face;
    private boolean isScatter;

    @Override
    public String toString() {
        return String.valueOf(face);
    }
}
