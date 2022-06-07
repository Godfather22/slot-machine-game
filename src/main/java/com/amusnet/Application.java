package com.amusnet;

import com.amusnet.config.GameConfig;
import com.amusnet.game.Game;
import com.amusnet.game.impl.NumberCard;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

@Slf4j
public class Application {
    public static void main(String[] args) {

        @SuppressWarnings("unchecked")
        var configuration = (GameConfig<NumberCard<Integer>>) GameConfig.getInstance();

        //  Temporary manual setup of configuration
        setupConfiguration(configuration);

        @SuppressWarnings("unchecked")
        var game = (Game<NumberCard<Integer>>) Game.getInstance();

        Scanner sc = new Scanner(System.in);
        final int maxLines = Integer.parseInt(game.getProperties().getProperty("max_lines"));
        final int maxBetAmount = Integer.parseInt(game.getProperties().getProperty("max_bet_amount"));

        // main game loop
        while (game.getCurrentBalance() >= 0.0) {

            game.prompt();

            String firstInput = sc.next();
            if (firstInput.equalsIgnoreCase("quit"))
                break;

            int linesInput = -1;
            try {
                linesInput = Integer.parseInt(firstInput);
            }
            catch (NumberFormatException e) {
                System.err.println("Invalid input!");
                log.error("Invalid user input: {}", linesInput);
            }

            int betInput = -1;
            try {
                betInput = Integer.parseInt(sc.next());
            }
            catch (NumberFormatException e) {
                System.err.println("Invalid input!");
                log.error("Invalid user input: {}", betInput);
            }

            // TODO checks for valid input

            game.setLinesPlayed(linesInput);
            game.setBetAmount(betInput);

            game.setCurrentBalance(game.getCurrentBalance() - betInput);
            game.generateScreen();
            game.setCurrentBalance(game.getCurrentBalance() + game.calculateTotalWin());
        }
    }

    @Deprecated
    private static void setupConfiguration(GameConfig<NumberCard<Integer>> configuration) {
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
        configuration.setScatters(Set.of(new NumberCard<Integer>(7, true)));

        // set starting balance
        configuration.setStartingBalance(100000);

        // set max bet amount
        configuration.setMaxBetAmount(10);

        log.info(configuration.toString());
    }

}
