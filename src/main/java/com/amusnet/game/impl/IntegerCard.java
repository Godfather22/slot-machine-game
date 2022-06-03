package com.amusnet.game.impl;

import lombok.NonNull;

public class IntegerCard extends NumberCard<Integer> {

    public IntegerCard(@NonNull Integer value, boolean scatter) {
        super(value, scatter);
    }

    public IntegerCard(@NonNull Integer value) {
        super(value);
    }
}
