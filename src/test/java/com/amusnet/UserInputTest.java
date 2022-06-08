package com.amusnet;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class UserInputTest {

    private static final int REQUIRED_INPUT_COUNT = 2;
    private static final int RANDOM_STRING_MAX_LENGTH = 30;
    private static final String INVALID_INPUT_SEED = "H1e2l3l4o5W6o7r8l9d";
    private static final String EXIT_STRING = "quit";

    private static final Properties PROPERTIES = new Properties();

    private static final InputStream OLD_STANDARD_IN = System.in;
    private static final PrintStream OLD_STANDARD_OUT = System.out;
    private static final PrintStream OLD_STANDARD_ERR = System.err;

    private static InputStream newStandardIn;

    private static OutputStream newOutput;
    private static PrintStream newStandardOut;

    private static OutputStream newError;
    private static PrintStream newStandardErr;

    @Nested
    @DisplayName("Test invalid user input")
    class InvalidInputTests {

        @Test
        void userGivesInvalidInputForLinesPlayed_CheckForCorrectErrorStreamContents() {
            // get input string with incorrect data for lines played
            String input = getInputString(new boolean[]{false, true});
            log.info("Generated input string:\n'{}'", input);
            rewireStandardInputAndOutput(input);
            Application.main(null);
            assertThat(newError.toString()).isEqualTo("Invalid input for number of lines played!" + System.lineSeparator());
        }

        private static String generateValidInputString(String field) throws IllegalArgumentException {
            Random rnd = new Random();
            // TODO dynamic field strings
            switch (field) {
                case "linesPlayed" -> {
                    int maxLines = Integer.parseInt(PROPERTIES.getProperty("max_lines"));
                    return String.valueOf(rnd.nextInt(maxLines) + 1);
                }
                case "betAmount" -> {
                    int betLimit = Integer.parseInt(PROPERTIES.getProperty("bet_limit"));
                    return String.valueOf(rnd.nextInt(betLimit) + 1);
                }
                default -> throw new IllegalArgumentException
                        (String.format("No such field '%s' accepts input", field));
            }
        }

        private static String generateInvalidInputString(String seed) {
            Random rnd = new Random();
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < RANDOM_STRING_MAX_LENGTH; i++)
                sb.append(seed.charAt(rnd.nextInt(seed.length())));

            return sb.toString();
        }

        private static String getInputString(boolean[] inputValidityMask) throws IllegalArgumentException {
            if (inputValidityMask.length != REQUIRED_INPUT_COUNT)
                throw new IllegalArgumentException
                        (String.format("%d inputs required, mask of size %d illegal",
                        REQUIRED_INPUT_COUNT, inputValidityMask.length));

            StringBuilder sb = new StringBuilder();

            // TODO dynamic field strings
            if (inputValidityMask[0])
                sb.append(generateValidInputString("linesPlayed"));
            else {
                sb.append(generateInvalidInputString(INVALID_INPUT_SEED));
                sb.append(System.lineSeparator());
                return appendExitCommand(sb);
            }

            sb.append(System.lineSeparator());

            // TODO dynamic field strings
            if (inputValidityMask[1])
                sb.append(generateValidInputString("betAmount"));
            else {
                sb.append(generateInvalidInputString(INVALID_INPUT_SEED));
                sb.append(System.lineSeparator());
                return appendExitCommand(sb);
            }

            sb.append(System.lineSeparator());
            return appendExitCommand(sb);
        }

        private static void rewireStandardInputAndOutput(String input) {

            newStandardIn = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));

            newOutput = new ByteArrayOutputStream();
            newStandardOut = new PrintStream(newOutput);

            newError = new ByteArrayOutputStream();
            newStandardErr = new PrintStream(newError);

            System.setIn(newStandardIn);
            System.setOut(newStandardOut);
            System.setErr(newStandardErr);
        }

        private static String appendExitCommand(StringBuilder sb) {
            sb.append(EXIT_STRING).append(System.lineSeparator());
            return sb.toString();
        }

    }

    @BeforeAll
    static void loadProperties() {
        try {
            PROPERTIES.load(new FileInputStream("src/main/resources/game.properties"));
        } catch (IOException e) {
            log.error("Error loading properties file");
            throw new RuntimeException(e);
        }

//        int maxLinesPlayed = Integer.parseInt(PROPERTIES.getProperty("max_lines"));
//        int maxBetAmount = Integer.parseInt(PROPERTIES.getProperty("max_bet_amount"));
//
//        String validUserInput = String.format("%d%s%d%s%s",
//                rnd.nextInt(maxLinesPlayed + 1), System.lineSeparator(), rnd.nextInt(maxBetAmount + 1),
//                System.lineSeparator(), EXIT_STRING);


    }

}
