package com.amusnet.game;

import com.amusnet.config.GameConfig;
import org.javatuples.Pair;

import java.util.List;

public class GameRound {

    private GameConfig config;
    private ReelScreen reelScreen;

    private int linesPlayed;
    private double betAmount;

    private double winFromLines, winFromScatters;

    public GameRound(GameConfig config, ReelScreen reelScreen) {
        this.config = config;
        this.reelScreen = reelScreen;
    }

    @SuppressWarnings("unused")
    public GameRound() {
    }

    @SuppressWarnings("unused")
    public GameRound(int linesPlayed, double betAmount) {
        this.linesPlayed = linesPlayed;
        this.betAmount = betAmount;
    }

    @SuppressWarnings("unused")
    public GameRound(ReelScreen reelScreen) {
        this.reelScreen = reelScreen;
    }

    @SuppressWarnings("unused")
    public GameRound(ReelScreen reelScreen, int linesPlayed, double betAmount) {
        this.reelScreen = reelScreen;
        this.linesPlayed = linesPlayed;
        this.betAmount = betAmount;
    }

    public ReelScreen getReelScreen() {
        return reelScreen;
    }

    public void setReelScreen(ReelScreen reelScreen) {
        this.reelScreen = reelScreen;
    }

    public int getLinesPlayed() {
        return linesPlayed;
    }

    public void setLinesPlayed(int linesPlayed) {
        this.linesPlayed = linesPlayed;
    }

    public double getBetAmount() {
        return betAmount;
    }

    public void setBetAmount(double betAmount) {
        this.betAmount = betAmount;
    }

    public double getWinFromLines() {
        return winFromLines;
    }

    public double getWinFromScatters() {
        return winFromScatters;
    }

    /**
     * Calculates the total win amount for the current bet, that is the sum
     * of line wins and scatter wins.
     *
     * @return The sum of line wins and scatter wins.
     */
    public double playRound() {

        double totalWinAmount = 0;

        for (int i = 0; i < linesPlayed; i++) {
            var currentLine = config.getLines().get(i);
            var occurs = getOccurrencesForLine(currentLine);
            if (occurs != null) {
                var winningCardValue = occurs.getValue0();
                var winningCardOccurrences = occurs.getValue1();
                var currentWinAmount = calculateRegularWins(occurs);
                if (currentWinAmount != 0.0) {
                    totalWinAmount += currentWinAmount;
                    System.out.printf("Line %d, Card %s x%d, win amount %s%n",
                            i + 1, winningCardValue, winningCardOccurrences,
                            config.getCurrencyFormat().format(currentWinAmount));
                }
            }
        }

        // win from lines is the total win amount up until now
        this.winFromLines = totalWinAmount;

        // for the sake of extensibility: in case there are more than one "scatter cards"
        double scatterWinAmount = 0.0;
        for (Integer s : config.getScatters()) {
            int scatterCount = getScatterCount(s);
            scatterWinAmount = calculateScatterWins(s, scatterCount);
            this.winFromScatters = scatterWinAmount;
            if (scatterWinAmount != 0.0) {
                totalWinAmount += scatterWinAmount;
                System.out.printf("Scatters %s x%d, win amount %s%n",
                        s, scatterCount,
                        config.getCurrencyFormat().format(scatterWinAmount));
            }
        }

        if (totalWinAmount == 0.0)
            System.out.println("No wins");
        return totalWinAmount;
    }

    //*******************
    //* UTILITY METHODS *
    //*******************

    // Note: 'line' in this method's vocabulary is meant in the context of the game
    private Pair<Integer, Integer> getOccurrencesForLine(List<Integer> line) {

        int previousCardValue, currentCardValue;
        int index = 1, streakCount = 1;
        do {
            previousCardValue = reelScreen.fetchScreen()[line.get(index - 1)][index - 1];
            currentCardValue = reelScreen.fetchScreen()[line.get(index)][index];
            ++index;
            if (currentCardValue == previousCardValue)
                ++streakCount;
            else
                break;

            if (index >= line.size())
                break;
        }
        while (true);

        if (streakCount < config.getTable().getOccurrenceCounts().get(0))
            return null;
        else
            return new Pair<>(previousCardValue, streakCount);

    }

    private double calculateRegularWins(Pair<Integer, Integer> occurs) {
        var tableData = config.getTable().getData();
        var rightSide = tableData.get(occurs.getValue0());
        var multiplier = rightSide.get(occurs.getValue1());
        return this.betAmount * multiplier;
    }

    private double calculateScatterWins(Integer scatterValue, int scatterCount) {
        var calcTable = config.getTable();

        // If the amount of scatters on screen is a valid win amount
        if (calcTable.getOccurrenceCounts().contains(scatterCount)) {
            // then calculate and return the win amount.
            Integer multiplier = calcTable.getData().get(scatterValue).get(scatterCount);
            var totalBet = this.betAmount * this.linesPlayed;
            return totalBet * multiplier;
        }
        return 0.0; // not enough scatters or none at all
    }

    private int getScatterCount(int scatterValue) {
        var screenView = this.reelScreen.fetchScreen();
        int scatterCount = 0;
        for (int i = 0; i < this.reelScreen.getRowCount(); i++)
            for (int j = 0; j < this.reelScreen.getColumnCount(); j++)
                if (scatterValue == screenView[i][j])
                    ++scatterCount;
        return scatterCount;
    }
}
