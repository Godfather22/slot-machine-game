package com.amusnet;

import com.amusnet.exception.ConfigurationInitializationException;
import com.amusnet.game.Game;
import com.amusnet.game.Screen;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class GameBehaviourTest {

    private static final Game<String> game;
    private static int[] diceRolls;

    static {
        try {
            game = new Game<>();
        } catch (ParserConfigurationException | IOException | SAXException e) {
            log.error("Fatal error while configuring game", e);
            throw new RuntimeException(e);
        } catch (ConfigurationInitializationException e) {
            log.error("Configuration constraint violated", e);
            throw new RuntimeException(e);
        }
    }

    // Testing a game of String cards with Integer currency (money) type
    @Nested
    @DisplayName("Tests for correct generation of screen reels")
    class ScreenGenerationTest {

        @Test
        void feedScreenGeneratorNumber29_correctScreenGenerates() {
            Given5Size30ReelArraysAndGenerationNumbers29_28_27_28_29(); // test edge cases
            WhenGenerationNumbersAreFedToGenerator();
            ThenScreenGeneratesWithPredictedContents(List.of(
                    List.of("C", "F", "C", "E", "F"),
                    List.of("G", "F", "F", "E", "G"),
                    List.of("G", "G", "F", "G", "G")
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
                    List.of("C", "F", "G", "A", "B"),
                    List.of("C", "F", "G", "A", "B"),
                    List.of("C", "G", "G", "F", "B")
            ));
        }

        private void Given5Size30ReelArraysAndGenerationNumbers15_28_0_10_19() {
            diceRolls = new int[]{15, 28, 0, 10, 19};
        }

        private void WhenGenerationNumbersAreFedToGenerator() {
            game.generateScreen(diceRolls);
        }

        private void ThenScreenGeneratesWithPredictedContents(List<List<String>> predictedContents) {
            var actual = game.getScreen();
            var prediction = new Screen<>(predictedContents);
            assertThat(actual).isEqualTo(prediction);
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
            game.setLinesPlayed(linesPlayed);
            game.setBetAmount(betAmount);
            log.info("Lines {}; Bet per line {}", linesPlayed, betAmount);
        }

        private void generateScreenFromDiceRolls() {
            Screen<String> screen =  game.generateScreen(diceRolls);
            log.info(System.lineSeparator() + screen.toString());
        }

        private void assertWinAmounts(double fromLines, double fromScatters) {
            double oldBalance = game.getCurrentBalance();
            assertThat(game.calculateTotalWinAndBalance()).as("Total win amount")
                    .isEqualTo(fromLines + fromScatters);
            assertThat(game.getLastWinFromLines()).as("Win from lines")
                    .isEqualTo(fromLines);
            assertThat(game.getLastWinFromScatters()).as("Win from scatters")
                    .isEqualTo(fromScatters);
            assertThat(game.getCurrentBalance()).as("New balance amount")
                    .isEqualTo(oldBalance + (fromLines + fromScatters));
        }

    }

}
