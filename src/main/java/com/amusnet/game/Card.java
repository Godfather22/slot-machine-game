package com.amusnet.game;

public interface Card<T> {
    void setValue(T value);
    T getValue();
    boolean isScatter();
}
