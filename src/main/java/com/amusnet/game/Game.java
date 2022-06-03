package com.amusnet.game;

import com.amusnet.config.GameConfig;
import com.amusnet.exception.InvalidCurrencyFormatException;
import com.amusnet.exception.InvalidOperationException;
import com.amusnet.game.impl.IntegerCard;
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
public class Game<C extends Card, M extends Number> {

    private Properties properties;

    // TODO eventually replace this with the above properties implementation
    private GameConfig<C, M> configuration;

    private Screen screen;

    private M currentBalance;

    private Integer linesPlayed;
    private M betAmount;

    private M lastWinFromLines, lastWinFromScatters;
    private boolean gameOver;

    // private constructor, because Game class is a thread-safe singleton
    private Game() {

        // retrieve game properties
        properties = new Properties();
        try {
            properties.load(new InputStreamReader(new FileInputStream("game.properties")));
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

    // TODO research generic methods
    public M calculateWinAmount() {

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
                // TODO calculate current win amount
                double currentWinAmount = 0;
                totalWinAmount += currentWinAmount;
                // TODO consider scatters
                System.out.printf("Line %d, Card %s x%d, win amount %s",
                        winningCardValue, winningCardOccurrences, currentWinAmount);
            }
        }

        // TODO return type
        return null;
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
    private Pair<IntegerCard, Integer> getOccurrencesForLine(List<Integer> line) {
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
            return new Pair<>(new IntegerCard(previousCardValue), streakCount);

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
