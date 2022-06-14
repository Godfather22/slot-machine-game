package com.amusnet.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * A simple class to represent a card of integer values.
 *
 * @deprecated No other type of card values are required for API other than integer.
 */
@Deprecated
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
