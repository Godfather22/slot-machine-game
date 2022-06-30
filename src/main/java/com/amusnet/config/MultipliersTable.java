package com.amusnet.config;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A nested class within GameConfig which represents a table of multiplication values for
 * the number of occurrences for each card. Used to calculate the player win amounts.
 *
 * @since 1.0
 */
public class MultipliersTable {

    // TODO Deprecate
    private List<Integer> occurrenceCounts;  // should always be sorted, need order hence not a Set

    private Map<Integer, Map<Integer, Integer>> data;

    private int minStreakCount = Integer.MAX_VALUE;
    private int maxStreakCount;

    public List<Integer> getOccurrenceCounts() {
        return occurrenceCounts;
    }

    public void setOccurrenceCounts(List<Integer> occurrenceCounts) {
        this.occurrenceCounts = occurrenceCounts;
    }

    public Map<Integer, Map<Integer, Integer>> getData() {
        return data;
    }

    public void setData(Map<Integer, Map<Integer, Integer>> data) {
        this.data = data;
    }

    public int getMaxStreakCount() {
        return maxStreakCount;
    }

    public void setMaxStreakCount(int maxStreakCount) {
        this.maxStreakCount = maxStreakCount;
    }

    public int getMinStreakCount() {
        return minStreakCount;
    }

    public void setMinStreakCount(int minStreakCount) {
        this.minStreakCount = minStreakCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MultipliersTable that = (MultipliersTable) o;
        return occurrenceCounts.equals(that.occurrenceCounts) && data.equals(that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(occurrenceCounts, data);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("%-10s", "card")).append(" ");

        for (var m : occurrenceCounts)
            sb.append(String.format("%5s", m));

        sb.append(System.lineSeparator());

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