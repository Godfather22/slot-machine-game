package com.amusnet;

import com.amusnet.exception.InvalidGameDataException;
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
import static org.assertj.core.api.Assertions.fail;

@Slf4j
public class GameBehaviourTest {

    private static final Game game;

    static {
        try {
            game = new Game();
        } catch (ParserConfigurationException | IOException | SAXException e) {
            log.error("Fatal error while configuring game", e);
            throw new RuntimeException(e);
        }
    }

    // Testing a game of Integer cards with Integer currency (money) type
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

}
