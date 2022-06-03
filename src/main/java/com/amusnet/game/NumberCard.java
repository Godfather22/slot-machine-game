package com.amusnet.game;

import com.amusnet.game.Card;

public interface NumberCard<T extends Number> extends Card {
    void setValue(T value);
    T getValue();
}
