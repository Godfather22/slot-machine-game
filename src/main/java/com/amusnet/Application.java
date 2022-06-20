package com.amusnet;

import com.amusnet.exception.ConfigurationInitializationException;
import com.amusnet.game.Game;
import com.amusnet.util.ErrorMessages;
import lombok.extern.slf4j.Slf4j;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Scanner;

import static com.amusnet.util.ErrorMessages.DefaultMessageTitles.*;

@Slf4j
public class Application {

    public static final Game GAME;

    static {
        try {
            GAME = new Game();
        } catch (ParserConfigurationException | IOException | SAXException e) {
            log.error("Fatal error while configuring game");
            throw new RuntimeException(e);
        } catch (ConfigurationInitializationException e) {
            log.error("Configuration constraint violated", e);
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {

        log.debug(System.lineSeparator() + GAME.getConfiguration().toString());

        ErrorMessages errorMessages = ErrorMessages.getInstance();

        Scanner sc = new Scanner(System.in);
        final int maxLines = GAME.getConfiguration().getLineCount();
        final double betLimit = GAME.getConfiguration().getBetLimit();

        final String exitCommand = GAME.getConfiguration().getExitCommand();

        // main game loop
        while (GAME.getCurrentBalance() >= 0.0) {

            GAME.prompt();

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

            GAME.setLinesPlayed(linesInput);
            GAME.setBetAmount(betInput);
            double newBalance = GAME.getCurrentBalance() - betInput * linesInput;
            GAME.setCurrentBalance(newBalance);

            // feedback
            System.out.printf("%s\t%s%nBalance: %s%n%n%s%n",
                    GAME.getLinesPlayed(),
                    GAME.getConfiguration().getCurrencyFormat().format(GAME.getBetAmount()),
                    GAME.getConfiguration().getCurrencyFormat().format(GAME.getCurrentBalance()),
                    GAME.generateScreen()
            );

            GAME.calculateTotalWinAndBalance();

            System.out.println();
        }
        System.out.println("Game over");
    }

}
