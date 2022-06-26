package com.amusnet;

import com.amusnet.config.GameConfig;
import com.amusnet.exception.ConfigurationInitializationException;
import com.amusnet.util.ErrorMessages;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConfigurationTest {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationTest.class);
    private static GameConfig config1, config2;

    @BeforeAll
    public static void setup() {
        Path xmlConfig = Path.of("src/main/resources/properties.xml");
        Path xsdValidation = Path.of("src/main/resources/properties.xsd");

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

    @Nested
    @DisplayName("Testing configuration initialization exception throwing")
    class ConfigInitExceptionTest {

        private static final ErrorMessages errorMessages = ErrorMessages.getInstance();

        private static final Path tempConfigFile = Path.of("target/generated-test-sources/properties.xml");

        private static String invalidXmlContent;

        @BeforeAll
        static void createTempFileAndInitializeWriter() {
            try {
                if (!tempConfigFile.toFile().createNewFile())
                    log.warn("File {} already exists", tempConfigFile);
            } catch (IOException e) {
                log.error("Failed to crate test configuration file");
                throw new RuntimeException(e);
            }
        }

        @AfterAll
        static void deleteTestConfigFile() {
            try {
                Files.delete(tempConfigFile);
            } catch (IOException e) {
                log.error("Failed to delete temporary configuration file");
            }
        }

        @Test
        void configurationXmlHasInvalidCurrencyFormat_shouldThrowConfigInitException() {
            setInvalidCurrencyFormat();
            Exception e = getException();
            assertEquals(e.getMessage(), errorMessages.message(
                    ErrorMessages.DefaultMessageTitles.TITLE_EMSG_INVALID_CURRENCY_FORMAT
            ));
        }

        @Test
        void configurationXmlHasInvalidColumnSize_shouldThrowConfigInitException() {
            setInvalidColumnSize();
            Exception e = getException();
            assertEquals(e.getMessage(), errorMessages.message
                    (ErrorMessages.DefaultMessageTitles.TITLE_EMSG_REELS_DISCREPANCY));
        }

        @Test
        void configurationXmlHasInvalidReelArraysSize_shouldThrowConfigInitException() {
            setInvalidReelArraysSize();
            Exception e = getException();
            assertEquals(e.getMessage(), errorMessages.message
                    (ErrorMessages.DefaultMessageTitles.TITLE_EMSG_REELS_DISCREPANCY));
        }

        @Test
        void configurationXmlHasDuplicateCardInTable_shouldThrowConfigInitException() {
            setDuplicateCardInTable();
            Exception e = getException();
            assertEquals(e.getMessage(), errorMessages.message
                    (ErrorMessages.DefaultMessageTitles.TITLE_EMSG_TABLE_DUPLICATE_CARDS));
        }

        @Test
        void configurationXmlHasDuplicatedOccurrence_shouldThrowConfigInitException() {
            setDuplicateOccurrenceInTable();
            Exception e = getException();
            assertEquals(e.getMessage(), errorMessages.message
                    (ErrorMessages.DefaultMessageTitles.TITLE_EMSG_TABLE_DUPLICATE_OCCURRENCE));
        }

        @Test
        void configurationXmlHasVaryingCardMultipliers_shouldThrowConfigInitException() {
            setVaryingCardMultipliers();
            Exception e = getException();
            assertEquals(e.getMessage(), errorMessages.message
                    (ErrorMessages.DefaultMessageTitles.TITLE_EMSG_TABLE_MULTIPLIERS_DISCREPANCY));
        }

        @Test
        void configurationXmlHasVaryingCardOccurrences_shouldThrowConfigInitException() {
            setVaryingCardOccurrences();
            Exception e = getException();
            assertEquals(e.getMessage(), errorMessages.message
                    (ErrorMessages.DefaultMessageTitles.TITLE_EMSG_TABLE_OCCURRENCES_DISCREPANCY));
        }

        @Test
        void configurationXmlHasMissingCardInTable_shouldThrowConfigInitException() {
            setMissingCardInTable();
            Exception e = getException();
            assertEquals(e.getMessage(), errorMessages.message
                    (ErrorMessages.DefaultMessageTitles.TITLE_EMSG_TABLE_MISSING_CARDS));
        }

        @Test
        void configurationXmlHasScatterNotInTable_shouldThrowConfigInitException() {
            setScatterNotInTable();
            Exception e = getException();
            assertEquals(e.getMessage(), errorMessages.message
                    (ErrorMessages.DefaultMessageTitles.TITLE_EMSG_NONEXISTENT_SCATTER));
        }

        @Test
        void configurationXmlHasWildcardNotInTable_shouldThrowConfigInitException() {
            setWildcardNotInTable();
            Exception e = getException();
            assertEquals(e.getMessage(), errorMessages.message
                    (ErrorMessages.DefaultMessageTitles.TITLE_EMSG_NONEXISTENT_WILDCARD));
        }

        private Exception getException() {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempConfigFile.toFile(), false))) {
                writer.write(invalidXmlContent);
            } catch (IOException e) {
                log.error("Failed to initialize file reader");
                throw new RuntimeException(e);
            }
            return assertThrows(ConfigurationInitializationException.class, () ->
                    new GameConfig(tempConfigFile));
        }

        // TODO manual feeding of invalid xml content is tedious

        private void setInvalidCurrencyFormat() {
            invalidXmlContent = """
                    <?xml version="1.0" encoding="UTF-8" ?>

                    <properties>
                        <rows>3</rows>
                        <columns>5</columns>
                        <currency format="foo"/>
                        <balance>100000</balance>
                        <betlimit>10</betlimit>
                        <exit>quit</exit>

                        <reelArrays>
                            <reelArray>6,6,6,1,1,1,0,0,0,3,3,3,4,4,4,2,2,2,5,5,5,1,1,1,7,4,4,4,2,2</reelArray>
                            <reelArray>6,6,6,2,2,2,1,1,1,0,0,0,5,5,5,1,1,1,7,3,3,3,2,2,2,0,0,0,5,5</reelArray>
                            <reelArray>6,6,6,4,4,4,0,0,0,1,1,1,5,5,5,2,2,2,7,3,3,3,0,0,0,2,2,2,5,5</reelArray>
                            <reelArray>6,6,6,2,2,2,4,4,4,0,0,0,5,5,5,3,3,3,1,1,1,7,2,2,2,0,0,0,4,4</reelArray>
                            <reelArray>6,6,6,1,1,1,4,4,4,2,2,2,5,5,5,0,0,0,7,1,1,1,3,3,3,2,2,2,5,5</reelArray>
                        </reelArrays>

                        <lineArrays>
                            <lineArray>1,1,1,1,1</lineArray>
                            <lineArray>0,0,0,0,0</lineArray>
                            <lineArray>2,2,2,2,2</lineArray>
                            <lineArray>0,1,2,1,0</lineArray>
                            <lineArray>2,1,0,1,2</lineArray>
                            <lineArray>0,0,1,2,2</lineArray>
                            <lineArray>2,2,1,0,0</lineArray>
                            <lineArray>1,2,2,2,1</lineArray>
                            <lineArray>1,0,0,0,1</lineArray>
                            <lineArray>0,1,1,1,0</lineArray>
                            <lineArray>2,1,1,1,2</lineArray>
                            <lineArray>1,2,1,0,1</lineArray>
                            <lineArray>1,0,1,2,1</lineArray>
                            <lineArray>0,1,0,1,0</lineArray>
                            <lineArray>2,1,2,1,2</lineArray>
                            <lineArray>1,1,2,1,1</lineArray>
                            <lineArray>1,1,0,1,1</lineArray>
                            <lineArray>0,2,0,2,0</lineArray>
                            <lineArray>2,0,2,0,2</lineArray>
                            <lineArray>1,0,2,0,1</lineArray>
                        </lineArrays>

                        <scatters>7</scatters>
                        
                        <wildcard>6</wildcard>

                        <multipliers>
                            <card face="0">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="1">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="2">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="3">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="40"/>
                                <multiplier occurrences="5" amount="200"/>
                            </card>
                            <card face="4">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="40"/>
                                <multiplier occurrences="5" amount="200"/>
                            </card>
                            <card face="5">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="80"/>
                                <multiplier occurrences="5" amount="400"/>
                            </card>
                            <card face="6">
                                <multiplier occurrences="3" amount="40"/>
                                <multiplier occurrences="4" amount="400"/>
                                <multiplier occurrences="5" amount="1000"/>
                            </card>
                            <card face="7">
                                <multiplier occurrences="3" amount="5"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="500"/>
                            </card>
                        </multipliers>
                    </properties>""";
        }

        private void setInvalidColumnSize() {
            invalidXmlContent = """
                    <?xml version="1.0" encoding="UTF-8" ?>

                    <properties>
                        <rows>3</rows>
                        <columns>4</columns>
                        <currency format="round"/>
                        <balance>100000</balance>
                        <betlimit>10</betlimit>
                        <exit>quit</exit>

                        <reelArrays>
                            <reelArray>6,6,6,1,1,1,0,0,0,3,3,3,4,4,4,2,2,2,5,5,5,1,1,1,7,4,4,4,2,2</reelArray>
                            <reelArray>6,6,6,2,2,2,1,1,1,0,0,0,5,5,5,1,1,1,7,3,3,3,2,2,2,0,0,0,5,5</reelArray>
                            <reelArray>6,6,6,4,4,4,0,0,0,1,1,1,5,5,5,2,2,2,7,3,3,3,0,0,0,2,2,2,5,5</reelArray>
                            <reelArray>6,6,6,2,2,2,4,4,4,0,0,0,5,5,5,3,3,3,1,1,1,7,2,2,2,0,0,0,4,4</reelArray>
                            <reelArray>6,6,6,1,1,1,4,4,4,2,2,2,5,5,5,0,0,0,7,1,1,1,3,3,3,2,2,2,5,5</reelArray>
                        </reelArrays>

                        <lineArrays>
                            <lineArray>1,1,1,1,1</lineArray>
                            <lineArray>0,0,0,0,0</lineArray>
                            <lineArray>2,2,2,2,2</lineArray>
                            <lineArray>0,1,2,1,0</lineArray>
                            <lineArray>2,1,0,1,2</lineArray>
                            <lineArray>0,0,1,2,2</lineArray>
                            <lineArray>2,2,1,0,0</lineArray>
                            <lineArray>1,2,2,2,1</lineArray>
                            <lineArray>1,0,0,0,1</lineArray>
                            <lineArray>0,1,1,1,0</lineArray>
                            <lineArray>2,1,1,1,2</lineArray>
                            <lineArray>1,2,1,0,1</lineArray>
                            <lineArray>1,0,1,2,1</lineArray>
                            <lineArray>0,1,0,1,0</lineArray>
                            <lineArray>2,1,2,1,2</lineArray>
                            <lineArray>1,1,2,1,1</lineArray>
                            <lineArray>1,1,0,1,1</lineArray>
                            <lineArray>0,2,0,2,0</lineArray>
                            <lineArray>2,0,2,0,2</lineArray>
                            <lineArray>1,0,2,0,1</lineArray>
                        </lineArrays>

                        <scatters>7</scatters>
                        
                        <wildcard>6</wildcard>

                        <multipliers>
                            <card face="0">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="1">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="2">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="3">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="40"/>
                                <multiplier occurrences="5" amount="200"/>
                            </card>
                            <card face="4">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="40"/>
                                <multiplier occurrences="5" amount="200"/>
                            </card>
                            <card face="5">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="80"/>
                                <multiplier occurrences="5" amount="400"/>
                            </card>
                            <card face="6">
                                <multiplier occurrences="3" amount="40"/>
                                <multiplier occurrences="4" amount="400"/>
                                <multiplier occurrences="5" amount="1000"/>
                            </card>
                            <card face="7">
                                <multiplier occurrences="3" amount="5"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="500"/>
                            </card>
                        </multipliers>
                    </properties>""";
        }

        private void setInvalidReelArraysSize() {
            invalidXmlContent = """
                    <?xml version="1.0" encoding="UTF-8" ?>

                    <properties>
                        <rows>3</rows>
                        <columns>5</columns>
                        <currency format="round"/>
                        <balance>100000</balance>
                        <betlimit>10</betlimit>
                        <exit>quit</exit>

                        <reelArrays>
                            <reelArray>6,6,6,1,1,1,0,0,0,3,3,3,4,4,4,2,2,2,5,5,5,1,1,1,7,4,4,4,2,2</reelArray>
                            <reelArray>6,6,6,2,2,2,1,1,1,0,0,0,5,5,5,1,1,1,7,3,3,3,2,2,2,0,0,0,5,5</reelArray>
                            <reelArray>6,6,6,4,4,4,0,0,0,1,1,1,5,5,5,2,2,2,7,3,3,3,0,0,0,2,2,2,5,5</reelArray>
                            <reelArray>6,6,6,2,2,2,4,4,4,0,0,0,5,5,5,3,3,3,1,1,1,7,2,2,2,0,0,0,4,4</reelArray>
                        </reelArrays>

                        <lineArrays>
                            <lineArray>1,1,1,1,1</lineArray>
                            <lineArray>0,0,0,0,0</lineArray>
                            <lineArray>2,2,2,2,2</lineArray>
                            <lineArray>0,1,2,1,0</lineArray>
                            <lineArray>2,1,0,1,2</lineArray>
                            <lineArray>0,0,1,2,2</lineArray>
                            <lineArray>2,2,1,0,0</lineArray>
                            <lineArray>1,2,2,2,1</lineArray>
                            <lineArray>1,0,0,0,1</lineArray>
                            <lineArray>0,1,1,1,0</lineArray>
                            <lineArray>2,1,1,1,2</lineArray>
                            <lineArray>1,2,1,0,1</lineArray>
                            <lineArray>1,0,1,2,1</lineArray>
                            <lineArray>0,1,0,1,0</lineArray>
                            <lineArray>2,1,2,1,2</lineArray>
                            <lineArray>1,1,2,1,1</lineArray>
                            <lineArray>1,1,0,1,1</lineArray>
                            <lineArray>0,2,0,2,0</lineArray>
                            <lineArray>2,0,2,0,2</lineArray>
                            <lineArray>1,0,2,0,1</lineArray>
                        </lineArrays>

                        <scatters>7</scatters>
                        
                        <wildcard>6</wildcard>

                        <multipliers>
                            <card face="0">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="1">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="2">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="3">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="40"/>
                                <multiplier occurrences="5" amount="200"/>
                            </card>
                            <card face="4">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="40"/>
                                <multiplier occurrences="5" amount="200"/>
                            </card>
                            <card face="5">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="80"/>
                                <multiplier occurrences="5" amount="400"/>
                            </card>
                            <card face="6">
                                <multiplier occurrences="3" amount="40"/>
                                <multiplier occurrences="4" amount="400"/>
                                <multiplier occurrences="5" amount="1000"/>
                            </card>
                            <card face="7">
                                <multiplier occurrences="3" amount="5"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="500"/>
                            </card>
                        </multipliers>
                    </properties>""";
        }

        private void setDuplicateCardInTable() {
            invalidXmlContent = """
                    <?xml version="1.0" encoding="UTF-8" ?>

                    <properties>
                        <rows>3</rows>
                        <columns>5</columns>
                        <currency format="round"/>
                        <balance>100000</balance>
                        <betlimit>10</betlimit>
                        <exit>quit</exit>

                        <reelArrays>
                            <reelArray>6,6,6,1,1,1,0,0,0,3,3,3,4,4,4,2,2,2,5,5,5,1,1,1,7,4,4,4,2,2</reelArray>
                            <reelArray>6,6,6,2,2,2,1,1,1,0,0,0,5,5,5,1,1,1,7,3,3,3,2,2,2,0,0,0,5,5</reelArray>
                            <reelArray>6,6,6,4,4,4,0,0,0,1,1,1,5,5,5,2,2,2,7,3,3,3,0,0,0,2,2,2,5,5</reelArray>
                            <reelArray>6,6,6,2,2,2,4,4,4,0,0,0,5,5,5,3,3,3,1,1,1,7,2,2,2,0,0,0,4,4</reelArray>
                            <reelArray>6,6,6,1,1,1,4,4,4,2,2,2,5,5,5,0,0,0,7,1,1,1,3,3,3,2,2,2,5,5</reelArray>
                        </reelArrays>

                        <lineArrays>
                            <lineArray>1,1,1,1,1</lineArray>
                            <lineArray>0,0,0,0,0</lineArray>
                            <lineArray>2,2,2,2,2</lineArray>
                            <lineArray>0,1,2,1,0</lineArray>
                            <lineArray>2,1,0,1,2</lineArray>
                            <lineArray>0,0,1,2,2</lineArray>
                            <lineArray>2,2,1,0,0</lineArray>
                            <lineArray>1,2,2,2,1</lineArray>
                            <lineArray>1,0,0,0,1</lineArray>
                            <lineArray>0,1,1,1,0</lineArray>
                            <lineArray>2,1,1,1,2</lineArray>
                            <lineArray>1,2,1,0,1</lineArray>
                            <lineArray>1,0,1,2,1</lineArray>
                            <lineArray>0,1,0,1,0</lineArray>
                            <lineArray>2,1,2,1,2</lineArray>
                            <lineArray>1,1,2,1,1</lineArray>
                            <lineArray>1,1,0,1,1</lineArray>
                            <lineArray>0,2,0,2,0</lineArray>
                            <lineArray>2,0,2,0,2</lineArray>
                            <lineArray>1,0,2,0,1</lineArray>
                        </lineArrays>

                        <scatters>7</scatters>
                        
                        <wildcard>6</wildcard>

                        <multipliers>
                            <card face="0">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="0">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="1">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="2">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="3">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="40"/>
                                <multiplier occurrences="5" amount="200"/>
                            </card>
                            <card face="4">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="40"/>
                                <multiplier occurrences="5" amount="200"/>
                            </card>
                            <card face="5">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="80"/>
                                <multiplier occurrences="5" amount="400"/>
                            </card>
                            <card face="6">
                                <multiplier occurrences="3" amount="40"/>
                                <multiplier occurrences="4" amount="400"/>
                                <multiplier occurrences="5" amount="1000"/>
                            </card>
                            <card face="7">
                                <multiplier occurrences="3" amount="5"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="500"/>
                            </card>
                        </multipliers>
                    </properties>""";
        }

        private void setDuplicateOccurrenceInTable() {
            invalidXmlContent = """
                    <?xml version="1.0" encoding="UTF-8" ?>

                    <properties>
                        <rows>3</rows>
                        <columns>5</columns>
                        <currency format="round"/>
                        <balance>100000</balance>
                        <betlimit>10</betlimit>
                        <exit>quit</exit>

                        <reelArrays>
                            <reelArray>6,6,6,1,1,1,0,0,0,3,3,3,4,4,4,2,2,2,5,5,5,1,1,1,7,4,4,4,2,2</reelArray>
                            <reelArray>6,6,6,2,2,2,1,1,1,0,0,0,5,5,5,1,1,1,7,3,3,3,2,2,2,0,0,0,5,5</reelArray>
                            <reelArray>6,6,6,4,4,4,0,0,0,1,1,1,5,5,5,2,2,2,7,3,3,3,0,0,0,2,2,2,5,5</reelArray>
                            <reelArray>6,6,6,2,2,2,4,4,4,0,0,0,5,5,5,3,3,3,1,1,1,7,2,2,2,0,0,0,4,4</reelArray>
                            <reelArray>6,6,6,1,1,1,4,4,4,2,2,2,5,5,5,0,0,0,7,1,1,1,3,3,3,2,2,2,5,5</reelArray>
                        </reelArrays>

                        <lineArrays>
                            <lineArray>1,1,1,1,1</lineArray>
                            <lineArray>0,0,0,0,0</lineArray>
                            <lineArray>2,2,2,2,2</lineArray>
                            <lineArray>0,1,2,1,0</lineArray>
                            <lineArray>2,1,0,1,2</lineArray>
                            <lineArray>0,0,1,2,2</lineArray>
                            <lineArray>2,2,1,0,0</lineArray>
                            <lineArray>1,2,2,2,1</lineArray>
                            <lineArray>1,0,0,0,1</lineArray>
                            <lineArray>0,1,1,1,0</lineArray>
                            <lineArray>2,1,1,1,2</lineArray>
                            <lineArray>1,2,1,0,1</lineArray>
                            <lineArray>1,0,1,2,1</lineArray>
                            <lineArray>0,1,0,1,0</lineArray>
                            <lineArray>2,1,2,1,2</lineArray>
                            <lineArray>1,1,2,1,1</lineArray>
                            <lineArray>1,1,0,1,1</lineArray>
                            <lineArray>0,2,0,2,0</lineArray>
                            <lineArray>2,0,2,0,2</lineArray>
                            <lineArray>1,0,2,0,1</lineArray>
                        </lineArrays>

                        <scatters>7</scatters>
                        
                        <wildcard>6</wildcard>

                        <multipliers>
                            <card face="0">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="1">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="2">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="3">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="40"/>
                                <multiplier occurrences="5" amount="200"/>
                            </card>
                            <card face="4">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="40"/>
                                <multiplier occurrences="5" amount="200"/>
                            </card>
                            <card face="5">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="80"/>
                                <multiplier occurrences="5" amount="400"/>
                            </card>
                            <card face="6">
                                <multiplier occurrences="3" amount="40"/>
                                <multiplier occurrences="4" amount="400"/>
                                <multiplier occurrences="5" amount="1000"/>
                            </card>
                            <card face="7">
                                <multiplier occurrences="3" amount="5"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="500"/>
                            </card>
                        </multipliers>
                    </properties>""";
        }

        private void setVaryingCardMultipliers() {
            invalidXmlContent = """
                    <?xml version="1.0" encoding="UTF-8" ?>

                    <properties>
                        <rows>3</rows>
                        <columns>5</columns>
                        <currency format="round"/>
                        <balance>100000</balance>
                        <betlimit>10</betlimit>
                        <exit>quit</exit>

                        <reelArrays>
                            <reelArray>6,6,6,1,1,1,0,0,0,3,3,3,4,4,4,2,2,2,5,5,5,1,1,1,7,4,4,4,2,2</reelArray>
                            <reelArray>6,6,6,2,2,2,1,1,1,0,0,0,5,5,5,1,1,1,7,3,3,3,2,2,2,0,0,0,5,5</reelArray>
                            <reelArray>6,6,6,4,4,4,0,0,0,1,1,1,5,5,5,2,2,2,7,3,3,3,0,0,0,2,2,2,5,5</reelArray>
                            <reelArray>6,6,6,2,2,2,4,4,4,0,0,0,5,5,5,3,3,3,1,1,1,7,2,2,2,0,0,0,4,4</reelArray>
                            <reelArray>6,6,6,1,1,1,4,4,4,2,2,2,5,5,5,0,0,0,7,1,1,1,3,3,3,2,2,2,5,5</reelArray>
                        </reelArrays>

                        <lineArrays>
                            <lineArray>1,1,1,1,1</lineArray>
                            <lineArray>0,0,0,0,0</lineArray>
                            <lineArray>2,2,2,2,2</lineArray>
                            <lineArray>0,1,2,1,0</lineArray>
                            <lineArray>2,1,0,1,2</lineArray>
                            <lineArray>0,0,1,2,2</lineArray>
                            <lineArray>2,2,1,0,0</lineArray>
                            <lineArray>1,2,2,2,1</lineArray>
                            <lineArray>1,0,0,0,1</lineArray>
                            <lineArray>0,1,1,1,0</lineArray>
                            <lineArray>2,1,1,1,2</lineArray>
                            <lineArray>1,2,1,0,1</lineArray>
                            <lineArray>1,0,1,2,1</lineArray>
                            <lineArray>0,1,0,1,0</lineArray>
                            <lineArray>2,1,2,1,2</lineArray>
                            <lineArray>1,1,2,1,1</lineArray>
                            <lineArray>1,1,0,1,1</lineArray>
                            <lineArray>0,2,0,2,0</lineArray>
                            <lineArray>2,0,2,0,2</lineArray>
                            <lineArray>1,0,2,0,1</lineArray>
                        </lineArrays>

                        <scatters>7</scatters>
                        
                        <wildcard>6</wildcard>

                        <multipliers>
                            <card face="0">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="1">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                                <multiplier occurrences="6" amount="200"/>
                            </card>
                            <card face="2">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="3">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="40"/>
                                <multiplier occurrences="5" amount="200"/>
                            </card>
                            <card face="4">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="40"/>
                                <multiplier occurrences="5" amount="200"/>
                            </card>
                            <card face="5">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="80"/>
                                <multiplier occurrences="5" amount="400"/>
                            </card>
                            <card face="6">
                                <multiplier occurrences="3" amount="40"/>
                                <multiplier occurrences="4" amount="400"/>
                                <multiplier occurrences="5" amount="1000"/>
                            </card>
                            <card face="7">
                                <multiplier occurrences="3" amount="5"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="500"/>
                            </card>
                        </multipliers>
                    </properties>""";
        }

        private void setVaryingCardOccurrences() {
            invalidXmlContent = """
                    <?xml version="1.0" encoding="UTF-8" ?>

                    <properties>
                        <rows>3</rows>
                        <columns>5</columns>
                        <currency format="round"/>
                        <balance>100000</balance>
                        <betlimit>10</betlimit>
                        <exit>quit</exit>

                        <reelArrays>
                            <reelArray>6,6,6,1,1,1,0,0,0,3,3,3,4,4,4,2,2,2,5,5,5,1,1,1,7,4,4,4,2,2</reelArray>
                            <reelArray>6,6,6,2,2,2,1,1,1,0,0,0,5,5,5,1,1,1,7,3,3,3,2,2,2,0,0,0,5,5</reelArray>
                            <reelArray>6,6,6,4,4,4,0,0,0,1,1,1,5,5,5,2,2,2,7,3,3,3,0,0,0,2,2,2,5,5</reelArray>
                            <reelArray>6,6,6,2,2,2,4,4,4,0,0,0,5,5,5,3,3,3,1,1,1,7,2,2,2,0,0,0,4,4</reelArray>
                            <reelArray>6,6,6,1,1,1,4,4,4,2,2,2,5,5,5,0,0,0,7,1,1,1,3,3,3,2,2,2,5,5</reelArray>
                        </reelArrays>

                        <lineArrays>
                            <lineArray>1,1,1,1,1</lineArray>
                            <lineArray>0,0,0,0,0</lineArray>
                            <lineArray>2,2,2,2,2</lineArray>
                            <lineArray>0,1,2,1,0</lineArray>
                            <lineArray>2,1,0,1,2</lineArray>
                            <lineArray>0,0,1,2,2</lineArray>
                            <lineArray>2,2,1,0,0</lineArray>
                            <lineArray>1,2,2,2,1</lineArray>
                            <lineArray>1,0,0,0,1</lineArray>
                            <lineArray>0,1,1,1,0</lineArray>
                            <lineArray>2,1,1,1,2</lineArray>
                            <lineArray>1,2,1,0,1</lineArray>
                            <lineArray>1,0,1,2,1</lineArray>
                            <lineArray>0,1,0,1,0</lineArray>
                            <lineArray>2,1,2,1,2</lineArray>
                            <lineArray>1,1,2,1,1</lineArray>
                            <lineArray>1,1,0,1,1</lineArray>
                            <lineArray>0,2,0,2,0</lineArray>
                            <lineArray>2,0,2,0,2</lineArray>
                            <lineArray>1,0,2,0,1</lineArray>
                        </lineArrays>

                        <scatters>7</scatters>
                        
                        <wildcard>6</wildcard>

                        <multipliers>
                            <card face="0">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="1">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="6" amount="100"/>
                            </card>
                            <card face="2">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="3">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="40"/>
                                <multiplier occurrences="5" amount="200"/>
                            </card>
                            <card face="4">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="40"/>
                                <multiplier occurrences="5" amount="200"/>
                            </card>
                            <card face="5">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="80"/>
                                <multiplier occurrences="5" amount="400"/>
                            </card>
                            <card face="6">
                                <multiplier occurrences="3" amount="40"/>
                                <multiplier occurrences="4" amount="400"/>
                                <multiplier occurrences="5" amount="1000"/>
                            </card>
                            <card face="7">
                                <multiplier occurrences="3" amount="5"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="500"/>
                            </card>
                        </multipliers>
                    </properties>""";
        }

        private void setMissingCardInTable() {
            invalidXmlContent = """
                    <?xml version="1.0" encoding="UTF-8" ?>

                    <properties>
                        <rows>3</rows>
                        <columns>5</columns>
                        <currency format="round"/>
                        <balance>100000</balance>
                        <betlimit>10</betlimit>
                        <exit>quit</exit>

                        <reelArrays>
                            <reelArray>6,6,6,1,1,1,0,0,0,3,3,3,4,4,4,2,2,2,5,5,5,1,1,1,7,4,4,4,2,2</reelArray>
                            <reelArray>6,6,6,2,2,2,1,1,1,0,0,0,5,5,5,1,1,1,7,3,3,3,2,2,2,0,0,0,5,5</reelArray>
                            <reelArray>6,6,6,4,4,4,0,0,0,1,1,1,5,5,5,2,2,2,7,3,3,3,0,0,0,2,2,2,5,5</reelArray>
                            <reelArray>6,6,6,2,2,2,4,4,4,0,0,0,5,5,5,3,3,3,1,1,1,7,2,2,2,0,0,0,4,4</reelArray>
                            <reelArray>6,6,6,1,1,1,4,4,4,2,2,2,5,5,5,0,0,0,7,1,1,1,3,3,3,2,2,2,5,5</reelArray>
                        </reelArrays>

                        <lineArrays>
                            <lineArray>1,1,1,1,1</lineArray>
                            <lineArray>0,0,0,0,0</lineArray>
                            <lineArray>2,2,2,2,2</lineArray>
                            <lineArray>0,1,2,1,0</lineArray>
                            <lineArray>2,1,0,1,2</lineArray>
                            <lineArray>0,0,1,2,2</lineArray>
                            <lineArray>2,2,1,0,0</lineArray>
                            <lineArray>1,2,2,2,1</lineArray>
                            <lineArray>1,0,0,0,1</lineArray>
                            <lineArray>0,1,1,1,0</lineArray>
                            <lineArray>2,1,1,1,2</lineArray>
                            <lineArray>1,2,1,0,1</lineArray>
                            <lineArray>1,0,1,2,1</lineArray>
                            <lineArray>0,1,0,1,0</lineArray>
                            <lineArray>2,1,2,1,2</lineArray>
                            <lineArray>1,1,2,1,1</lineArray>
                            <lineArray>1,1,0,1,1</lineArray>
                            <lineArray>0,2,0,2,0</lineArray>
                            <lineArray>2,0,2,0,2</lineArray>
                            <lineArray>1,0,2,0,1</lineArray>
                        </lineArrays>

                        <scatters>7</scatters>
                        
                        <wildcard>6</wildcard>

                        <multipliers>
                            <card face="0">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="1">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="2">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="3">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="40"/>
                                <multiplier occurrences="5" amount="200"/>
                            </card>
                            <card face="4">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="40"/>
                                <multiplier occurrences="5" amount="200"/>
                            </card>
                            <card face="5">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="80"/>
                                <multiplier occurrences="5" amount="400"/>
                            </card>
                            <card face="6">
                                <multiplier occurrences="3" amount="40"/>
                                <multiplier occurrences="4" amount="400"/>
                                <multiplier occurrences="5" amount="1000"/>
                            </card>
                        </multipliers>
                    </properties>""";
        }

        private void setScatterNotInTable() {
            invalidXmlContent = """
                    <?xml version="1.0" encoding="UTF-8" ?>

                    <properties>
                        <rows>3</rows>
                        <columns>5</columns>
                        <currency format="round"/>
                        <balance>100000</balance>
                        <betlimit>10</betlimit>
                        <exit>quit</exit>

                        <reelArrays>
                            <reelArray>6,6,6,1,1,1,0,0,0,3,3,3,4,4,4,2,2,2,5,5,5,1,1,1,7,4,4,4,2,2</reelArray>
                            <reelArray>6,6,6,2,2,2,1,1,1,0,0,0,5,5,5,1,1,1,7,3,3,3,2,2,2,0,0,0,5,5</reelArray>
                            <reelArray>6,6,6,4,4,4,0,0,0,1,1,1,5,5,5,2,2,2,7,3,3,3,0,0,0,2,2,2,5,5</reelArray>
                            <reelArray>6,6,6,2,2,2,4,4,4,0,0,0,5,5,5,3,3,3,1,1,1,7,2,2,2,0,0,0,4,4</reelArray>
                            <reelArray>6,6,6,1,1,1,4,4,4,2,2,2,5,5,5,0,0,0,7,1,1,1,3,3,3,2,2,2,5,5</reelArray>
                        </reelArrays>

                        <lineArrays>
                            <lineArray>1,1,1,1,1</lineArray>
                            <lineArray>0,0,0,0,0</lineArray>
                            <lineArray>2,2,2,2,2</lineArray>
                            <lineArray>0,1,2,1,0</lineArray>
                            <lineArray>2,1,0,1,2</lineArray>
                            <lineArray>0,0,1,2,2</lineArray>
                            <lineArray>2,2,1,0,0</lineArray>
                            <lineArray>1,2,2,2,1</lineArray>
                            <lineArray>1,0,0,0,1</lineArray>
                            <lineArray>0,1,1,1,0</lineArray>
                            <lineArray>2,1,1,1,2</lineArray>
                            <lineArray>1,2,1,0,1</lineArray>
                            <lineArray>1,0,1,2,1</lineArray>
                            <lineArray>0,1,0,1,0</lineArray>
                            <lineArray>2,1,2,1,2</lineArray>
                            <lineArray>1,1,2,1,1</lineArray>
                            <lineArray>1,1,0,1,1</lineArray>
                            <lineArray>0,2,0,2,0</lineArray>
                            <lineArray>2,0,2,0,2</lineArray>
                            <lineArray>1,0,2,0,1</lineArray>
                        </lineArrays>

                        <scatters>8</scatters>

                        <wildcard>6</wildcard>

                        <multipliers>
                            <card face="0">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="1">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="2">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="3">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="40"/>
                                <multiplier occurrences="5" amount="200"/>
                            </card>
                            <card face="4">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="40"/>
                                <multiplier occurrences="5" amount="200"/>
                            </card>
                            <card face="5">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="80"/>
                                <multiplier occurrences="5" amount="400"/>
                            </card>
                            <card face="6">
                                <multiplier occurrences="3" amount="40"/>
                                <multiplier occurrences="4" amount="400"/>
                                <multiplier occurrences="5" amount="1000"/>
                            </card>
                            <card face="7">
                                <multiplier occurrences="3" amount="5"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="500"/>
                            </card>
                        </multipliers>
                    </properties>""";
        }

        private void setWildcardNotInTable() {
            invalidXmlContent = """
                    <?xml version="1.0" encoding="UTF-8" ?>

                    <properties>
                        <rows>3</rows>
                        <columns>5</columns>
                        <currency format="round"/>
                        <balance>100000</balance>
                        <betlimit>10</betlimit>
                        <exit>quit</exit>

                        <reelArrays>
                            <reelArray>6,6,6,1,1,1,0,0,0,3,3,3,4,4,4,2,2,2,5,5,5,1,1,1,7,4,4,4,2,2</reelArray>
                            <reelArray>6,6,6,2,2,2,1,1,1,0,0,0,5,5,5,1,1,1,7,3,3,3,2,2,2,0,0,0,5,5</reelArray>
                            <reelArray>6,6,6,4,4,4,0,0,0,1,1,1,5,5,5,2,2,2,7,3,3,3,0,0,0,2,2,2,5,5</reelArray>
                            <reelArray>6,6,6,2,2,2,4,4,4,0,0,0,5,5,5,3,3,3,1,1,1,7,2,2,2,0,0,0,4,4</reelArray>
                            <reelArray>6,6,6,1,1,1,4,4,4,2,2,2,5,5,5,0,0,0,7,1,1,1,3,3,3,2,2,2,5,5</reelArray>
                        </reelArrays>

                        <lineArrays>
                            <lineArray>1,1,1,1,1</lineArray>
                            <lineArray>0,0,0,0,0</lineArray>
                            <lineArray>2,2,2,2,2</lineArray>
                            <lineArray>0,1,2,1,0</lineArray>
                            <lineArray>2,1,0,1,2</lineArray>
                            <lineArray>0,0,1,2,2</lineArray>
                            <lineArray>2,2,1,0,0</lineArray>
                            <lineArray>1,2,2,2,1</lineArray>
                            <lineArray>1,0,0,0,1</lineArray>
                            <lineArray>0,1,1,1,0</lineArray>
                            <lineArray>2,1,1,1,2</lineArray>
                            <lineArray>1,2,1,0,1</lineArray>
                            <lineArray>1,0,1,2,1</lineArray>
                            <lineArray>0,1,0,1,0</lineArray>
                            <lineArray>2,1,2,1,2</lineArray>
                            <lineArray>1,1,2,1,1</lineArray>
                            <lineArray>1,1,0,1,1</lineArray>
                            <lineArray>0,2,0,2,0</lineArray>
                            <lineArray>2,0,2,0,2</lineArray>
                            <lineArray>1,0,2,0,1</lineArray>
                        </lineArrays>

                        <scatters>7</scatters>

                        <wildcard>8</wildcard>

                        <multipliers>
                            <card face="0">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="1">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="2">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="3">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="40"/>
                                <multiplier occurrences="5" amount="200"/>
                            </card>
                            <card face="4">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="40"/>
                                <multiplier occurrences="5" amount="200"/>
                            </card>
                            <card face="5">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="80"/>
                                <multiplier occurrences="5" amount="400"/>
                            </card>
                            <card face="6">
                                <multiplier occurrences="3" amount="40"/>
                                <multiplier occurrences="4" amount="400"/>
                                <multiplier occurrences="5" amount="1000"/>
                            </card>
                            <card face="7">
                                <multiplier occurrences="3" amount="5"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="500"/>
                            </card>
                        </multipliers>
                    </properties>""";
        }
    }

}
