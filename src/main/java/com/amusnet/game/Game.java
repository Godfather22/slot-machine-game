package com.amusnet.game;

import com.amusnet.config.GameConfig;
import com.amusnet.exception.InvalidCurrencyFormatException;
import com.amusnet.exception.InvalidOperationException;
import com.amusnet.game.impl.NumberCard;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Properties;
import java.util.Random;

@Data
@Slf4j
public class Game<C extends Card> {

    private Properties properties;

    // TODO eventually replace this with the above properties implementation
    private GameConfig<C> configuration;

    private Screen screen;

    private double currentBalance;

    private Integer linesPlayed;
    private double betAmount;

    String currencyFormat;
    private double lastWinFromLines, lastWinFromScatters;
    private boolean gameOver;

    // private constructor, because Game class is a thread-safe singleton
    @SuppressWarnings("unchecked")
    private Game() {

        this.configuration = (GameConfig<C>) GameConfig.getInstance();

        // retrieve game properties
        properties = new Properties();
        try {
            properties.load(new InputStreamReader(new FileInputStream("src/main/resources/game.properties")));
        } catch (IOException e) {
            log.error("Error reading .properties file", e);
            throw new RuntimeException(e);
        }

        // set up screen size
        int rowSize = Integer.parseInt(properties.getProperty("screen_rows"));
        int columnSize = Integer.parseInt(properties.getProperty("screen_columns"));
        this.screen = new Screen(rowSize, columnSize);

        // set up initial balance
        String initialBalanceProp = properties.getProperty("starting_balance");
        try {
            initializeGenericSum(this.currentBalance, initialBalanceProp);
        } catch (InvalidCurrencyFormatException e) {
            log.error("Cannot parse '{}' as Number type", initialBalanceProp);
            throw new RuntimeException(e);
        }

        // set currency format (whole numbers or floating-point)
        this.currencyFormat = (properties.getProperty("round_currency").equals("true") ? "%d" : "%f.2");

        // game isn't over from the beginning, this isn't life
        this.gameOver = false;
    }

    @SuppressWarnings("rawtypes")
    private static volatile Game instance;

    // instance is retrieved using double-checked locking (DCL)
    @SuppressWarnings("rawtypes")
    public static Game getInstance() {
        Game result = instance;
        if (result != null)
            return result;
        synchronized (Game.class) {
            if (instance == null)
                instance = new Game();
            return instance;
        }
    }

    @SuppressWarnings("unchecked")
    public double calculateWinAmount() {

        double totalWinAmount = 0;

        // TODO migrate from configuration to property-based
        int numOfLines = configuration.getLines().size();
        for (int i = 0; i < numOfLines; i++) {
            var currentLine = configuration.getLines().get(i);
            var occurs = getOccurrencesForLine(currentLine);
            if (occurs != null) {
                var winningCard = occurs.getValue0();
                var winningCardValue = winningCard.getValue();
                var winningCardOccurrences = occurs.getValue1();
                var currentWinAmount =  calculateWinAmount(occurs);
                totalWinAmount += currentWinAmount;
                System.out.printf("Line %d, Card %s x%d, win amount " + this.currencyFormat,
                        i + 1, winningCardValue, winningCardOccurrences, currentWinAmount);
            }
            // for the sake of extensibility: in case there are more than one "scatter cards"
            for (var s : configuration.getScatters())
                totalWinAmount += winAmountFromScatters((C) s);
        }
        return totalWinAmount;
    }

    private double winAmountFromScatters(C scatterCard) {
        var screenView = this.screen.getView();

        // Count the amount of scatters on screen
        int scatterCount = 0;
        for (int i = 0; i < this.screen.getRowCount(); i++)
            for (int j = 0; j < this.screen.getColumnCount(); j++)
                if (screenView[i][j].equals(scatterCard))
                    ++scatterCount;

        var calcTable = configuration.getTable();

        // If the amount of scatters on screen is a valid win amount
        if (calcTable.getOccurrenceCounts().contains(scatterCount)) {
            // then calculate and return the win amount.
            Integer multiplier = calcTable.getData().get(scatterCard).get(scatterCount);
            var totalBet = this.betAmount * this.linesPlayed;
            return totalBet * multiplier;
        }
        return 0.0; // not enough scatters or none at all
    }

    private <R extends Number, T extends NumberCard<R>> double calculateWinAmount(Pair<T, Integer> occurs) {
        var tableData = configuration.getTable().getData();
        var row = tableData.get((occurs.getValue0()));
        var multiplier = row.get(occurs.getValue1());
        return this.betAmount * multiplier;
    }

    public void quit() {
        this.gameOver = true;
    }

    //*******************
    //* UTILITY METHODS *
    //*******************

    public void prompt() {
        System.out.printf("Balance: %s | Lines available: 1-%d | Bets per lines available: 1-%s%n",
                currentBalance, configuration.getLines().size(), configuration.getMaxBetAmount());
        System.out.println("Please enter lines you want to play on and a bet per line:");
    }

    // Note: 'line' in this method's vocabulary is meant in the context of the game
    private Pair<NumberCard<Integer>, Integer> getOccurrencesForLine(List<Integer> line) {
        // check if there is a streak, starting from the beginning
        boolean streak = true;

        Integer previousCardValue = line.get(0);
        Integer currentCardValue;
        int index = 0, streakCount = 0;
        while (streak) {
            try {
                currentCardValue = (Integer) screen.getCardValueAt(line.get(index), index);
                ++index;
            } catch (InvalidOperationException e) {
                log.error("Cannot call method for non NumberCard Cards", e);
                throw new RuntimeException(e);
            }
            if (currentCardValue.equals(previousCardValue))
                ++streakCount;
            else
                streak = false;
        }

        if (streakCount == 0)
            return null;
        else
            return new Pair<>(new NumberCard<Integer>(previousCardValue), streakCount);

    }

    public List<List<Integer>> generateScreen() {
        return generateScreen(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
    }

    public List<List<Integer>> generateScreen(long seed) {
        Random rnd = new Random(seed);
        // TODO generate screen algorithm
        return null;
    }

    private void initializeGenericSum(Number sum, String value) throws InvalidCurrencyFormatException {
        if (sum instanceof Integer)
            sum = Integer.parseInt(value);
        else if (sum instanceof Long)
            sum = Long.parseLong(value);
        else if (sum instanceof Double)
            sum = Double.parseDouble(value);
        else
            throw new InvalidCurrencyFormatException("Error parsing property type");
    }
}
