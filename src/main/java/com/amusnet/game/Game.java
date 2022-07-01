package com.amusnet.game;

import com.amusnet.config.DatabaseConnectionJdbi;
import com.amusnet.config.GameConfig;
import com.amusnet.exception.ConfigurationInitializationException;
import com.amusnet.game.components.GameRound;
import com.amusnet.game.components.GameState;
import com.amusnet.game.components.InfoScreen;
import com.amusnet.game.components.ReelScreen;
import com.amusnet.util.ErrorMessages;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.statement.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

import static com.amusnet.util.ErrorMessages.DefaultMessageTitles.*;

/**
 * Represents a configured game. Game logic and calculations are provided.
 *
 * @see GameConfig
 */
public class Game {

    public static final GameConfig CONFIGURATION;
    private static final Logger log = LoggerFactory.getLogger(Game.class);
    private static final Path XML_CONFIG = Path.of("src/main/resources/properties.xml");           // configuration file
    private static final Path XSD_VALIDATION = Path.of("src/main/resources/properties.xsd");       // configuration file validation

    static {
        try {
            CONFIGURATION = new GameConfig(XML_CONFIG, XSD_VALIDATION);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            log.error("Fatal error while configuring game");
            throw new RuntimeException(e);
        } catch (ConfigurationInitializationException e) {
            log.error("Configuration constraint violated", e);
            throw new RuntimeException(e);
        }
    }

    private final String gameName;
    private final LocalDateTime gameCreated;

    private final GameState gameState;
    private final InfoScreen infoScreen;

    boolean saveHistory;

    {
        // set creation time
        this.gameCreated = LocalDateTime.now();

        // set up game state
        this.gameState = new GameState(CONFIGURATION.getStartingBalance(), new GameRound(CONFIGURATION));

        // set up info screen
        this.infoScreen = new InfoScreen(CONFIGURATION, gameState);

        // by default game's history will NOT be tracked
        this.saveHistory = false;
    }

    /**
     * Creates a fully-configured game instance.
     */
    public Game() {
        this.gameName = "game" + this.gameCreated.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSS"));
    }

    /**
     * Creates a fully-configured game instance.
     *
     * @param gameName Name of the game instance.
     */
    @SuppressWarnings("unused")
    public Game(String gameName) {
        this.gameName = gameName;
    }

    //************************
    //* Field Access Methods *
    //************************

    public GameConfig getConfiguration() {
        return CONFIGURATION;
    }

    public GameState getGameState() {
        return gameState;
    }

    public Game setHistoryTracking(boolean flag) {
        this.saveHistory = flag;
        return this;
    }

    //*********************
    //* GAME START METHOD *
    //*********************

    @SuppressWarnings("UnusedReturnValue")
    public String play() {
        //log.debug(System.lineSeparator() + game.getConfiguration().toString());

        ErrorMessages errorMessages = ErrorMessages.getInstance();

        if (saveHistory)
            setupGameInstanceTable();

        Scanner sc = new Scanner(System.in);
        final int maxLines = this.getConfiguration().getLineCount();
        final double betLimit = this.getConfiguration().getBetLimit();

        final String exitCommand = this.getConfiguration().getExitCommand();

        // main this loop
        while (this.getGameState().getCurrentBalance() >= 0.0) {

            this.prompt();

            String firstInput = sc.next();
            if (firstInput.equalsIgnoreCase(exitCommand))
                break;

            int linesInput = -1;
            double betInput = -1.0;

            // valid input checks
            {
                try {
                    linesInput = Integer.parseInt(firstInput);
                } catch (NumberFormatException e) {
                    System.err.println(errorMessages.message
                            (TITLE_EMSG_INVALID_LINES_INPUT, "Invalid input for number of lines played!"));
                    log.error("Invalid user input for number of lines played: {}", linesInput);
                    continue;
                }

                try {
                    betInput = Double.parseDouble(sc.next());
                } catch (NumberFormatException e) {
                    System.err.println(errorMessages.message
                            (TITLE_EMSG_INVALID_BET_INPUT, "Invalid input for bet amount!"));
                    log.error("Invalid user input for bet amount: {}", betInput);
                    continue;
                }
            }

            // bound checks
            {
                if (linesInput < 1 || linesInput > maxLines) {
                    System.err.println(errorMessages.message
                            (TITLE_EMSG_INCORRECT_LINES_INPUT, "Incorrect number of lines chosen!"));
                    log.error("Number of lines input {} out of bounds for available values: 1-{}",
                            linesInput, maxLines);
                    continue;
                }
                if (betInput < 1 || betInput > betLimit) {
                    System.err.println(errorMessages.message
                            (TITLE_EMSG_INCORRECT_BET_INPUT, "Incorrect bet amount placed!"));
                    log.error("Bet amount input {} out of bounds for available values: 1-{}",
                            betInput, betLimit);
                    continue;
                }
            }

            this.setupNextRound(linesInput, betInput);
            this.getGameState().subtractFromBalance(betInput * linesInput);

            // feedback
            System.out.printf("%s\t%s%nBalance: %s%n%n%s%n",
                    this.getGameState().getGameRound().getLinesPlayed(),
                    this.getConfiguration().getCurrencyFormat().format(this.getGameState().getGameRound().getBetAmount()),
                    this.getConfiguration().getCurrencyFormat().format(this.getGameState().getCurrentBalance()),
                    this.getGameState().getGameRound().getReelScreen()
            );

            // side effect - prints necessary info to screen
            double totalWin = this.playNextRound();

            if (saveHistory)
                writeTurnToDatabase(
                        this.getGameState().getGameRound().getLinesPlayed(),
                        this.getGameState().getGameRound().getBetAmount(),
                        totalWin
                );

            System.out.println();
        }

        // out of loop, game has ended
        System.out.println("game over");
        return this.gameName;
    }

