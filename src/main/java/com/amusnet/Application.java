package com.amusnet;

import com.amusnet.config.GameConfig;
import com.amusnet.exception.InvalidGameDataException;
import com.amusnet.game.Game;
import com.amusnet.game.impl.NumberCard;
import com.amusnet.util.ErrorMessages;
import lombok.extern.slf4j.Slf4j;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import static com.amusnet.util.ErrorMessages.DefaultMessageTitles.*;

@Slf4j
public class Application {
    public static void main(String[] args) {



        //  Temporary manual setup of configuration
        //setupConfiguration(configuration);
        //log.debug(configuration.toString());

        ErrorMessages errorMessages = ErrorMessages.getInstance();

        Game game = null;
        try {
            game = new Game();
        } catch (ParserConfigurationException | IOException | SAXException e) {
            log.error("Fatal error while configuring game");
            throw new RuntimeException(e);
        }

        Scanner sc = new Scanner(System.in);
        final int maxLines = game.getConfiguration().getMaxLines();
        final double betLimit = game.getConfiguration().getBetLimit();

        final String exitCommand = game.getConfiguration().getExitCommand();

        // main game loop
        while (game.getCurrentBalance() >= 0.0) {

            game.prompt();

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
                    log.error("Error: Number of lines input {} out of bounds for available values: 1-{}",
                            linesInput, maxLines);
                    continue;
                }
                if (betInput < 1 || betInput > betLimit) {
                    System.err.println(errorMessages.message
                            (TITLE_EMSG_INCORRECT_BET_INPUT, "Incorrect bet amount placed!"));
                    log.error("Error: Bet amount input {} out of bounds for available values: 1-{}",
                            betInput, betLimit);
                    continue;
                }
            }

            game.setLinesPlayed(linesInput);
            game.setBetAmount(betInput);
            game.setCurrentBalance(game.getCurrentBalance() - betInput * linesInput);

            // feedback
            System.out.printf("%s\t%s%nBalance: %s%n%n%s%n",
                    game.getLinesPlayed(),
                    game.getConfiguration().getCurrencyFormat().format(game.getBetAmount()),
                    game.getConfiguration().getCurrencyFormat().format(game.getCurrentBalance()),
                    game.generateScreen()
            );
            try {
                game.calculateTotalWinAndBalance();
            } catch (InvalidGameDataException e) {
                log.error(e.getMessage());
            }

            System.out.println();
        }
    }

    @Deprecated
    private static void setupConfiguration(GameConfig configuration) {
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

            Map<NumberCard<Integer>, Map<Integer, Integer>> tableData = Map.of(
                    new NumberCard<>(0), Map.of(  3, 10,
                            4, 20,
                            5, 100),
                    new NumberCard<>(1), Map.of(  3, 10,
                            4, 20,
                            5, 100),
                    new NumberCard<>(2), Map.of(  3, 10,
                            4, 20,
                            5, 100),
                    new NumberCard<>(3), Map.of(  3, 20,
                            4, 40,
                            5, 200),
                    new NumberCard<>(4), Map.of(  3, 20,
                            4, 40,
                            5, 200),
                    new NumberCard<>(5), Map.of(  3, 20,
                            4, 80,
                            5, 400),
                    new NumberCard<>(6), Map.of(  3, 40,
                            4, 400,
                            5, 1000),
                    new NumberCard<>(7, true), Map.of(  3, 5,
                            4, 20,
                            5, 500)
            );

            configuration.setupTable(occurrenceCounts, tableData);
        }

        // set scatters
        configuration.setScatters(Set.of(new NumberCard<>(7, true)));

        // set starting balance
        configuration.setStartingBalance(100000);

        // set max bet amount
        configuration.setBetLimit(10);

        // TODO not thread-safe
        // set currency format
        configuration.getCurrencyFormat().applyPattern("#.##");
    }

}
