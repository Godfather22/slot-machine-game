package com.amusnet.game;

import com.amusnet.config.GameConfig;
import com.amusnet.exception.ConfigurationInitializationException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * Represents a configured game. Game logic and calculations are provided.
 *
 * @since 1.0
 * @see GameConfig
 */
@Slf4j
public class Game<T> {

    @Getter
    private final GameConfig<T> configuration;
    @Getter
    private final Screen<T> screen;
    @Getter
    @Setter
    private double currentBalance;
    @Getter
    @Setter
    private int linesPlayed;
    @Getter
    @Setter
    private double betAmount;
    @Getter
    private double lastWinFromLines, lastWinFromScatters;

    /**
     * Creates a fully-configured game instance.
     *
     * @throws ParserConfigurationException If a DocumentBuilder cannot be created which satisfies the configuration requested.
     * @throws IOException If any IO errors occur.
     * @throws SAXException If any parse errors occur.
     */
    public Game() throws ParserConfigurationException, IOException, SAXException, ConfigurationInitializationException {

        File xmlConfig = new File("src/main/resources/letter-properties.xml");     // configuration file
        File xsdValidation = new File("src/main/resources/properties.xsd");     // configuration file validation

        this.configuration = new GameConfig<>(xmlConfig, xsdValidation);

        // set up screen size
        int rowSize = configuration.getScreenRowCount();
        int columnSize = configuration.getScreenColumnCount();
        this.screen = new Screen<>(rowSize, columnSize);

        // set up initial balance
        this.currentBalance = configuration.getStartingBalance();

    }

    /**
     * Prompt the user for input with an informative message.
     */
    public void prompt() {
        System.out.printf("Balance: %s | Lines available: 1-%d | Bets per lines available: 1-%s%n",
                this.configuration.getCurrencyFormat().format(this.currentBalance), configuration.getLines().size(),
                this.configuration.getCurrencyFormat().format(configuration.getBetLimit()));
        System.out.println("Please enter lines you want to play on and a bet per line: ");
    }

    /**
     * Generates a two-dimensional array of integers which represents the game screen.
     *
     * @return The updated screen property.
     * @see Screen
     */
    public Screen<T> generateScreen() {
        Random rnd = new Random();
        int[] diceRolls = new int[this.configuration.getScreenColumnCount()];
        for (int i = 0; i < diceRolls.length; i++)
            diceRolls[i] = rnd.nextInt(configuration.getReels().get(0).size());
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
     * @see Screen
     */
    public Screen<T> generateScreen(int[] diceRolls) {
        // tests do a better job than this
        //log.debug("DiceRoll for screen generation: {}", diceRoll);

        var reelArrays = this.configuration.getReels();
        int screenReelSize = this.configuration.getScreenRowCount();
        int screenRowsSize = this.configuration.getScreenColumnCount();
        for (int i = 0; i < screenRowsSize; i++) {
            int index = diceRolls[i];
            for (int j = 0; j < screenReelSize; j++) {
                if (index >= reelArrays.get(i).size())
                    index = 0;
                this.screen.getView()[j][i] = reelArrays.get(i).get(index);
                index += 1;
            }
        }
        return this.screen;
    }

    public double calculateTotalWinAndBalance() {
        double totalWin = calculateTotalWin();
        this.currentBalance += totalWin;
        return totalWin;
    }

    /**
     * Calculates the total win amount for the current bet, that is the sum
     * of line wins and scatter wins.
     *
     * @return The sum of line wins and scatter wins.
     */
    private double calculateTotalWin() {

        // should never come to this
//        if (this.linesPlayed < 1)
//            throw new InvalidGameDataException("Invalid value for field 'linesPlayed'");
//        if (this.betAmount < 1.0)
//            throw new InvalidGameDataException("Invalid value for field 'betAmount'");

        double totalWinAmount = 0;

        for (int i = 0; i < this.linesPlayed; i++) {
            var currentLine = configuration.getLines().get(i);
            var occurs = getOccurrencesForLine(currentLine);
            if (occurs != null) {
                var winningCardValue = occurs.getValue0();
                var winningCardOccurrences = occurs.getValue1();
                var currentWinAmount =  calculateRegularWins(occurs);
                if (currentWinAmount != 0.0) {
                    totalWinAmount += currentWinAmount;
                    System.out.printf("Line %d, Card %s x%d, win amount %s%n",
                            i + 1, winningCardValue, winningCardOccurrences,
                            this.configuration.getCurrencyFormat().format(currentWinAmount));
                }
            }
        }
        this.lastWinFromLines = totalWinAmount;

        // for the sake of extensibility: in case there are more than one "scatter cards"
        double scatterWinAmount = 0.0;
        for (T s : configuration.getScatters()) {
            int scatterCount = getScatterCount(s);
            scatterWinAmount = calculateScatterWins(s, scatterCount);
            if (scatterWinAmount != 0.0) {
                totalWinAmount += scatterWinAmount;
                System.out.printf("Scatters %s x%d, win amount %s%n",
                        s.toString(), scatterCount,
                        this.configuration.getCurrencyFormat().format(scatterWinAmount));
            }
        }
        this.lastWinFromScatters = scatterWinAmount;
        if (totalWinAmount == 0.0)
            System.out.println("No wins");
        return totalWinAmount;
    }

    //*******************
    //* UTILITY METHODS *
    //*******************

    // Note: 'line' in this method's vocabulary is meant in the context of the game
    private Pair<T, Integer> getOccurrencesForLine(List<Integer> line) {
        // check if there is a streak, starting from the beginning
        boolean streak = true;

        T previousCardValue, currentCardValue;
        int index = 1, streakCount = 1;
        do {
            previousCardValue = screen.getView()[line.get(index - 1)][index - 1];
            currentCardValue = screen.getView()[line.get(index)][index];
            ++index;
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
            return new Pair<>(previousCardValue, streakCount);

    }

    private double calculateRegularWins(Pair<T, Integer> occurs) {
        var tableData = configuration.getTable().getData();
        var rightSide = tableData.get(occurs.getValue0());
        var multiplier = rightSide.get(occurs.getValue1());
        return this.betAmount * multiplier;
    }

    private double calculateScatterWins(T scatterValue, int scatterCount) {
        var calcTable = this.configuration.getTable();

        // If the amount of scatters on screen is a valid win amount
        if (calcTable.getOccurrenceCounts().contains(scatterCount)) {
            // then calculate and return the win amount.
            Integer multiplier = calcTable.getData().get(scatterValue).get(scatterCount);
            var totalBet = this.betAmount * this.linesPlayed;
            return totalBet * multiplier;
        }
        return 0.0; // not enough scatters or none at all
    }

    private int getScatterCount(T scatterValue) {
        var screenView = this.screen.getView();
        int scatterCount = 0;
        for (int i = 0; i < this.screen.getRowCount(); i++)
            for (int j = 0; j < this.screen.getColumnCount(); j++)
                if (scatterValue.equals(screenView[i][j]))
                    ++scatterCount;
        return scatterCount;
    }
}
