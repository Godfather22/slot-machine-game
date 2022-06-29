package com.amusnet.game;

import com.amusnet.config.GameConfig;
import com.amusnet.exception.ConfigurationInitializationException;
import com.amusnet.game.components.GameRound;
import com.amusnet.game.components.GameState;
import com.amusnet.game.components.InfoScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Represents a configured game. Game logic and calculations are provided.
 *
 * @see GameConfig
 * @since 1.0
 */
public class Game {

    public static final GameConfig CONFIGURATION;
    private static final Logger log = LoggerFactory.getLogger(Game.class);
    private static final Path xmlConfig = Path.of("src/main/resources/properties.xml");           // configuration file
    private static final Path xsdValidation = Path.of("src/main/resources/properties.xsd");       // configuration file validation

    static {
        try {
            CONFIGURATION = new GameConfig(xmlConfig, xsdValidation);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            log.error("Fatal error while configuring game");
            throw new RuntimeException(e);
        } catch (ConfigurationInitializationException e) {
            log.error("Configuration constraint violated", e);
            throw new RuntimeException(e);
        }
    }

    private final GameState gameState;

    private final InfoScreen infoScreen;
    private final GameRound gameRound;

    /**
     * Creates a fully-configured game instance.
     */
    public Game() {

        // set up game state
        this.gameState = new GameState(CONFIGURATION.getStartingBalance(), 0.0, 0.0);

        // set up info screen
        this.infoScreen = new InfoScreen(CONFIGURATION, gameState);

        // set up game round
        this.gameRound = new GameRound(CONFIGURATION);

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

    public GameRound getGameRound() {
        return gameRound;
    }

//*********************
    //* Main Game Methods *
    //*********************

    public void prompt() {
        infoScreen.print();
    }

    public void setupNextRound(int linesPlayed, double betAmount) {
        setupNextRound(linesPlayed, betAmount, true);
    }

    public void setupNextRound(int linesPlayed, double betAmount, boolean generateReelScreen) {
        if (generateReelScreen)
            this.gameRound.getReelScreen().generateScreen();
        this.gameRound.setLinesPlayed(linesPlayed);
        this.gameRound.setBetAmount(betAmount);
    }

    public double playNextRound() {
        double win = gameRound.playRound();
        if (win != 0.0) {
            double balanceUntilNow = gameState.getCurrentBalance();
            gameState.setCurrentBalance(balanceUntilNow + win);
        }
        return win;
    }

}
