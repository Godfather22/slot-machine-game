package com.amusnet;

import com.amusnet.config.GameConfig;
import com.amusnet.game.Game;
import com.amusnet.game.impl.NumberCard;
import com.amusnet.game.impl.IntegerCard;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

@Slf4j
public class Application {
    public static void main(String[] args) {

        @SuppressWarnings("unchecked")
        var configuration = (GameConfig<NumberCard, Integer>) GameConfig.getInstance();

        //  Temporary manual setup of configuration

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
            var columnIndexes = Map.of(
                    "x3", 3,
                    "x4", 4,
                    "x5", 5
            );

            var tableData = Map.of(
                    new IntegerCard(0), Map.of(  3, 10,
                            4, 20,
                            5, 100),
                    new IntegerCard(1), Map.of(  3, 10,
                            4, 20,
                            5, 100),
                    new IntegerCard(2), Map.of(  3, 10,
                            4, 20,
                            5, 100),
                    new IntegerCard(3), Map.of(  3, 20,
                            4, 40,
                            5, 200),
                    new IntegerCard(4), Map.of(  3, 20,
                            4, 40,
                            5, 200),
                    new IntegerCard(5), Map.of(  3, 20,
                            4, 80,
                            5, 400),
                    new IntegerCard(6), Map.of(  3, 40,
                            4, 400,
                            5, 1000),
                    new IntegerCard(7, true), Map.of(  3, 5,
                            4, 20,
                            5, 500)
            );

            // TODO troubleshoot generic shenanigans
//            configuration.setupTable(columnIndexes, tableData);
        }

        // set starting balance
        configuration.setStartingBalance(100000);

        // set max bet amount
        configuration.setMaxBetAmount(10);

        log.info(configuration.toString());

        @SuppressWarnings("unchecked")
        var game = (Game<IntegerCard, Integer>) Game.getInstance();

        Scanner sc = new Scanner(System.in);

        // main game loop
        while (!game.isGameOver()) {
            String input = sc.nextLine();
            if (input.equalsIgnoreCase("quit"))
                game.quit();
        }
    }
}
