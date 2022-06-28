package com.amusnet.game;

import com.amusnet.config.GameConfig;
import com.amusnet.exception.MissingTableElementException;
import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.List;

public class GameRound {

    private final GameConfig config;
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

    @SuppressWarnings("unused")
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

        // 0-based, i.e. line with index 0 is the first line, etc...
        for (int i = 0; i < linesPlayed; i++) {
            var currentLine = config.getLines().get(i);
            List<Integer> lineCards = new ArrayList<>(currentLine.size());

            // construct array containing the cards in current line
            for (int j = 0; j < currentLine.size(); j++) {
                var card = this.reelScreen.getCardAt(currentLine.get(j), j);
                lineCards.add(card);
            }

            var cardOccursAndWin = getOccurrencesAndWinForLineCards(lineCards);    // heavy-lifting happens here

            if (cardOccursAndWin != null) {
                var winningCardValue = cardOccursAndWin.getValue0();
                var winningCardOccurrences = cardOccursAndWin.getValue1();
                var currentWinAmount = cardOccursAndWin.getValue2();
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
        double scatterWinAmount;
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

    //*************************
    //* HEAVY-LIFTING METHODS *
    //*************************

    private Triplet<Integer, Integer, Double> getOccurrencesAndWinForLineCards(List<Integer> lineCards) {

        var wildcard = this.config.getWildcard();

        // if there are no wildcards in the lineCards
        if (!lineCards.contains(wildcard))
            return extractLineWinNoWildcards(lineCards);

        List<Boolean> wildcardMask = new ArrayList<>(config.getScreenColumnCount());

        // extract cards for current lineCards and
        // construct a 'wildcard mask'
        // e.g. for lineCards 1 6 6 1 2
        // mask is       f t t f f
        for (Integer lineCard : lineCards)
            wildcardMask.add(wildcard.equals(lineCard));

        return extractLineWin(lineCards, wildcardMask);
    }

    private Triplet<Integer, Integer, Double> extractLineWinNoWildcards(List<Integer> lineCards) {

        int previousCardValue, currentCardValue;
        int index = 1, streakCount = 1;
        do {
            previousCardValue = lineCards.get(index - 1);
            currentCardValue = lineCards.get(index);
            ++index;
            if (currentCardValue == previousCardValue)
                ++streakCount;
            else
                break;

            if (index >= lineCards.size())
                break;
        }
        while (true);

        var table = this.config.getTable();

        if (streakCount < table.getOccurrenceCounts().get(0))
            return null;
        else {
            // store win from card
            double win;
            try {
                win = table.calculateRegularWin(previousCardValue, streakCount, this.betAmount);
            } catch (MissingTableElementException e) {
                throw new RuntimeException(e);
            }

            // return
            return new Triplet<>(previousCardValue, streakCount, win);
        }

    }

    // TODO refactor out wildcardMask?

    /***
     *
     * Return a Quartet tuple with the following type arguments:
     * <br/>
     * A: Integer indicating the winning card<br/>
     * B: Integer indicating the win occurrence count for the winning card<br/>
     * C: Double indicating the win amount after considering A and B<br/>
     *
     * @param lineCards The cards as encountered in the current examined line.
     * @param wildcardMask The wildcard mask for the current line cards
     * @return A triplet containing the aforementioned information
     */
    private Triplet<Integer, Integer, Double> extractLineWin(List<Integer> lineCards, List<Boolean> wildcardMask) {

        var wildcard = config.getWildcard();
        var firstCardInLine = lineCards.get(0);
        var secondCardInLine = lineCards.get(1);

        // small optimization 1:
        // if first two lineCards are not equal and not wildcards
        // there's no possibility of a streak
        if (!firstCardInLine.equals(secondCardInLine) &&
                !firstCardInLine.equals(wildcard) &&
                !secondCardInLine.equals(wildcard)) {
            return null;
        }

        var table = this.config.getTable();

        // small optimization 2:
        // there are only wildcards among the current lineCards
        if (!wildcardMask.contains(false)) {
            // store amount of lineCards
            int lineCardsCount = lineCards.size();

            // store win from all wildcards (acting as normal cards)
            double winFromAllWildcards;
            try {
                winFromAllWildcards = table.calculateRegularWin(wildcard, lineCardsCount, this.betAmount);
            } catch (MissingTableElementException e) {
                throw new RuntimeException(e);
            }

            // return
            return new Triplet<>(wildcard, lineCards.size(), winFromAllWildcards);
        }

        boolean initialWildcard = wildcardMask.get(0).equals(true);

        var potentialWinningCard = firstCardInLine;
        for (int i = 1; potentialWinningCard.equals(wildcard); ++i)
            potentialWinningCard = lineCards.get(i);

        // apply mask
        // e.g. line 1 6 6 1 2
        // with mask f t t f f
        //
        // becomes   1 1 1 1 2
        List<Integer> appliedMaskLineCards = new ArrayList<>(lineCards.size());
        for (int i = 0; i < lineCards.size(); ++i) {
            if (wildcardMask.get(i).equals(true))
                appliedMaskLineCards.add(i, potentialWinningCard);
            else
                appliedMaskLineCards.add(i, lineCards.get(i));
        }

        // count potential winning card's occurrences in applied mask lineCards
        int potentialOccurrences = getFirstOccurrencesForCard(potentialWinningCard, appliedMaskLineCards);

        // store win after mask application
        double appliedMaskWin;
        try {
            appliedMaskWin = table.calculateRegularWin(potentialWinningCard, potentialOccurrences, this.betAmount);
        } catch (MissingTableElementException e) {
            return null;
        }

        if (initialWildcard) {
            // count wildcard occurrences in applied mask lineCards
            int wildcardOccurrences = getFirstOccurrencesForCard(wildcard, lineCards);

            // store win from wildcards (acting as normal cards)
            double wildcardWin;
            try {
                wildcardWin = table.calculateRegularWin(wildcard, wildcardOccurrences, this.betAmount);
            } catch (MissingTableElementException e) {
                throw new RuntimeException(e);
            }

            /*
            Compare with appliedMaskWin and return appropriate info.
            The '>' case is skipped, since it is returned even if
            program execution does not step into current if statement's
            body.
             */

            if (appliedMaskWin < wildcardWin)
                return new Triplet<>(wildcard, wildcardOccurrences, wildcardWin);

            if (appliedMaskWin == wildcardWin)
                if (potentialWinningCard > wildcardWin)
                    return new Triplet<>(potentialWinningCard, potentialOccurrences, appliedMaskWin);
                else
                    return new Triplet<>(wildcard, wildcardOccurrences, wildcardWin);

        }

        return new Triplet<>(potentialWinningCard, potentialOccurrences, appliedMaskWin);

    }

    //*******************
    //* UTILITY METHODS *
    //*******************

    /***
     *
     * Returns the number of times a card is encountered in the beginning
     * of the current line of cards.
     * <br/><br/>
     * Example:
     * getFirstOccurrencesForCard(1, {1,1,6,5,2})
     *      -> will return 2
     *
     * @param card The card for which the occurrence count will be returned.
     * @param lineCards The cards present on the current examined line.
     * @return The number of times card is *continuously* present from the beginning of the line
     */
    private int getFirstOccurrencesForCard(Integer card, List<Integer> lineCards) {
        int potentialOccurrences = 0;
        while (potentialOccurrences < lineCards.size()
                && lineCards.get(potentialOccurrences).equals(card))
            ++potentialOccurrences;
        return potentialOccurrences;
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
