package com.amusnet.game.impl;

import com.amusnet.game.Card;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class NumberCard implements Card<Number> {
    @NonNull
    Number value;
    boolean scatter;
}
