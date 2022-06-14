package com.amusnet;

import com.amusnet.config.GameConfig;
import com.amusnet.util.ErrorMessages;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Random;

import static com.amusnet.util.ErrorMessages.DefaultMessageTitles.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@Slf4j
public class UserInputTest {

    private static final int REQUIRED_INPUT_COUNT = 2;
    private static final int RANDOM_STRING_MAX_LENGTH = 30;
    private static final String INVALID_INPUT_SEED = "H1e2l3l4o5W6o7r8l9d";

    private static final GameConfig CONFIG = Application.GAME.getConfiguration();
    private static final ErrorMessages ERROR_MESSAGES = ErrorMessages.getInstance();

    private static InputStream newStandardIn;

    private static OutputStream newOutput;
    private static PrintStream newStandardOut;

    private static OutputStream newError;
    private static PrintStream newStandardErr;

    @Nested
    @DisplayName("Test invalid user input")
    class InvalidInputTests {

        @Test
        void userGivesInvalidInputForLinesPlayed_errorStreamHasCorrectContents() {
            // get input string with incorrect data for lines played
            String input = getInputString(new boolean[]{false, true});
            log.info("Generated input string:\n'{}'", input);
            rewireStandardInputAndOutput(input);
            Application.main(null);
            assertThat(newError.toString()).isEqualTo
                    (ERROR_MESSAGES.message(TITLE_EMSG_INVALID_LINES_INPUT) + System.lineSeparator());
        }

        @Test
        void userGivesInvalidInputForBetAmount_errorStreamHasCorrectContents() {
            // get input string with incorrect data for lines played
            String input = getInputString(new boolean[]{true, false});
            log.info("Generated input string:\n'{}'", input);
            rewireStandardInputAndOutput(input);
            Application.main(null);
            assertThat(newError.toString()).isEqualTo
                    (ERROR_MESSAGES.message(TITLE_EMSG_INVALID_BET_INPUT) + System.lineSeparator());
        }

        @Test
        void userGivesInvalidInputForBothInputFields_errorStreamHasCorrectContents() {
            // get input string with incorrect data for lines played
            String input = getInputString(new boolean[]{false, false});
            log.info("Generated input string:\n'{}'", input);
            rewireStandardInputAndOutput(input);
            Application.main(null);
            assertThat(newError.toString()).isEqualTo
                    (ERROR_MESSAGES.message(TITLE_EMSG_INVALID_LINES_INPUT) + System.lineSeparator());
        }

        @Test
        void userInputsMoreLinesPlayedThanAvailable_errorStreamHasCorrectContents() {
            // get input string with incorrect data for lines played
            String input = getInputString(new int[]{1, 0});
            log.info("Generated input string:\n'{}'", input);
            rewireStandardInputAndOutput(input);
            Application.main(null);
            assertThat(newError.toString()).isEqualTo
                    (ERROR_MESSAGES.message(TITLE_EMSG_INCORRECT_LINES_INPUT) + System.lineSeparator());
        }

        @Test
        void userInputsLessThan1LinesPlayed_errorStreamHasCorrectContents() {
            // get input string with incorrect data for lines played
            String input = getInputString(new int[]{-1, 0});
            log.info("Generated input string:\n'{}'", input);
            rewireStandardInputAndOutput(input);
            Application.main(null);
            assertThat(newError.toString()).isEqualTo
                    (ERROR_MESSAGES.message(TITLE_EMSG_INCORRECT_LINES_INPUT) + System.lineSeparator());
        }

        @Test
        void userInputsBetGreaterThanBetLimit_errorStreamHasCorrectContents() {
            // get input string with incorrect data for lines played
            String input = getInputString(new int[]{0, 1});
            log.info("Generated input string:\n'{}'", input);
            rewireStandardInputAndOutput(input);
            Application.main(null);
            assertThat(newError.toString()).isEqualTo
                    (ERROR_MESSAGES.message(TITLE_EMSG_INCORRECT_BET_INPUT) + System.lineSeparator());
        }

        @Test
        void userInputsBetLessThan1_errorStreamHasCorrectContents() {
            // get input string with incorrect data for lines played
            String input = getInputString(new int[]{0, -1});
            log.info("Generated input string:\n'{}'", input);
            rewireStandardInputAndOutput(input);
            Application.main(null);
            assertThat(newError.toString()).isEqualTo
                    (ERROR_MESSAGES.message(TITLE_EMSG_INCORRECT_BET_INPUT) + System.lineSeparator());
        }

        private static String generateValidInputString(String field) {
            Random rnd = new Random();
            // TODO dynamic field strings
            switch (field) {
                case "linesPlayed" -> {
                    int maxLines = CONFIG.getMaxLines();
                    return String.valueOf(rnd.nextInt(maxLines) + 1);
                }
                case "betAmount" -> {
                    double betLimit = CONFIG.getBetLimit();
                    return String.valueOf(rnd.nextDouble(betLimit) + 1);
                }
                default -> {
                    log.error("No such field '{}' accepts input", field);
                    fail(String.format("Field '%s' does not exist or does not accept input", field));
                    return null;
                }
            }
        }

        private static String generateInvalidInputString(String seed) {
            Random rnd = new Random();
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < RANDOM_STRING_MAX_LENGTH; i++)
                sb.append(seed.charAt(rnd.nextInt(seed.length())));

            return sb.toString();
        }

        private static String getInputString(boolean[] inputValidityMask) {
            if (inputValidityMask.length != REQUIRED_INPUT_COUNT) {
                log.error("Error: {} inputs required, mask of size {} illegal",
                        REQUIRED_INPUT_COUNT, inputValidityMask.length);
                fail(String.format("Input validity mask should be of size %d. Instead size is %d",
                        REQUIRED_INPUT_COUNT, inputValidityMask.length));
            }

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

        private static String getInputString(int[] boundViolationsMask) throws IllegalArgumentException {
            if (boundViolationsMask.length != REQUIRED_INPUT_COUNT) {
                log.error("Error: {} inputs required, mask of size {} illegal",
                        REQUIRED_INPUT_COUNT, boundViolationsMask.length);
                fail(String.format("Bound violations mask should be of size %d. Instead size is %d",
                        REQUIRED_INPUT_COUNT, boundViolationsMask.length));
            }

            StringBuilder sb = new StringBuilder();

            int i = -1;
            try {
                int[] bounds = new int[]{CONFIG.getMaxLines(), (int) CONFIG.getBetLimit()};
                for (i = 0; i < REQUIRED_INPUT_COUNT; i++) {
                    sb.append(getValueForField(boundViolationsMask[i], bounds[i]));
                }
            } catch (IllegalArgumentException e) {
               log.error("ERROR: Invalid value in array position {}. Should be -1, 0 or 1", i);
               throw new RuntimeException(e);
            }

            return appendExitCommand(sb);
        }

        // TODO ambiguous naming
        private static String getValueForField(int maskValue, int bound) {

            Random rnd = new Random();
            StringBuilder sb = new StringBuilder();

            switch (maskValue) {
                case -1 -> sb.append(rnd.nextInt() * -1);
                case 0 -> sb.append(rnd.nextInt(bound) + 1);
                case 1 -> sb.append(rnd.nextInt(bound + 1, Integer.MAX_VALUE));
                default -> throw new IllegalArgumentException(
                        String.format("Illegal mask value %d. Should be -1, 0, or 1",
                                maskValue));
            }
            sb.append(System.lineSeparator());
            return sb.toString();
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
            sb.append(CONFIG.getExitCommand()).append(System.lineSeparator());
            return sb.toString();
        }

    }

}
