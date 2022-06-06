package com.amusnet.game;

import com.amusnet.exception.InvalidOperationException;
import com.amusnet.game.impl.NumberCard;
import lombok.Data;

import java.util.List;

@Data
public class Screen {
    private int rowCount;
    private int columnCount; // reels

    private Card[][] view;

    public Screen(int rowCount, int columnCount) {
        this.rowCount = rowCount;
        this.columnCount = columnCount;

        view = new Card[rowCount][columnCount];
    }

    public Screen(List<List<Integer>> metaList) {
        this.rowCount = metaList.size();
        this.columnCount = metaList.get(0).size();
        view = new Card[rowCount][columnCount];

        for (int i = 0; i < rowCount; i++)
            for (int j = 0; j < columnCount; j++)
                view[i][j] = new NumberCard<Integer>(metaList.get(i).get(j));
    }

    public Card getCardAt(int row, int column) {
        return view[row][column];
    }

    public Number getCardValueAt(int row, int column) throws InvalidOperationException {
        var card = view[row][column];
        if (card instanceof NumberCard<?>)
            return ((NumberCard<?>) card).getValue();
        else
            throw new InvalidOperationException("Card element is not a NumberCard");
    }

}
