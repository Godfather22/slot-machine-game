package com.amusnet.game;

import com.amusnet.game.impl.IntegerCard;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

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

}
