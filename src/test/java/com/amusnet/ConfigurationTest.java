package com.amusnet;

import com.amusnet.config.GameConfig;
import com.amusnet.exception.ConfigurationInitializationException;
import com.amusnet.util.ErrorMessages;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
public class ConfigurationTest {

    private static GameConfig<String> config1, config2;

    @BeforeAll
    public static void setup() {
        File xmlConfig = new File("src/main/resources/letter-properties.xml");
        File xsdValidation = new File("src/main/resources/properties.xsd");

        try {
            config1 = new GameConfig<>(xmlConfig, xsdValidation);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            log.error("Error initializing configuration", e);
            throw new RuntimeException(e);
        } catch (ConfigurationInitializationException e) {
            log.error("Configuration constraint violated", e);
            throw new RuntimeException(e);
        }
        config2 = new GameConfig<>();
        configManualSetup(config2);
    }

    @Test
    public void givenOneConfigurationFromXmlAndOneManualWhichAreEqual_theyAreTrulyEqual() {
        Assertions.assertThat(config1)
                .usingRecursiveComparison()
                .ignoringFields("table")    // TODO No
                .isEqualTo(config2);
    }

    private static void configManualSetup(GameConfig<String> configuration) {

        configuration.setScreenRowCount(3);
        configuration.setScreenColumnCount(5);
        configuration.setLineCount(20);
        configuration.setExitCommand("quit");

        // set up reel arrays
        {
            configuration.setReels(List.of(
                    List.of("G","G","G","B","B","B","A","A","A","D","D","D","E","E","E","C","C","C","F","F","F","B","B","B","H","E","E","E","C","C"),
                    List.of("G","G","G","C","C","C","B","B","B","A","A","A","F","F","F","B","B","B","H","D","D","D","C","C","C","A","A","A","F","F"),
                    List.of("G","G","G","E","E","E","A","A","A","B","B","B","F","F","F","C","C","C","H","D","D","D","A","A","A","C","C","C","F","F"),
                    List.of("G","G","G","C","C","C","E","E","E","A","A","A","F","F","F","D","D","D","B","B","B","H","C","C","C","A","A","A","E","E"),
                    List.of("G","G","G","B","B","B","E","E","E","C","C","C","F","F","F","A","A","A","H","B","B","B","D","D","D","C","C","C","F","F")
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

            Map<String, Map<Integer, Integer>> tableData = Map.of(
                    "A", Map.of(  3, 10,
                            4, 20,
                            5, 100),
                    "B", Map.of(  3, 10,
                            4, 20,
                            5, 100),
                    "C", Map.of(  3, 10,
                            4, 20,
                            5, 100),
                    "D", Map.of(  3, 20,
                            4, 40,
                            5, 200),
                    "E", Map.of(  3, 20,
                            4, 40,
                            5, 200),
                    "F", Map.of(  3, 20,
                            4, 80,
                            5, 400),
                    "G", Map.of(  3, 40,
                            4, 400,
                            5, 1000),
                    "H", Map.of(  3, 5,
                            4, 20,
                            5, 500)
            );

            configuration.setupTable(occurrenceCounts, tableData);
        }

        // set scatters
        configuration.setScatters(Set.of("H"));

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

        private static final File tempConfigFile = new File("target/generated-test-sources/letter-properties.xml");

        private static String invalidXmlContent;

        @BeforeAll
        static void createTempFileAndInitializeWriter() {
            try {
                if (!tempConfigFile.createNewFile())
                    log.warn("File {} already exists", tempConfigFile.getPath());
            } catch (IOException e) {
                log.error("Failed to crate test configuration file");
                throw new RuntimeException(e);
            }
        }

        @AfterAll
        static void deleteTestConfigFile() {
            if (!tempConfigFile.delete())
                log.error("Failed to delete generated file");
        }

        @Test
        public void configurationXmlHasInvalidColumnSize_shouldThrowConfigInitException() {
            setInvalidColumnSize();
            Exception e = getException();
            assertEquals(e.getMessage(), errorMessages.message
                    (ErrorMessages.DefaultMessageTitles.TITLE_EMSG_REELS_DISCREPANCY));
        }

        @Test
        public void configurationXmlHasInvalidReelArraysSize_shouldThrowConfigInitException() {
            setInvalidReelArraysSize();
            Exception e = getException();
            assertEquals(e.getMessage(), errorMessages.message
                    (ErrorMessages.DefaultMessageTitles.TITLE_EMSG_REELS_DISCREPANCY));
        }

        @Test
        public void configurationXmlHasDuplicateCardInTable_shouldThrowConfigInitException() {
            setDuplicateCardInTable();
            Exception e = getException();
            assertEquals(e.getMessage(), errorMessages.message
                    (ErrorMessages.DefaultMessageTitles.TITLE_EMSG_TABLE_DUPLICATE_CARDS));
        }

        @Test
        public void configurationXmlHasDuplicatedOccurrence_shouldThrowConfigInitException() {
            setDuplicateOccurrenceInTable();
            Exception e = getException();
            assertEquals(e.getMessage(), errorMessages.message
                    (ErrorMessages.DefaultMessageTitles.TITLE_EMSG_TABLE_DUPLICATE_OCCURRENCE));
        }

        @Test
        public void configurationXmlHasVaryingCardMultipliers_shouldThrowConfigInitException() {
            setVaryingCardMultipliers();
            Exception e = getException();
            assertEquals(e.getMessage(), errorMessages.message
                    (ErrorMessages.DefaultMessageTitles.TITLE_EMSG_TABLE_MULTIPLIERS_DISCREPANCY));
        }

        @Test
        public void configurationXmlHasVaryingCardOccurrences_shouldThrowConfigInitException() {
            setVaryingCardOccurrences();
            Exception e = getException();
            assertEquals(e.getMessage(), errorMessages.message
                    (ErrorMessages.DefaultMessageTitles.TITLE_EMSG_TABLE_OCCURRENCES_DISCREPANCY));
        }

        @Test
        public void configurationXmlHasMissingCardInTable_shouldThrowConfigInitException() {
            setMissingCardInTable();
            Exception e = getException();
            assertEquals(e.getMessage(), errorMessages.message
                    (ErrorMessages.DefaultMessageTitles.TITLE_EMSG_TABLE_MISSING_CARDS));
        }

        private Exception getException() {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempConfigFile, false))){
                writer.write(invalidXmlContent);
            } catch (IOException e) {
                log.error("Failed to initialize file reader");
                throw new RuntimeException(e);
            }
            return assertThrows(ConfigurationInitializationException.class, () ->
                    new GameConfig<String>(tempConfigFile));
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
                            <reelArray>G,G,G,B,B,B,A,A,A,D,D,D,E,E,E,C,C,C,F,F,F,B,B,B,H,E,E,E,C,C</reelArray>
                            <reelArray>G,G,G,C,C,C,B,B,B,A,A,A,F,F,F,B,B,B,H,D,D,D,C,C,C,A,A,A,F,F</reelArray>
                            <reelArray>G,G,G,E,E,E,A,A,A,B,B,B,F,F,F,C,C,C,H,D,D,D,A,A,A,C,C,C,F,F</reelArray>
                            <reelArray>G,G,G,C,C,C,E,E,E,A,A,A,F,F,F,D,D,D,B,B,B,H,C,C,C,A,A,A,E,E</reelArray>
                            <reelArray>G,G,G,B,B,B,E,E,E,C,C,C,F,F,F,A,A,A,H,B,B,B,D,D,D,C,C,C,F,F</reelArray>
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

                        <scatters>H</scatters>

                        <multipliers>
                            <card face="A">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="B">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="C">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="D">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="40"/>
                                <multiplier occurrences="5" amount="200"/>
                            </card>
                            <card face="E">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="40"/>
                                <multiplier occurrences="5" amount="200"/>
                            </card>
                            <card face="F">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="80"/>
                                <multiplier occurrences="5" amount="400"/>
                            </card>
                            <card face="G">
                                <multiplier occurrences="3" amount="40"/>
                                <multiplier occurrences="4" amount="400"/>
                                <multiplier occurrences="5" amount="1000"/>
                            </card>
                            <card face="H">
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
                            <reelArray>G,G,G,B,B,B,A,A,A,D,D,D,E,E,E,C,C,C,F,F,F,B,B,B,H,E,E,E,C,C</reelArray>
                            <reelArray>G,G,G,C,C,C,B,B,B,A,A,A,F,F,F,B,B,B,H,D,D,D,C,C,C,A,A,A,F,F</reelArray>
                            <reelArray>G,G,G,E,E,E,A,A,A,B,B,B,F,F,F,C,C,C,H,D,D,D,A,A,A,C,C,C,F,F</reelArray>
                            <reelArray>G,G,G,C,C,C,E,E,E,A,A,A,F,F,F,D,D,D,B,B,B,H,C,C,C,A,A,A,E,E</reelArray>
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

                        <scatters>H</scatters>

                        <multipliers>
                            <card face="A">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="B">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="C">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="D">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="40"/>
                                <multiplier occurrences="5" amount="200"/>
                            </card>
                            <card face="E">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="40"/>
                                <multiplier occurrences="5" amount="200"/>
                            </card>
                            <card face="F">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="80"/>
                                <multiplier occurrences="5" amount="400"/>
                            </card>
                            <card face="G">
                                <multiplier occurrences="3" amount="40"/>
                                <multiplier occurrences="4" amount="400"/>
                                <multiplier occurrences="5" amount="1000"/>
                            </card>
                            <card face="H">
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
                            <reelArray>G,G,G,B,B,B,A,A,A,D,D,D,E,E,E,C,C,C,F,F,F,B,B,B,H,E,E,E,C,C</reelArray>
                            <reelArray>G,G,G,C,C,C,B,B,B,A,A,A,F,F,F,B,B,B,H,D,D,D,C,C,C,A,A,A,F,F</reelArray>
                            <reelArray>G,G,G,E,E,E,A,A,A,B,B,B,F,F,F,C,C,C,H,D,D,D,A,A,A,C,C,C,F,F</reelArray>
                            <reelArray>G,G,G,C,C,C,E,E,E,A,A,A,F,F,F,D,D,D,B,B,B,H,C,C,C,A,A,A,E,E</reelArray>
                            <reelArray>G,G,G,B,B,B,E,E,E,C,C,C,F,F,F,A,A,A,H,B,B,B,D,D,D,C,C,C,F,F</reelArray>
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

                        <scatters>H</scatters>

                        <multipliers>
                            <card face="A">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="A">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="C">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="D">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="40"/>
                                <multiplier occurrences="5" amount="200"/>
                            </card>
                            <card face="E">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="40"/>
                                <multiplier occurrences="5" amount="200"/>
                            </card>
                            <card face="F">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="80"/>
                                <multiplier occurrences="5" amount="400"/>
                            </card>
                            <card face="G">
                                <multiplier occurrences="3" amount="40"/>
                                <multiplier occurrences="4" amount="400"/>
                                <multiplier occurrences="5" amount="1000"/>
                            </card>
                            <card face="H">
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
                            <reelArray>G,G,G,B,B,B,A,A,A,D,D,D,E,E,E,C,C,C,F,F,F,B,B,B,H,E,E,E,C,C</reelArray>
                            <reelArray>G,G,G,C,C,C,B,B,B,A,A,A,F,F,F,B,B,B,H,D,D,D,C,C,C,A,A,A,F,F</reelArray>
                            <reelArray>G,G,G,E,E,E,A,A,A,B,B,B,F,F,F,C,C,C,H,D,D,D,A,A,A,C,C,C,F,F</reelArray>
                            <reelArray>G,G,G,C,C,C,E,E,E,A,A,A,F,F,F,D,D,D,B,B,B,H,C,C,C,A,A,A,E,E</reelArray>
                            <reelArray>G,G,G,B,B,B,E,E,E,C,C,C,F,F,F,A,A,A,H,B,B,B,D,D,D,C,C,C,F,F</reelArray>
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

                        <scatters>H</scatters>

                        <multipliers>
                            <card face="A">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="B">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="C">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="D">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="40"/>
                                <multiplier occurrences="5" amount="200"/>
                            </card>
                            <card face="E">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="40"/>
                                <multiplier occurrences="5" amount="200"/>
                            </card>
                            <card face="F">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="80"/>
                                <multiplier occurrences="5" amount="400"/>
                            </card>
                            <card face="G">
                                <multiplier occurrences="3" amount="40"/>
                                <multiplier occurrences="4" amount="400"/>
                                <multiplier occurrences="5" amount="1000"/>
                            </card>
                            <card face="H">
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
                            <reelArray>G,G,G,B,B,B,A,A,A,D,D,D,E,E,E,C,C,C,F,F,F,B,B,B,H,E,E,E,C,C</reelArray>
                            <reelArray>G,G,G,C,C,C,B,B,B,A,A,A,F,F,F,B,B,B,H,D,D,D,C,C,C,A,A,A,F,F</reelArray>
                            <reelArray>G,G,G,E,E,E,A,A,A,B,B,B,F,F,F,C,C,C,H,D,D,D,A,A,A,C,C,C,F,F</reelArray>
                            <reelArray>G,G,G,C,C,C,E,E,E,A,A,A,F,F,F,D,D,D,B,B,B,H,C,C,C,A,A,A,E,E</reelArray>
                            <reelArray>G,G,G,B,B,B,E,E,E,C,C,C,F,F,F,A,A,A,H,B,B,B,D,D,D,C,C,C,F,F</reelArray>
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

                        <scatters>H</scatters>

                        <multipliers>
                            <card face="A">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="B">
                                <multiplier occurrences="2" amount="10"/>
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="C">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="D">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="40"/>
                                <multiplier occurrences="5" amount="200"/>
                            </card>
                            <card face="E">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="40"/>
                                <multiplier occurrences="5" amount="200"/>
                            </card>
                            <card face="F">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="80"/>
                                <multiplier occurrences="5" amount="400"/>
                            </card>
                            <card face="G">
                                <multiplier occurrences="3" amount="40"/>
                                <multiplier occurrences="4" amount="400"/>
                                <multiplier occurrences="5" amount="1000"/>
                            </card>
                            <card face="H">
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
                            <reelArray>G,G,G,B,B,B,A,A,A,D,D,D,E,E,E,C,C,C,F,F,F,B,B,B,H,E,E,E,C,C</reelArray>
                            <reelArray>G,G,G,C,C,C,B,B,B,A,A,A,F,F,F,B,B,B,H,D,D,D,C,C,C,A,A,A,F,F</reelArray>
                            <reelArray>G,G,G,E,E,E,A,A,A,B,B,B,F,F,F,C,C,C,H,D,D,D,A,A,A,C,C,C,F,F</reelArray>
                            <reelArray>G,G,G,C,C,C,E,E,E,A,A,A,F,F,F,D,D,D,B,B,B,H,C,C,C,A,A,A,E,E</reelArray>
                            <reelArray>G,G,G,B,B,B,E,E,E,C,C,C,F,F,F,A,A,A,H,B,B,B,D,D,D,C,C,C,F,F</reelArray>
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

                        <scatters>H</scatters>

                        <multipliers>
                            <card face="A">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="B">
                                <multiplier occurrences="6" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="C">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="D">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="40"/>
                                <multiplier occurrences="5" amount="200"/>
                            </card>
                            <card face="E">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="40"/>
                                <multiplier occurrences="5" amount="200"/>
                            </card>
                            <card face="F">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="80"/>
                                <multiplier occurrences="5" amount="400"/>
                            </card>
                            <card face="G">
                                <multiplier occurrences="3" amount="40"/>
                                <multiplier occurrences="4" amount="400"/>
                                <multiplier occurrences="5" amount="1000"/>
                            </card>
                            <card face="H">
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
                            <reelArray>G,G,G,B,B,B,A,A,A,D,D,D,E,E,E,C,C,C,F,F,F,B,B,B,H,E,E,E,C,C</reelArray>
                            <reelArray>G,G,G,C,C,C,B,B,B,A,A,A,F,F,F,B,B,B,H,D,D,D,C,C,C,A,A,A,F,F</reelArray>
                            <reelArray>G,G,G,E,E,E,A,A,A,B,B,B,F,F,F,C,C,C,H,D,D,D,A,A,A,C,C,C,F,F</reelArray>
                            <reelArray>G,G,G,C,C,C,E,E,E,A,A,A,F,F,F,D,D,D,B,B,B,H,C,C,C,A,A,A,E,E</reelArray>
                            <reelArray>G,G,G,B,B,B,E,E,E,C,C,C,F,F,F,A,A,A,H,B,B,B,D,D,D,C,C,C,F,F</reelArray>
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

                        <scatters>H</scatters>

                        <multipliers>
                            <card face="A">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="B">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="C">
                                <multiplier occurrences="3" amount="10"/>
                                <multiplier occurrences="4" amount="20"/>
                                <multiplier occurrences="5" amount="100"/>
                            </card>
                            <card face="D">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="40"/>
                                <multiplier occurrences="5" amount="200"/>
                            </card>
                            <card face="E">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="40"/>
                                <multiplier occurrences="5" amount="200"/>
                            </card>
                            <card face="F">
                                <multiplier occurrences="3" amount="20"/>
                                <multiplier occurrences="4" amount="80"/>
                                <multiplier occurrences="5" amount="400"/>
                            </card>
                            <card face="G">
                                <multiplier occurrences="3" amount="40"/>
                                <multiplier occurrences="4" amount="400"/>
                                <multiplier occurrences="5" amount="1000"/>
                            </card>
                        </multipliers>
                    </properties>""";
        }
    }

}
