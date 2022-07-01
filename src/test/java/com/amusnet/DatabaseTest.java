package com.amusnet;

import com.amusnet.config.DatabaseConnectionJdbi;
import com.amusnet.config.GameConfig;
import com.amusnet.exception.ConfigurationInitializationException;
import com.amusnet.game.Game;
import org.assertj.db.type.Source;
import org.assertj.db.type.Table;
import org.jdbi.v3.core.Handle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Random;

import static org.assertj.db.api.Assertions.assertThat;

public class DatabaseTest {

    private static final Path XML_CONFIG = Path.of("src/main/resources/properties.xml");           // configuration file
    private static final Path XSD_VALIDATION = Path.of("src/main/resources/properties.xsd");       // configuration file validation

    private static final GameConfig CONFIG;
    private static final Source DATA_SOURCE;

    private static final int NUMBER_OF_INPUTS = 20;
    private static InputStream newStandardIn;
    private static String gameInstanceName;
    private static Table gameInstanceTable;
    private static Table gamesTable;
    private static String input;

    static {
        try {
            CONFIG = new GameConfig(XML_CONFIG, XSD_VALIDATION);
        } catch (ParserConfigurationException | IOException | SAXException | ConfigurationInitializationException e) {
            throw new RuntimeException(e);
        }

        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("src/main/resources/db.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        DATA_SOURCE = new Source(
                properties.getProperty("url"),
                properties.getProperty("user"),
                properties.getProperty("password")
        );
    }

    @BeforeAll
    static void playGame() {
        // generate random valid input
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < NUMBER_OF_INPUTS; i++) {
            int linesPlayed = rnd.nextInt(1, CONFIG.getLineCount() + 1);
            int betAmount = (int) rnd.nextDouble(1.0, CONFIG.getBetLimit());
            sb.append(linesPlayed).append(" ").append(betAmount);
            sb.append(System.lineSeparator());
        }

        // append exit command to end of input
        sb.append(CONFIG.getExitCommand());

        // save input as string
        input = sb.toString();

        // pass input to custom input stream and make it System.in
        rewireStandardInput(sb.toString());

        // play a game instance
        gameInstanceName = new Game().setHistoryTracking(true).play();
    }

    @AfterAll   // history for test games not needed
    static void dropGameInstanceTable() {
        DatabaseConnectionJdbi dbc = DatabaseConnectionJdbi.getInstance();
        try (Handle handle = dbc.jdbi().open()) {
            handle.execute("DROP TABLE " + gameInstanceName + "; ");
        }
        assertThat(gameInstanceTable).doesNotExist();
    }

    private static void rewireStandardInput(String input) {
        newStandardIn = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        System.setIn(newStandardIn);
    }

    @Test
    void whenGameInstanceHasFinishedPlaying_thenMainGamesTableExists() {
        gamesTable = new Table(DATA_SOURCE, "games");
        assertThat(gamesTable).exists();
    }

    @Test
    void whenGameInstanceHasFinishedPlaying_thenNewTableWithGameHistoryExists() {

        // AssertJ-DB API
        gameInstanceTable = new Table(DATA_SOURCE, gameInstanceName);
        assertThat(gameInstanceTable).exists();

        // check whether history corresponds to input data
        String[] inputs = input.split(System.lineSeparator());
        for (int i = 0; i < NUMBER_OF_INPUTS; i++) {
            assertThat(gameInstanceTable).column("turn")
                    .value(i).isEqualTo(i + 1);

            String[] lineAndBetInputs = inputs[i].split(" ");

            assertThat(gameInstanceTable).column("lines_played")
                    .value(i).isEqualTo(Integer.parseInt(lineAndBetInputs[0]));

            assertThat(gameInstanceTable).column("bet_amount")
                    .value(i).isEqualTo(Double.parseDouble(lineAndBetInputs[1]));

            // TODO find an elegant way to check for exact sum
            assertThat(gameInstanceTable).column("total_win")
                    .value(i).isGreaterThanOrEqualTo(0.0);

            // TODO find an elegant way to check for exact roll
            assertThat(gameInstanceTable).column("reel_rolls")
                    .value(i).isNotNull();
        }

    }

}
