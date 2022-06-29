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
        if (targetCardRightColumns == null) {
            throw new MissingTableElementException("No such card in multipliers table");
        }

        var multiplicationAmount = targetCardRightColumns.get(occurrenceCount);
        if (multiplicationAmount == null) {
            throw new MissingTableElementException("No such occurrence count card in multipliers table");
        }

        return betAmount * multiplicationAmount;
    }

    // TODO make method for calculating scatters - throws exception if invalid scatter
}
