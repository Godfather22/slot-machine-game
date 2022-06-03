package com.amusnet.game;

import com.amusnet.exception.InvalidOperationException;
import com.amusnet.game.impl.IntegerCard;
import com.amusnet.game.impl.NumberCard;
import lombok.Data;

import java.util.List;

@Data
public class Screen {
    int rows;
    int columns; // reels

    Card[][] view;

    public Screen(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;

        view = new Card[rows][columns];
    }

    public Screen(List<List<Integer>> metaList) {
        this.rows = metaList.size();
        this.columns = metaList.get(0).size();
        view = new Card[rows][columns];

        for (int i = 0; i < rows; i++)
            for (int j = 0; j < columns; j++)
                view[i][j] = new IntegerCard(metaList.get(i).get(j));
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
