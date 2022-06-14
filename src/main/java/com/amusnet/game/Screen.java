package com.amusnet.game;

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
                view[i][j] = new Card(metaList.get(i).get(j));
    }

    public Card getCardAt(int row, int column) {
        return view[row][column];
    }

    public Number getCardValueAt(int row, int column) {
        return view[row][column].getValue();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < this.rowCount; i++) {
            for (int j = 0; j < this.columnCount; j++)
                sb.append(String.format("%-3s", this.view[i][j]));
            sb.append(System.lineSeparator());
        }

        return sb.toString();
    }

}
