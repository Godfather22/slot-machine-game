package com.amusnet;

import com.amusnet.config.GameConfig;
import com.amusnet.game.Game;

import java.util.List;
import java.util.Map;

public class Application {
    public static void main(String[] args) {

        @SuppressWarnings("unchecked")
        var configuration = (GameConfig<Integer, Integer>) GameConfig.getInstance();

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

        // set up multipliers
        {
            // TODO Set up templates
            configuration.setMultipliers(Map.of(
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
            ));
        }

        // set starting balance
        configuration.setStartingBalance(100000);

        // set max bet amount
        configuration.setMaxBetAmount(10);

        @SuppressWarnings("unchecked")
        var game = (Game<Integer, Integer>) Game.getInstance();

        // main game loop
        while (true) {

        }
    }
}