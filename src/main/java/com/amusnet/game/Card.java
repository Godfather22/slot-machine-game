package com.amusnet.game;

import java.util.Objects;

/**
 * A simple class to represent a card of integer values.
 */
public class Card<T> {

    private final T face;
    private boolean isScatter;

    public Card(T face, boolean isScatter) {
        this.face = face;
        this.isScatter = isScatter;
    }

    public Card(T face) {
        this.face = face;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card<?> card = (Card<?>) o;
        return isScatter == card.isScatter && face.equals(card.face);
    }

    @Override
    public int hashCode() {
        return Objects.hash(face, isScatter);
    }

    @Override
    public String toString() {
        return String.valueOf(face);
    }
}
