package com.amusnet.game.impl;

import com.amusnet.game.NumberCard;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class IntegerCard implements NumberCard<Integer> {
    @NonNull
    private Integer value;
    private boolean scatter;
}
