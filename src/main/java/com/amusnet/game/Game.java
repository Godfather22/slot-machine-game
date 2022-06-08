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
import java.text.DecimalFormat;
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

    DecimalFormat currencyFormat;
    private double lastWinFromLines, lastWinFromScatters;

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

        // TODO not thread-safe
        // set currency format (whole numbers or floating-point)
        this.currencyFormat = new DecimalFormat("#.##");

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

    public void prompt() {
        System.out.printf("Balance: %s | Lines available: 1-%d | Bets per lines available: 1-%s%n",
                currentBalance, configuration.getLines().size(), configuration.getMaxBetAmount());
        System.out.println("Please enter lines you want to play on and a bet per line:");
    }

    public void generateScreen() {
        generateScreen(new Random().nextInt(configuration.getReels().get(0).size()));
    }

    public void generateScreen(int diceRoll) {
        var reelArrays = configuration.getReels();
        int screenReelSize = Integer.parseInt(properties.getProperty("screen_rows"));
        int screenRowsSize = Integer.parseInt(properties.getProperty("screen_columns"));
        for (int i = 0; i < screenRowsSize; i++) {
            int index = diceRoll;
            for (int j = 0; j < screenReelSize; j++) {
                if (index >= reelArrays.get(i).size())
                    index = 0;
                screen.getView()[j][i] = new NumberCard<Integer>(reelArrays.get(i).get(index));     //TODO generify?
                index += 1;
            }
        }
    }

    public double calculateTotalWin() {

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
                var currentWinAmount =  calculateRegularWins(occurs);
                if (currentWinAmount != 0.0) {
                    totalWinAmount += currentWinAmount;
                    System.out.printf("Line %d, Card %s x%d, win amount %s%n",
                            i + 1, winningCardValue, winningCardOccurrences, currencyFormat.format(currentWinAmount));
                }
            }
        }
        // for the sake of extensibility: in case there are more than one "scatter cards"
        for (var s : configuration.getScatters()) {
            int scatterCount = 0;
            double scatterWinAmount = calculateScatterWins(s, scatterCount);
            if (scatterWinAmount != 0.0) {
                totalWinAmount += scatterWinAmount;
                System.out.printf("Scatters %s x%d, win amount %s%n",
                        s.toString(), scatterCount, this.currencyFormat.format(scatterWinAmount));
            }
        }
        return totalWinAmount;
    }

    //*******************
    //* UTILITY METHODS *
    //*******************

    // Note: 'line' in this method's vocabulary is meant in the context of the game
    private Pair<NumberCard<Integer>, Integer> getOccurrencesForLine(List<Integer> line) {  // TODO generify
        // check if there is a streak, starting from the beginning
        boolean streak = true;

        Integer previousCardValue, currentCardValue;
        int index = 1, streakCount = 0;
        do {
            try {
                previousCardValue = (Integer) screen.getCardValueAt(line.get(0), 0);
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

            if (index >= line.size())
                break;
        }
        while (streak);

        if (streakCount < configuration.getTable().getOccurrenceCounts().get(0))
            return null;
        else
            return new Pair<>(new NumberCard<Integer>(previousCardValue), streakCount);

    }

    private <R extends Number, T extends NumberCard<R>> double calculateRegularWins(Pair<T, Integer> occurs) {
        var tableData = configuration.getTable().getData();
        var row = tableData.get((occurs.getValue0()));
        var multiplier = row.get(occurs.getValue1());
        return this.betAmount * multiplier;
    }

    private double calculateScatterWins(Card scatterCard, int scatterCount) {
        var screenView = this.screen.getView();

        for (int i = 0; i < this.screen.getRowCount(); i++)
            for (int j = 0; j < this.screen.getColumnCount(); j++)
                if ((screenView[i][j].toString()).equals(scatterCard.toString()))
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
