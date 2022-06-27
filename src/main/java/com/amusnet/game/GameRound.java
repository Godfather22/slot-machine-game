package com.amusnet.game;

import com.amusnet.config.GameConfig;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.List;

public class GameRound {

    private GameConfig config;
    private ReelScreen reelScreen;

    private int linesPlayed;
    private double betAmount;

    private double winFromLines, winFromScatters;

    @SuppressWarnings("unused")
    public GameRound(GameConfig config, ReelScreen reelScreen) {
        this.config = config;
        this.reelScreen = reelScreen;
    }

    public GameRound(GameConfig config) {
        this.config = config;
        this.reelScreen = new ReelScreen(config);
    }

    //******************
    //* ACCESS METHODS *
    //******************

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

    //****************
    //* MAIN METHODS *
    //****************

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

        List<Integer> lineCards = new ArrayList<>(config.getScreenColumnCount());

        for (int i = 0; i < line.size(); ++i) {
            lineCards.add(this.reelScreen.getCardAt(line.get(i), i));
        }

        Pair<Integer, Integer> result = null;
        if (lineCards.size() % 2 == 0)
            result = extractWinFromLine(lineCards);
        else {
            int lastElementIndex = lineCards.size() - 1;
            var lastCardInLine = lineCards.get(lastElementIndex);
            var noTailOccurrences = extractWinFromLine(lineCards.subList(0, lastElementIndex));
            if (noTailOccurrences != null) {
                if (lastCardInLine.equals(noTailOccurrences.getValue0()))
                    result = new Pair<>(noTailOccurrences.getValue0(), noTailOccurrences.getValue1() + 1);
            }
        }
        return result;
    }

    private Pair<Integer, Integer> extractWinFromLine(List<Integer> lineCards) {

        var wildcard = config.getWildcard();
        var firstCardInLine = lineCards.get(0);
        var secondCardInLine = lineCards.get(1);

        // small optimization:
        // if first two cards are not equal and not wildcards
        // there's no possibility of a streak
        if (!firstCardInLine.equals(secondCardInLine) &&
                !firstCardInLine.equals(wildcard) &&
                !secondCardInLine.equals(wildcard)) {
            return null;
        }

        Integer potentialWinningCard = firstCardInLine;
        boolean initialWildcard = firstCardInLine.equals(wildcard);
        int potentialOccurrences = 0;
        int wildcardOccurrences = 0;
        for (int i = 0; i < lineCards.size(); i += 2) {
            var leftCard = lineCards.get(i);
            var rightCard = lineCards.get(i + 1);

            boolean leftIsWildcard = leftCard.equals(wildcard);
            boolean rightIsWildcard = rightCard.equals(wildcard);

            if (leftIsWildcard) {
                wildcardOccurrences++;
            } else {
                potentialWinningCard = leftCard;
            }
            potentialOccurrences++;

            if (rightIsWildcard) {
                wildcardOccurrences++;
                potentialOccurrences++;
            } else {
                if (!leftIsWildcard)
                    if (leftCard.equals(rightCard))
                        potentialOccurrences++;
                    else
                        break;
                else if (potentialWinningCard.equals(rightCard) || potentialWinningCard.equals(wildcard))
                    potentialOccurrences++;
                else
                    break;
            }
        }

        if (potentialOccurrences < config.getTable().getOccurrenceCounts().get(0))
            return null;
        else
            return new Pair<>(potentialWinningCard, potentialOccurrences);
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