    //*********************
    //* GAME LOOP METHODS *
    //*********************

    private void prompt() {
        infoScreen.print();
    }

    private void setupNextRound(int linesPlayed, double betAmount) {
        setupNextRound(linesPlayed, betAmount, true);
    }

    @SuppressWarnings("UnusedReturnValue")
    public ReelScreen setupNextRound(int linesPlayed, double betAmount, boolean generateReelScreen) {
        ReelScreen rs = null;
        if (generateReelScreen)
            rs = gameState.getGameRound().getReelScreen().generateScreen();
        gameState.getGameRound().setLinesPlayed(linesPlayed);
        gameState.getGameRound().setBetAmount(betAmount);
        return rs;
    }

    public double playNextRound() {
        double win = gameState.getGameRound().playRound();
        if (win > 0.0)
            gameState.addToBalance(win);
        return win;
    }

    private void setupGameInstanceTable() {
        DatabaseConnectionJdbi dbc = DatabaseConnectionJdbi.getInstance();
        try (Handle handle = dbc.jdbi().open()) {
            handle.execute("""
                    CREATE TABLE IF NOT EXISTS `games` (
                      `id` int NOT NULL AUTO_INCREMENT,
                      `started` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      `name` varchar(100) NOT NULL,
                      PRIMARY KEY (`id`),
                      UNIQUE KEY `id_UNIQUE` (`id`),
                      UNIQUE KEY `name_UNIQUE` (`name`)
                    )""");

            handle.execute("""
                    CREATE TABLE\040""" + gameName + """ 
                       (
                      `turn` int NOT NULL AUTO_INCREMENT,
                      `lines_played` int NOT NULL,
                      `bet_amount` decimal(10,0) NOT NULL,
                      `total_win` decimal(10,0) NOT NULL,
                      PRIMARY KEY (`turn`),
                      UNIQUE KEY `turn_UNIQUE` (`turn`)
                    )""");

            Update recordGame = handle.createUpdate("INSERT INTO `games` (started, name) VALUES (:timestamp, :name); ");
            recordGame.bind("timestamp", this.gameCreated).bind("name", this.gameName);
            recordGame.execute();
        }
    }

    private void writeTurnToDatabase(int lines, double bet, double win) {
        DatabaseConnectionJdbi dbc = DatabaseConnectionJdbi.getInstance();
        try (Handle handle = dbc.jdbi().open()) {
            Update update = handle.createUpdate("INSERT INTO " + gameName + " " +
                    "(`lines_played`, `bet_amount`, `total_win`)" +
                    "VALUES " +
                    "(:lines, :bet, :win); ");
            update.bind("lines", lines)
                    .bind("bet", bet)
                    .bind("win", win);
            update.execute();
        }
    }

}
