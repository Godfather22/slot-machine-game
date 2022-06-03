package com.amusnet.game.impl;

import com.amusnet.game.Card;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class NumberCard<T extends Number> implements Card {

    @NonNull
    private Integer value;
    private boolean scatter;

    @Override
    public boolean isScatter() {
        return false;
    }
}
