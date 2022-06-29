package com.amusnet.game.components;

/**
 * A simple class to represent a card of integer values.
 *
 * @deprecated No other type of card values are required for API other than integer.
 */
@Deprecated
public class Card {

    private final int value;
    private boolean isScatter;

    public Card(int value, boolean isScatter) {
        this.value = value;
        this.isScatter = isScatter;
    }

    public Card(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public boolean isScatter() {
        return isScatter;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
