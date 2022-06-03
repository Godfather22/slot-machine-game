package com.amusnet.config;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GameConfig<T, M> {

    // thread-safe singleton
    private GameConfig() {}

    @SuppressWarnings("rawtypes")
    private static volatile GameConfig instance;

    // instance is retrieved using double-checked locking (DCL)
    @SuppressWarnings({"rawtypes"})
    public static GameConfig getInstance() {
        GameConfig result = instance;
        if (result != null)
            return result;
        synchronized (GameConfig.class) {
            if (instance == null)
                instance = new GameConfig();
            return instance;
        }
    }

    private List<List<T>> reels;
    private List<List<T>> lines;

    private Map<T, Map<Integer, Integer>> multipliers;

    private Integer startingBalance;
    private M maxBetAmount;

}
