package com.amusnet.config;

import com.amusnet.game.Card;
import lombok.Data;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO constraints
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

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append(String.format("%-10s", "card")).append(" ");

            for (var m : occurrenceCounts)
                sb.append(String.format("%5s", m));

            sb.append("\n");

            var keys = data.keySet();
            keys.forEach(key -> {
                sb.append(String.format("%-10s", key)).append(" ");
                var rightCells = data.get(key).values();
                rightCells.forEach(cell -> sb.append(String.format("%5.2s", cell)).append(" "));
                sb.append(System.lineSeparator());
            });
            return sb.toString();
        }

    }

    private List<List<Integer>> reels;
    private List<List<Integer>> lines;

    private MultipliersTable<T> table = new MultipliersTable<>();

    private double startingBalance;
    private double betLimit;

    private Set<Card> scatters;

    private DecimalFormat currencyFormat = new DecimalFormat();

    public void setupTable(List<Integer> occurrenceCounts, Map<T, Map<Integer, Integer>> data) {
        table.occurrenceCounts = occurrenceCounts;
        table.data = data;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Current configuration:\n\n");

        sb.append("Starting balance: ").append(startingBalance).append("\n");

        sb.append("Max Bet Amount: ").append(betLimit).append("\n");

        sb.append("Reel arrays:\n");
        reels.forEach(ra -> sb.append(ra).append("\n"));

        sb.append("Line arrays:\n");
        lines.forEach(la -> sb.append(la).append("\n"));

        sb.append("Multipliers Table:\n");
        sb.append(table);

        sb.append("Currency format:\n");
        sb.append(this.currencyFormat.toPattern());

        sb.append(System.lineSeparator());

        return sb.toString();
    }

}
