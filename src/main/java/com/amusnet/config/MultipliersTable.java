package com.amusnet.config;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;

/**
 * Class that represents a table of multiplication values for
 * the number of occurrences for each card.
 */
public class MultipliersTable {

    private LinkedHashSet<Integer> occurrenceCounts;

    private Map<Integer, Map<Integer, Integer>> data;

    private int minStreakCount = Integer.MAX_VALUE;
    private int maxStreakCount = Integer.MIN_VALUE;

    private int largestMultiplierAmount = Integer.MIN_VALUE;

    public LinkedHashSet<Integer> getOccurrenceCounts() {
        return occurrenceCounts;
    }

    public void setOccurrenceCounts(LinkedHashSet<Integer> occurrenceCounts) {
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

    public int getLargestMultiplierAmount() {
        return largestMultiplierAmount;
    }

    public void setLargestMultiplierAmount(int largestMultiplierAmount) {
        this.largestMultiplierAmount = largestMultiplierAmount;
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

        var cards = data.keySet();
        cards.forEach(card -> {
            sb.append(String.format("%-10s", card)).append(" ");
            var cardRightColumns = data.get(card);
            occurrenceCounts.forEach(oc -> {
                if (cardRightColumns.containsKey(oc))
                    sb.append(String.format("%5s", cardRightColumns.get(oc)));
                else
                    sb.append(String.format("%5s", " "));
            });
            sb.append(System.lineSeparator());
        });

        sb.append("Minimum streak count: ").append(minStreakCount);
        sb.append(System.lineSeparator());
        sb.append("Maximum streak count: ").append(maxStreakCount);
        sb.append(System.lineSeparator());

        return sb.toString();
    }

}