package com.amusnet;

import com.amusnet.game.Game;
import com.amusnet.game.components.ReelScreen;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class GameBehaviourTest {

    private final Logger log = LoggerFactory.getLogger(GameBehaviourTest.class);

    private final Game game = new Game();
    private final ReelScreen rs = game.getGameRound().getReelScreen();
    private int[] diceRolls;

    // Testing a game of Integer cards with Integer currency (money) type
    @Nested
    @DisplayName("Tests for correct generation of screen reels")
    class ReelScreenGenerationTest {

        @Test
        void feedScreenGeneratorNumber29_correctScreenGenerates() {
            Given5Size30ReelArraysAndGenerationNumbers29_28_27_28_29(); // test edge cases
            WhenGenerationNumbersAreFedToGenerator();
            ThenScreenGeneratesWithPredictedContents(List.of(
                    List.of(2, 5, 2, 4, 5),
                    List.of(6, 5, 5, 4, 6),
                    List.of(6, 6, 5, 6, 6)
            ));
        }

        private void Given5Size30ReelArraysAndGenerationNumbers29_28_27_28_29() {
            diceRolls = new int[]{29, 28, 27, 28, 29};
        }

        @Test
        void feedScreenGeneratorNumber15_correctScreenGenerates() {
            Given5Size30ReelArraysAndGenerationNumbers15_28_0_10_19();
            WhenGenerationNumbersAreFedToGenerator();
            ThenScreenGeneratesWithPredictedContents(List.of(
                    List.of(2, 5, 6, 0, 1),
                    List.of(2, 5, 6, 0, 1),
                    List.of(2, 6, 6, 5, 1)
            ));
        }

        private void Given5Size30ReelArraysAndGenerationNumbers15_28_0_10_19() {
            diceRolls = new int[]{15, 28, 0, 10, 19};
        }

        private void WhenGenerationNumbersAreFedToGenerator() {
            rs.generateScreen(diceRolls);
        }

        private void ThenScreenGeneratesWithPredictedContents(List<List<Integer>> predictedContents) {
            var prediction = new ReelScreen(predictedContents);
            assertThat(rs).usingRecursiveComparison()
                    .ignoringFields("config")
                    .isEqualTo(prediction);
        }
    }

    @Nested
    @DisplayName("Tests for correct gameplay")
    class GameplayTest {

        @Test
        void play20LinesFor5_diceRollsAre22_18_17_0_29_win500FromScatters() {
            GivenBetOn20LinesForAmount5AndDiceRolls22_18_17_0_29();
            WhenGenerationNumbersAreFedToGenerator();
            ThenShouldWin500FromScatters();
        }

        private void GivenBetOn20LinesForAmount5AndDiceRolls22_18_17_0_29() {
            makeBet(20, 5.0);
            diceRolls = new int[]{22, 18, 17, 0, 29};
        }

        private void ThenShouldWin500FromScatters() {
            assertWinAmounts(0.0, 500.0);
        }

        @Test
        void play10LinesFor5_diceRollsAre12_19_9_22_28_win0Total() {
            GivenBetOn10LinesForAmount5AndDiceRolls12_19_9_22_28();
            WhenGenerationNumbersAreFedToGenerator();
            ThenShouldWin0Total();
        }

        private void GivenBetOn10LinesForAmount5AndDiceRolls12_19_9_22_28() {
            makeBet(10, 5.0);
            diceRolls = new int[]{12, 19, 9, 22, 28};
        }

        @Test
        void play20LinesFor10_diceRollsAre5_2_19_13_0_win0Total() {
            GivenBetOn20LinesForAmount10AndDiceRolls5_2_19_13_0();
            WhenGenerationNumbersAreFedToGenerator();
            ThenShouldWin0Total();
        }

        private void GivenBetOn20LinesForAmount10AndDiceRolls5_2_19_13_0() {
            makeBet(20, 10.0);
            diceRolls = new int[]{5, 2, 19, 13, 0};
        }

        private void ThenShouldWin0Total() {
            assertWinAmounts(0.0, 0.0);
        }

        @Test
        void play5LinesFor10_diceRollsAre15_3_15_22_25_win50000FromLines() {
            GivenBetOn5LinesForAmount10AndDiceRolls15_3_15_22_25();
            WhenGenerationNumbersAreFedToGenerator();
            ThenShouldWin5000FromLines();
        }

        private void GivenBetOn5LinesForAmount10AndDiceRolls15_3_15_22_25() {
            makeBet(5, 10.0);
            diceRolls = new int[]{15, 3, 15, 22, 25};
        }

        @Test
        void play5LinesFor10_diceRollsAre15_3_15_22_9_win50000FromLines() {
            GivenBetOn5LinesForAmount10AndDiceRolls15_3_15_22_9();
            WhenGenerationNumbersAreFedToGenerator();
            ThenShouldWin5000FromLines();
        }

        private void GivenBetOn5LinesForAmount10AndDiceRolls15_3_15_22_9() {
            makeBet(5, 10.0);
            diceRolls = new int[]{15, 3, 15, 22, 9};
        }

        private void ThenShouldWin5000FromLines() {
            assertWinAmounts(5000.0, 0.0);
        }

        @Test
        void play20LinesFor5_diceRollsAre23_16_7_19_13_win100FromLinesAnd500FromScatters() {
            GivenBetOn20LinesForAmount5AndDiceRolls23_16_7_19_13();
            WhenGenerationNumbersAreFedToGenerator();
            ThenShouldWin100FromLinesAnd500FromScatters();
        }

        private void GivenBetOn20LinesForAmount5AndDiceRolls23_16_7_19_13() {
            makeBet(20, 5.0);
            diceRolls = new int[]{23, 16, 7, 19, 13};
        }

        private void WhenGenerationNumbersAreFedToGenerator() {
            generateScreenFromDiceRolls();
        }

        private void ThenShouldWin100FromLinesAnd500FromScatters() {
            assertWinAmounts(100.0, 500.0);
        }

        //
        // helper methods
        //

        private void makeBet(int linesPlayed, double betAmount) {
            game.setupNextRound(linesPlayed, betAmount, false);
            log.info("Lines {}; Bet per line {}", linesPlayed, betAmount);
        }

        private void generateScreenFromDiceRolls() {
            ReelScreen reelScreen = rs.generateScreen(diceRolls);
            log.info(System.lineSeparator() + reelScreen.toString());
        }

        private void assertWinAmounts(double fromLines, double fromScatters) {
            double oldBalance = game.getGameState().getCurrentBalance();
            assertThat(game.playNextRound()).as("Total win amount")
                    .isEqualTo(fromLines + fromScatters);
            assertThat(game.getGameRound().getWinFromLines()).as("Win from lines")
                    .isEqualTo(fromLines);
            assertThat(game.getGameRound().getWinFromScatters()).as("Win from scatters")
                    .isEqualTo(fromScatters);
            assertThat(game.getGameState().getCurrentBalance()).as("New balance amount")
                    .isEqualTo(oldBalance + (fromLines + fromScatters));
        }

    }

}
