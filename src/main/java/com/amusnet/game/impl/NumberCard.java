package com.amusnet.game.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Deprecated
public class NumberCard<T extends Number> {

    @NonNull
    private T value;
    private boolean scatter;

    public boolean isScatter() {
        return scatter;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
