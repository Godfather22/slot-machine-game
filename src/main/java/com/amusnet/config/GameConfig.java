package com.amusnet.config;

import com.amusnet.game.Card;
import com.amusnet.game.impl.NumberCard;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class GameConfig<T extends Card> {

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
    public static class MultipliersTable<T> {

        private List<Integer> occurrenceCounts;  // should always be sorted, need order hence not a Set
        private Map<T, Map<Integer, Integer>> data;

        // TODO fix formatting
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append(String.format("%s-10", "card")).append(" ");

            for (var m : occurrenceCounts)
                sb.append(String.format("%s-5", m));

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

    private MultipliersTable<T> table = new MultipliersTable<>();

    private Integer startingBalance;
    private double maxBetAmount;

    private Set<Card> scatters;

    public void setupTable(List<Integer> occurrenceCounts, Map<T, Map<Integer, Integer>> data) {
        table.occurrenceCounts = occurrenceCounts;
        table.data = data;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Current configuration:\n\n");

        sb.append("Starting balance: ").append(startingBalance).append("\n");

        sb.append("Max Bet Amount: ").append(maxBetAmount).append("\n");

        sb.append("Reel arrays:\n");
        reels.forEach(ra -> sb.append(ra).append("\n"));

        sb.append("Line arrays:\n");
        lines.forEach(la -> sb.append(la).append("\n"));

        sb.append("Multipliers Table:\n");
        sb.append(table).append("\n");
        sb.append("\n");

        return sb.toString();
    }

}
