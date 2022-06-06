package com.amusnet;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Random;

@Slf4j
public class UserInputTests {

    private static final int RANDOM_STRING_MAX_LENGTH = 30;
    private static final String EXIT_STRING = "quit";

    private static final Properties PROPERTIES = new Properties();

    private static final InputStream OLD_STANDARD_IN = System.in;
    private static final PrintStream OLD_STANDARD_OUT = System.out;
    private static final PrintStream OLD_STANDARD_ERR = System.err;

    private static InputStream newStandardIn;
    private static PrintStream newStandardOut;
    private static PrintStream newStandardErr;

    private UserInputTests() {
        try {
            PROPERTIES.load(new FileInputStream("src/main/resources/game.properties"));
        } catch (IOException e) {
            log.error("Error loading properties file");
            throw new RuntimeException(e);
        }

        String invalidInputSeed = "H1e2l3l4o5W6o7r8l9d";
        Random rnd = new Random();

//        int maxLinesPlayed = Integer.parseInt(PROPERTIES.getProperty("max_lines"));
//        int maxBetAmount = Integer.parseInt(PROPERTIES.getProperty("max_bet_amount"));
//
//        String validUserInput = String.format("%d%s%d%s%s",
//                rnd.nextInt(maxLinesPlayed + 1), System.lineSeparator(), rnd.nextInt(maxBetAmount + 1),
//                System.lineSeparator(), EXIT_STRING);

        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();

        for (int i = 0; i < RANDOM_STRING_MAX_LENGTH; i++)
            sb1.append(invalidInputSeed.charAt(rnd.nextInt(invalidInputSeed.length())));
        for (int i = 0; i < RANDOM_STRING_MAX_LENGTH; i++)
            sb2.append(invalidInputSeed.charAt(rnd.nextInt(invalidInputSeed.length())));

        String invalidUserInput = String.format("%s%s%s%s%s", sb1, System.lineSeparator(), sb2,
                                                System.lineSeparator(), EXIT_STRING);

        newStandardIn = new ByteArrayInputStream(invalidUserInput.getBytes(StandardCharsets.UTF_8));
        newStandardOut = new PrintStream(new ByteArrayOutputStream());
        newStandardErr = new PrintStream(new ByteArrayOutputStream());
    }

    @BeforeAll
    static void changeStandardInputAndOutput() {
        System.setIn(newStandardIn);
        System.setOut(newStandardOut);
        System.setErr(newStandardErr);
    }

    @Test
    void userGivesInvalidInput_ThrowExceptionAndLogError() {
//        givenWaitingForUserInput();
//        whenUserInputsInvalidInput();
//        thenThrowExceptionRePromptUserAndLogError();

        Application.main(null);
    }

    private void givenWaitingForUserInput() {

    }

    private void whenUserInputsInvalidInput() {

    }

    private void thenThrowExceptionRePromptUserAndLogError() {
    }

}
