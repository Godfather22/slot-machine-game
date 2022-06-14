package com.amusnet.game;

import lombok.Data;

import java.util.List;

/**
 * A simple class for representing the game screen.
 *
 * @since 1.0
 */
@Data
public class Screen {
    private int rowCount;
    private int columnCount; // reels

    private int[][] view;

    /**
     * Initialize a screen of size rowCount x columnCount.
     *
     * @param rowCount The number of rows.
     * @param columnCount The number of columns.
     */
    public Screen(int rowCount, int columnCount) {
        this.rowCount = rowCount;
        this.columnCount = columnCount;

        view = new int[rowCount][columnCount];
    }

    /**
     * Initialize a screen via a list containing a list of Integers.
     *
     * @param metaList A list containing a list of Integers.
     */
    public Screen(List<List<Integer>> metaList) {
        this.rowCount = metaList.size();
        this.columnCount = metaList.get(0).size();
        view = new int[rowCount][columnCount];

        for (int i = 0; i < rowCount; i++)
            for (int j = 0; j < columnCount; j++)
                view[i][j] = metaList.get(i).get(j);
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
