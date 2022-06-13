package com.amusnet;

import com.amusnet.config.GameConfig;
import com.amusnet.exception.InvalidGameDataException;
import com.amusnet.game.Card;
import com.amusnet.game.Game;
import com.amusnet.game.Screen;
import com.amusnet.game.impl.NumberCard;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

import java.util.*;

@Slf4j
public class GameBehaviourTest {

    @SuppressWarnings("unchecked")
    private static final GameConfig<NumberCard<Integer>> config = (GameConfig<NumberCard<Integer>>) GameConfig.getInstance();
    
    // Testing a game of Integer cards with Integer currency (money) type
    @SuppressWarnings("unchecked")
    private static final Game<NumberCard<Integer>> game = (Game<NumberCard<Integer>>) Game.getInstance();

    @Nested
    @DisplayName("Tests for correct generation of screen reels")
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
            setUpReelArrays();      // no need for further configuration here
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
            setUpReelArrays();      // no need for further configuration here
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
    }

    @Nested
    @DisplayName("Tests for correct gameplay")
    class GameplayTest {

        @BeforeAll
        static void configure() {
            configureAll();
        }

        @Test
        void play5LinesFor10_diceRollIs30_win50000FromLines() {
            GivenBetOn5LinesForAmount10();
            WhenGenerationDiceRollIs30();
            ThenShouldWin50000FromLines();
        }

        private void WhenGenerationDiceRollIs30() {
            generateScreenWithDiceRoll(30);
        }

        private void WhenGenerationDiceRollIs24() {
            generateScreenWithDiceRoll(24);
        }

        @Test
        void play20LinesFor5_diceRollIs18_win500FromScatters() {
            GivenBetOn20LinesForAmount5();
            WhenGenerationDiceRollIs18();
            ThenShouldWin500FromScatters();
        }

        private void WhenGenerationDiceRollIs18() {
            generateScreenWithDiceRoll(18);
        }

        private void ThenShouldWin500FromScatters() {
            assertWinAmounts(0.0, 500.0);
        }

        @Test
        void play10LinesFor5_diceRollIs12_win0Total() {
            GivenBetOn10LinesForAmount5();
            WhenGenerationDiceRollIs12();
            ThenShouldWin0Total();    // the first card of each line is different from the rest
        }

        private void GivenBetOn10LinesForAmount5() {
            makeBet(10, 5.0);
        }

        private void WhenGenerationDiceRollIs12() {
            generateScreenWithDiceRoll(12);
        }

        @Test
        void play20LinesFor10_diceRollIs6_win0Total() {
            GivenBetOn20LinesForAmount10();
            WhenGenerationDiceRollIs6();
            ThenShouldWin0Total();
        }

        private void GivenBetOn20LinesForAmount10() {
            makeBet(20, 10.0);
        }

        private void WhenGenerationDiceRollIs6() {
            generateScreenWithDiceRoll(6);
        }

        private void ThenShouldWin0Total() {
            assertWinAmounts(0.0, 0.0);
        }

        @Test
        void play5LinesFor10_diceRollIs0_win50000FromLines() {
            GivenBetOn5LinesForAmount10();
            WhenGenerationDiceRollIs0();
            ThenShouldWin50000FromLines();
        }

        private void GivenBetOn5LinesForAmount10() {
            makeBet(5, 10.0);
        }

        private void WhenGenerationDiceRollIs0() {
            generateScreenWithDiceRoll(0);
        }

        private void ThenShouldWin50000FromLines() {
            assertWinAmounts(50000.0, 0.0);
        }

        @Test
        void play20LinesFor5_win100FromLinesAnd500FromScatters() {
            GivenBetOn20LinesForAmount5();
            WhenLine4HasFourInitial_1_s();
            ThenShouldWin100FromLinesAnd500FromScatters();
        }

        private void GivenBetOn20LinesForAmount5() {
            makeBet(20, 5.0);
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
            assertWinAmounts(100.0, 500.0);
        }

        //
        // helper methods
        //

        private void makeBet(int linesPlayed, double betAmount) {
            game.setLinesPlayed(linesPlayed);
            game.setBetAmount(betAmount);
            log.info("Lines {}; Bet per line {}", linesPlayed, betAmount);
        }

        private void generateScreenWithDiceRoll(int diceRoll) {
            Screen screen = game.generateScreen(diceRoll);
            log.info(System.lineSeparator() + screen.toString());
        }

        private void assertWinAmounts(double fromLines, double fromScatters) {
            double oldBalance = game.getCurrentBalance();
            try {
                assertThat(game.calculateTotalWinAndBalance()).as("Total win amount")
                        .isEqualTo(fromLines + fromScatters);
            } catch (InvalidGameDataException e) {
                fail("Method threw unexpected exception");
            }
            assertThat(game.getLastWinFromLines()).as("Win from lines")
                    .isEqualTo(fromLines);
            assertThat(game.getLastWinFromScatters()).as("Win from scatters")
                    .isEqualTo(fromScatters);
            assertThat(game.getCurrentBalance()).as("New balance amount")
                    .isEqualTo(oldBalance + (fromLines + fromScatters));
        }

    }

    //
    // helper methods for configuration
    //

    private static void configureAll() {
        // set up reel arrays
        {
            setUpReelArrays();
        }

        // set up lines
        {
            setUpLineArrays();
        }

        // set up table
        {
            setUpScoreTable();
        }

        // set scatters
        setScatterCards(Set.of(new NumberCard<Integer>(7, true)));

        // set starting balance
        setStartingBalance(100000);

        // set max bet amount
        setMaxBetAmount(10);
    }

    private static void setUpReelArrays() {
        config.setReels(List.of(
                List.of(6,6,6,1,1,1,0,0,0,3,3,3,4,4,4,2,2,2,5,5,5,1,1,1,7,4,4,4,2,2),
                List.of(6,6,6,2,2,2,1,1,1,0,0,0,5,5,5,1,1,1,7,3,3,3,2,2,2,0,0,0,5,5),
                List.of(6,6,6,4,4,4,0,0,0,1,1,1,5,5,5,2,2,2,7,3,3,3,0,0,0,2,2,2,5,5),
                List.of(6,6,6,2,2,2,4,4,4,0,0,0,5,5,5,3,3,3,1,1,1,7,2,2,2,0,0,0,4,4),
                List.of(6,6,6,1,1,1,4,4,4,2,2,2,5,5,5,0,0,0,7,1,1,1,3,3,3,2,2,2,5,5)

        ));
    }

    private static void setUpLineArrays() {
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

    private static void setUpScoreTable() {
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

    private static void setScatterCards(Set<Card> scatterCardsValues) {
        config.setScatters(scatterCardsValues);
    }

    private static void setStartingBalance(int balance) {
        config.setStartingBalance(balance);
    }

    private static void setMaxBetAmount(int amount) {
        config.setBetLimit(amount);
    }

}
