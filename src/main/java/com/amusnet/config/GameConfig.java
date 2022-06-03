package com.amusnet.config;

import com.amusnet.game.Card;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GameConfig<T extends Card<?>, M extends Number> {

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

    @Data
    static class MultipliersTable<T> {

        private Map<String, Integer> columnIndexes;
        private Map<T, Map<Integer, Integer>> data;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append(String.format("%s-10", "card")).append(" ");

            var columnIndexesKeySet = getColumnIndexes().keySet();
            for (var key : columnIndexesKeySet)
                sb.append(String.format("%s-5", key));

            sb.append("\n");

            var keys = data.keySet();
            keys.forEach(key -> {
                sb.append(String.format("%s-10", key)).append(" ");
                var rightCells = data.get(key).values();
                rightCells.forEach(cell -> sb.append(String.format("%s-10.2", cell)).append(" "));
            });
            return sb.toString();
        }

    }

    private List<List<Integer>> reels;
    private List<List<Integer>> lines;

    MultipliersTable<T> table;

    private Integer startingBalance;
    private M maxBetAmount;

    public void setupTable(Map<String, Integer> columnIndexes, Map<T, Map<Integer, Integer>> data) {
        table.columnIndexes = columnIndexes;
        table.data = data;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Current configuration:\n\n");

        sb.append("Starting balance: ").append(startingBalance).append("\n\n");

        sb.append("Max Bet Amount: ").append(maxBetAmount).append("\n\n");

        sb.append("Reel arrays:\n");
        reels.forEach(sb::append);
        sb.append("\n\n");

        sb.append("Line arrays:\n");
        lines.forEach(sb::append);
        sb.append("\n\n");

        sb.append("Multipliers Table:\n");
        sb.append(table);
        sb.append("\n\n");

        return sb.toString();
    }

}
