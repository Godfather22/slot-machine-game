package com.amusnet;

import com.amusnet.config.GameConfig;
import com.amusnet.game.Game;
import com.amusnet.game.Screen;
import com.amusnet.game.impl.NumberCard;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class GameBehaviourTest {

    @SuppressWarnings("unchecked")
    private static final GameConfig<NumberCard<Integer>> config = (GameConfig<NumberCard<Integer>>) GameConfig.getInstance();
    
    // Testing a game of Integer cards with Integer currency (money) type
    @SuppressWarnings("unchecked")
    private static final Game<NumberCard<Integer>> game = (Game<NumberCard<Integer>>) Game.getInstance();

    @BeforeAll
    static void setUpConfiguration() {
        // set up reel arrays
        {
            config.setReels(List.of(
                    List.of(6,6,6,1,1,1,0,0,0,3,3,3,4,4,4,2,2,2,5,5,5,1,1,1,7,4,4,4,2,2),
                    List.of(6,6,6,2,2,2,1,1,1,0,0,0,5,5,5,1,1,1,7,3,3,3,2,2,2,0,0,0,5,5),
                    List.of(6,6,6,4,4,4,0,0,0,1,1,1,5,5,5,2,2,2,7,3,3,3,0,0,0,2,2,2,5,5),
                    List.of(6,6,6,2,2,2,4,4,4,0,0,0,5,5,5,3,3,3,1,1,1,7,2,2,2,0,0,0,4,4),
                    List.of(6,6,6,1,1,1,4,4,4,2,2,2,5,5,5,0,0,0,7,1,1,1,3,3,3,2,2,2,5,5)

            ));
        }

        // set up lines
        {
            config.setLines(List.of(
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

            config.setupTable(occurrenceCounts, tableData);
        }

        // set scatters
        config.setScatters(Set.of(new NumberCard<Integer>(7, true)));

        // set starting balance
        config.setStartingBalance(100000);

        // set max bet amount
        config.setMaxBetAmount(10);
    }

    @Nested
    @DisplayName("Tests for the correct generation of screen reels")
    class ScreenGenerationTest {

        int generationNumber;

        @Test
        void feedScreenGeneratorNumber29_correctScreenGenerates() {
            Given5Size30ReelArraysAndGenerationNumber29();
            WhenGenerationNumberIsFedToGenerator();
            ThenScreenGeneratesWithPredictedContents(List.of(
                    List.of(2, 5, 5, 4, 5),
                    List.of(6, 6, 6, 6, 6),
                    List.of(6, 6, 6, 6, 6)
            ));
        }

        private void Given5Size30ReelArraysAndGenerationNumber29() {
            set5Size30ReelArrays();
            this.generationNumber = 29;
        }

        @Test
        void feedScreenGeneratorNumber15_correctScreenGenerates() {
            Given5Size30ReelArraysAndGenerationNumber15();
            WhenGenerationNumberIsFedToGenerator();
            ThenScreenGeneratesWithPredictedContents(List.of(
                    List.of(2, 1, 2, 3, 0),
                    List.of(2, 1, 2, 3, 0),
                    List.of(2, 1, 2, 3, 0)
            ));
        }

        private void Given5Size30ReelArraysAndGenerationNumber15() {
            set5Size30ReelArrays();
            this.generationNumber = 15;
        }

        private void WhenGenerationNumberIsFedToGenerator() {
            game.generateScreen(this.generationNumber);
        }

        private void ThenScreenGeneratesWithPredictedContents(List<List<Integer>> predictedContents) {
            var actual = game.getScreen();
            var prediction = new Screen(predictedContents);
            assertThat(actual).isEqualTo(prediction);
        }

        private void set5Size30ReelArrays() {
            config.setReels(List.of(
                    List.of(6,6,6,1,1,1,0,0,0,3,3,3,4,4,4,2,2,2,5,5,5,1,1,1,7,4,4,4,2,2),
                    List.of(6,6,6,2,2,2,1,1,1,0,0,0,5,5,5,1,1,1,7,3,3,3,2,2,2,0,0,0,5,5),
                    List.of(6,6,6,4,4,4,0,0,0,1,1,1,5,5,5,2,2,2,7,3,3,3,0,0,0,2,2,2,5,5),
                    List.of(6,6,6,2,2,2,4,4,4,0,0,0,5,5,5,3,3,3,1,1,1,7,2,2,2,0,0,0,4,4),
                    List.of(6,6,6,1,1,1,4,4,4,2,2,2,5,5,5,0,0,0,7,1,1,1,3,3,3,2,2,2,5,5)
            ));
        }
    }

    @Test
    void play20LinesFor5_win100FromLinesAnd500FromScatters() {
        GivenBetOn20LinesForAmount5();
        WhenLine4HasFourInitial_1_s();
        ThenShouldWin100FromLinesAnd500FromScatters();
    }

    private void GivenBetOn20LinesForAmount5() {
        game.setLinesPlayed(20);
        game.setBetAmount(5);
    }

    private void WhenLine4HasFourInitial_1_s() {
        // enforce non-random generation
        var screenAsList = List.of(
                List.of(1, 1, 0, 1, 5),
                List.of(7, 1, 0, 1, 5),
                List.of(4, 7, 1, 7, 0)
        );
        game.setScreen(new Screen(screenAsList));
    }

    private void ThenShouldWin100FromLinesAnd500FromScatters() {
        Double winAmount = game.calculateTotalWin();
    }

}
