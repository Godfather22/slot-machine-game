package com.amusnet.game.components;

import com.amusnet.config.GameConfig;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * A simple class for representing the game screen.
 */
public class ReelScreen {

    private GameConfig config;

    private int rowCount;
    private int columnCount; // reels

    private int[][] view;

    public ReelScreen(GameConfig config) {
        this.config = config;
        rowCount = config.getScreenRowCount();
        columnCount = config.getScreenColumnCount();
        view = new int[rowCount][columnCount];
    }

    /**
     * Initialize a screen via a list containing a list of Integers.
     *
     * @param metaList A list containing a list of Integers.
     */
    public ReelScreen(List<List<Integer>> metaList) {
        fromList(metaList);
    }

    private void fromList(List<List<Integer>> metaList) {
        this.rowCount = metaList.size();
        this.columnCount = metaList.get(0).size();
        view = new int[rowCount][columnCount];

        for (int i = 0; i < rowCount; i++)
            for (int j = 0; j < columnCount; j++)
                view[i][j] = metaList.get(i).get(j);
    }

    /**
     * Fetches A COPY OF the current reel screen view.
     *
     * @return A copy of the current reel screen view.
     */
    public int[][] fetchScreen() {
        return Arrays.copyOf(view, view.length);
    }

    public int getCardAt(int row, int column) {
        return view[row][column];
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getColumnCount() {
        return columnCount;
    }

    /**
     * Generates a two-dimensional array of integers which represents the game screen.
     *
     * @return The updated screen property.
     * @see ReelScreen
     */
    public ReelScreen generateScreen() {
        Random rnd = new Random();
        int[] diceRolls = new int[config.getScreenColumnCount()];
        for (int i = 0; i < diceRolls.length; i++)
            diceRolls[i] = rnd.nextInt(config.getReels().get(0).size());
        return generateScreen(diceRolls);
    }

    /**
     * Generates a two-dimensional array of integers which represents the game screen.
     * The generation is controlled by an array of integers, called 'dice rolls'.
     * Each dice roll corresponds to the initial position in the reel arrays from
     * which the population of the screen reels will begin. If the position is towards
     * the end of the reel array and more elements are needed than are available until
     * the end of the reel array, an overflow occurs and the rest of the elements are
     * chosen from the beginning of the reel array.
     * <br></br>
     * <br></br>
     * Example:
     * <br></br>
     * <br></br>
     * A diceRoll of 28 is generated for the following reel array:
     * [6,6,6,1,1,1,0,0,0,3,3,3,4,4,4,2,2,2,5,5,5,1,1,1,7,4,4,4,2,2]
     * <br></br>
     * <br></br>
     * The elements for the screen array will be the 28th, 29th and 0th (2, 2, 6).
     * <br></br>
     *
     * @return The updated screen property.
     * @see ReelScreen
     */
    public ReelScreen generateScreen(int[] diceRolls) {

        var reelArrays = config.getReels();
        int screenReelSize = config.getScreenRowCount();
        int screenRowsSize = config.getScreenColumnCount();
        for (int i = 0; i < screenRowsSize; i++) {
            int index = diceRolls[i];
            for (int j = 0; j < screenReelSize; j++) {
                if (index >= reelArrays.get(i).size())
                    index = 0;
                view[j][i] = reelArrays.get(i).get(index);
                index += 1;
            }
        }
        return this;
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
