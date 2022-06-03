package com.amusnet.game.impl;

import com.amusnet.game.NumberCard;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public class IntegerCard implements NumberCard<Integer> {
    @NonNull
    private Integer value;
    private boolean scatter;
}
