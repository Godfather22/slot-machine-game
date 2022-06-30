package com.amusnet.game.components;

import com.amusnet.config.MultipliersTable;
import com.amusnet.exception.MissingTableElementException;

public class WinCalculator {
    private final MultipliersTable table;

    public WinCalculator(MultipliersTable table) {
        this.table = table;
    }

    public double calculateRegularWin(Integer card, Integer occurrenceCount, double betAmount) throws MissingTableElementException {
        var targetCardRightColumns = table.getData().get(card);
        if (targetCardRightColumns == null)
            throw new MissingTableElementException("No such card in multipliers table");

        var multiplicationAmount = targetCardRightColumns.get(occurrenceCount);
        if (multiplicationAmount == null)
            throw new MissingTableElementException("No such occurrence count card in multipliers table");

        return betAmount * multiplicationAmount;
    }

    public double calculateScatterWin(Integer scatterValue, int scatterCount, double betAmount) throws MissingTableElementException {
        if (!table.getData().containsKey(scatterValue))
            throw new MissingTableElementException("No such card in multipliers table");

        // If Card class weren't deprecated, it'd be useful here to easily check whether
        // scatterValue is actually a scatter value, without additional fields or arguments

        // If the amount of scatters on screen is a valid win amount
        if (table.getData().get(scatterValue).get(scatterCount) != null) {
            // then calculate and return the win amount.
            Integer multiplier = table.getData().get(scatterValue).get(scatterCount);
            return betAmount * multiplier;
        }
        return 0.0; // not enough scatters or none at all
    }

}
