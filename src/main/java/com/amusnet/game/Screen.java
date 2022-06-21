package com.amusnet.game;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A simple class for representing the game screen.
 *
 * @since 1.0
 */
public class Screen<T> {
    private final int rowCount;
    private final int columnCount; // reels

    private final T[][] view;

    /**
     * Initialize a screen of size rowCount x columnCount.
     *
     * @param rowCount    The number of rows.
     * @param columnCount The number of columns.
     */
    @SuppressWarnings("unchecked")
    public Screen(int rowCount, int columnCount) {
        this.rowCount = rowCount;
        this.columnCount = columnCount;

        view = (T[][]) new Object[rowCount][columnCount];
    }

    /**
     * Initialize a screen via a list containing a list of Integers.
     *
     * @param metaList A list containing a list of Integers.
     */
    @SuppressWarnings("unchecked")
    public Screen(List<List<T>> metaList) {
        this.rowCount = metaList.size();
        this.columnCount = metaList.get(0).size();
        view = (T[][]) new Object[rowCount][columnCount];

        for (int i = 0; i < rowCount; i++)
            for (int j = 0; j < columnCount; j++)
                view[i][j] = metaList.get(i).get(j);
    }

    public T[][] fetchScreen() {
        return Arrays.copyOf(view, view.length);
    }

    public T getCardAt(int row, int column) {
        return view[row][column];
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getColumnCount() {
        return columnCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Screen<?> screen = (Screen<?>) o;
        return rowCount == screen.rowCount && columnCount == screen.columnCount && Arrays.deepEquals(view, screen.view);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(rowCount, columnCount);
        result = 31 * result + Arrays.deepHashCode(view);
        return result;
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
