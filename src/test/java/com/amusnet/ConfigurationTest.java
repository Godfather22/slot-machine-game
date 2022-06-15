package com.amusnet;

import com.amusnet.config.GameConfig;
import com.amusnet.exception.ConfigurationInitializationException;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class ConfigurationTest {

    private static GameConfig config1, config2;

    @BeforeAll
    public static void setup() {
        File xmlConfig = new File("src/main/resources/properties.xml");
        File xsdValidation = new File("src/main/resources/properties.xsd");

        try {
            config1 = new GameConfig(xmlConfig, xsdValidation);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            log.error("Error initializing configuration", e);
            throw new RuntimeException(e);
        } catch (ConfigurationInitializationException e) {
            log.error("Configuration constraint violated", e);
            throw new RuntimeException(e);
        }
        config2 = new GameConfig();
        configManualSetup(config2);
    }

    @Test
    public void givenOneConfigurationFromXmlAndOneManualWhichAreEqual_theyAreTrulyEqual() {
        Assertions.assertThat(config1)
                .usingRecursiveComparison()
                .ignoringFields("table")    // TODO No
                .isEqualTo(config2);
    }

    private static void configManualSetup(GameConfig configuration) {

        configuration.setScreenRowCount(3);
        configuration.setScreenColumnCount(5);
        configuration.setLineCount(20);
        configuration.setExitCommand("quit");

        // set up reel arrays
        {
            configuration.setReels(List.of(
                    List.of(6,6,6,1,1,1,0,0,0,3,3,3,4,4,4,2,2,2,5,5,5,1,1,1,7,4,4,4,2,2),
                    List.of(6,6,6,2,2,2,1,1,1,0,0,0,5,5,5,1,1,1,7,3,3,3,2,2,2,0,0,0,5,5),
                    List.of(6,6,6,4,4,4,0,0,0,1,1,1,5,5,5,2,2,2,7,3,3,3,0,0,0,2,2,2,5,5),
                    List.of(6,6,6,2,2,2,4,4,4,0,0,0,5,5,5,3,3,3,1,1,1,7,2,2,2,0,0,0,4,4),
                    List.of(6,6,6,1,1,1,4,4,4,2,2,2,5,5,5,0,0,0,7,1,1,1,3,3,3,2,2,2,5,5)

            ));
        }

        // set up lines
        {
            configuration.setLines(List.of(
                    List.of(1, 1, 1, 1, 1),     // 1
                    List.of(0, 0, 0, 0, 0),
                    List.of(2, 2, 2, 2, 2),
                    List.of(0, 1, 2, 1, 0),
                    List.of(2, 1, 0, 1, 2),     // 5
                    List.of(0, 0, 1, 2, 2),
                    List.of(2, 2, 1, 0, 0),
                    List.of(1, 2, 2, 2, 1),
                    List.of(1, 0, 0, 0, 1),
                    List.of(0, 1, 1, 1, 0),     // 10
                    List.of(2, 1, 1, 1, 2),
                    List.of(1, 2, 1, 0, 1),
                    List.of(1, 0, 1, 2, 1),
                    List.of(0, 1, 0, 1, 0),
                    List.of(2, 1, 2, 1, 2),     // 15
                    List.of(1, 1, 2, 1, 1),
                    List.of(1, 1, 0, 1, 1),
                    List.of(0, 2, 0, 2, 0),
                    List.of(2, 0, 2, 0, 2),
                    List.of(1, 0, 2, 0, 1)      // 20

            ));
        }

        // set up table
        {
            var occurrenceCounts = List.of(3, 4, 5);

            Map<Integer, Map<Integer, Integer>> tableData = Map.of(
                    0, Map.of(  3, 10,
                            4, 20,
                            5, 100),
                    1, Map.of(  3, 10,
                            4, 20,
                            5, 100),
                    2, Map.of(  3, 10,
                            4, 20,
                            5, 100),
                    3, Map.of(  3, 20,
                            4, 40,
                            5, 200),
                    4, Map.of(  3, 20,
                            4, 40,
                            5, 200),
                    5, Map.of(  3, 20,
                            4, 80,
                            5, 400),
                    6, Map.of(  3, 40,
                            4, 400,
                            5, 1000),
                    7, Map.of(  3, 5,
                            4, 20,
                            5, 500)
            );

            configuration.setupTable(occurrenceCounts, tableData);
        }

        // set scatters
        configuration.setScatters(Set.of(7));

        // set starting balance
        configuration.setStartingBalance(100000);

        // set max bet amount
        configuration.setBetLimit(10);

        // TODO not thread-safe
        // set currency format
        configuration.getCurrencyFormat().applyPattern("#");
    }

}
