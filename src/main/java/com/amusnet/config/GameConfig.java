package com.amusnet.config;

import lombok.Data;

import java.util.List;
import java.util.Map;

// TODO Make singleton
@Data
public class GameConfig<T> {
    List<List<T>> reels;
    List<List<T>> lines;
    Map<T, Map<Short, Integer>> multipliers;
    Integer startingBalance;
}
