package com.amusnet;

import com.amusnet.game.Game;
import com.amusnet.game.ReelScreen;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class WildcardBehaviourTest {

    private final Logger log = LoggerFactory.getLogger(GameBehaviourTest.class);

    private final Game game = new Game();
    private final ReelScreen rs = game.getGameRound().getReelScreen();
    private int[] diceRolls;

    @Test
        // Requirement 5H
    void betOn3LinesWithAmount10_genArrayIs0_0_0_15_19_win1200FromLines() {
        GivenBetOn3LinesFor10AndGenArray0_0_0_15_19();
        WhenGenerationNumbersAreFedToGenerator();
        ThenShouldWin1200FromLines();
    }

    private void GivenBetOn3LinesFor10AndGenArray0_0_0_15_19() {
        game.setupNextRound(3, 10.0, false);
        diceRolls = new int[]{0, 0, 0, 15, 19};
    }

    @Test
        // Requirement 5G
    void betOn1LineWithAmount10_genArrayIs0_0_0_15_19_win400FromLines() {
        GivenBetOn1LineFor10AndGenArray0_0_0_15_19();
        WhenGenerationNumbersAreFedToGenerator();
        ThenShouldWin400FromLines();
    }

    private void GivenBetOn1LineFor10AndGenArray0_0_0_15_19() {
        game.setupNextRound(1, 10.0, false);
        diceRolls = new int[]{0, 0, 0, 15, 19};
    }

    @Test
        // Requirement 5F
    void betOn3LinesWithAmount5_genArrayIs0_0_0_18_19_win1500FromLines() {
        GivenBetOn3LinesFor5AndGenArray0_0_0_18_19();
        WhenGenerationNumbersAreFedToGenerator();
        ThenShouldWin1500FromLines();
    }

    private void GivenBetOn3LinesFor5AndGenArray0_0_0_18_19() {
        game.setupNextRound(3, 5.0, false);
        diceRolls = new int[]{0, 0, 0, 18, 19};
    }

    private void ThenShouldWin1500FromLines() {
        assertWinAmount(1500.0);
    }

    @Test
        // Requirement 5E
    void betOn1LineWithAmount5_genArrayIs0_0_0_18_19_win500FromLines() {
        GivenBetOn1LineFor5AndGenArray0_0_0_18_19();
        WhenGenerationNumbersAreFedToGenerator();
        ThenShouldWin500FromLines();
    }

    private void GivenBetOn1LineFor5AndGenArray0_0_0_18_19() {
        game.setupNextRound(1, 5.0);
        diceRolls = new int[]{0, 0, 0, 18, 19};
    }

    private void ThenShouldWin500FromLines() {
        assertWinAmount(500.0);
    }

    @Test
        // Requirement 5D
    void betOn3LinesWithAmount10_genArrayIs0_0_0_18_9_win1200FromLines() {
        GivenBetOn3LinesFor10AndGenArray0_0_0_18_9();
        WhenGenerationNumbersAreFedToGenerator();
        ThenShouldWin1200FromLines();
    }

    private void GivenBetOn3LinesFor10AndGenArray0_0_0_18_9() {
        game.setupNextRound(3, 10.0, false);
        diceRolls = new int[]{0, 0, 0, 18, 9};
    }

    private void ThenShouldWin1200FromLines() {
        assertWinAmount(1200.0);
    }

    @Test
        // Requirement 5C
    void betOn1LineWithAmount10_genArrayIs0_0_0_18_9_win400FromLines() {
        GivenBetOn1LineFor10AndGenArray0_0_0_18_9();
        WhenGenerationNumbersAreFedToGenerator();
        ThenShouldWin400FromLines();
    }

    private void GivenBetOn1LineFor10AndGenArray0_0_0_18_9() {
        game.setupNextRound(1, 10.0, false);
        diceRolls = new int[]{0, 0, 0, 18, 9};
    }

    private void ThenShouldWin400FromLines() {
        assertWinAmount(400.0);
    }

    @Test
        // Requirement 5B
    void betOn1LineWithAmount10_genArrayIs10_20_29_17_24_win200FromLines() {
        GivenBetOn1LineFor10AndGenArray10_20_29_17_24();
        WhenGenerationNumbersAreFedToGenerator();
        ThenShouldWin200FromLines();
    }

    private void GivenBetOn1LineFor10AndGenArray10_20_29_17_24() {
        game.setupNextRound(1, 10.0, false);
        diceRolls = new int[]{10, 20, 29, 17, 24};
    }

    private void ThenShouldWin200FromLines() {
        assertWinAmount(200.0);
    }

    @Test
        // Requirement 5A
    void betOn1LineWithAmount5_genArrayIs4_29_29_17_24_win100FromLines() {
        GivenBetOn1LineFor5AndGenArray4_29_29_17_24();
        WhenGenerationNumbersAreFedToGenerator();
        ThenShouldWin100FromLines();
    }

    private void GivenBetOn1LineFor5AndGenArray4_29_29_17_24() {
        game.setupNextRound(1, 5.0, false);
        diceRolls = new int[]{4, 29, 29, 17, 24};
    }

    private void WhenGenerationNumbersAreFedToGenerator() {
        generateScreenFromDiceRolls();
    }

    private void ThenShouldWin100FromLines() {
        assertWinAmount(100.0);
    }

    private void generateScreenFromDiceRolls() {
        ReelScreen reelScreen = rs.generateScreen(diceRolls);
        log.info(System.lineSeparator() + reelScreen.toString());
    }

    private void assertWinAmount(double fromLines) {
        double oldBalance = game.getGameState().getCurrentBalance();
        assertThat(game.playNextRound()).as("Total win amount")
                .isEqualTo(fromLines + 0.0);
        assertThat(game.getGameRound().getWinFromLines()).as("Win from lines")
                .isEqualTo(fromLines);
        assertThat(game.getGameRound().getWinFromScatters()).as("Win from scatters")
                .isEqualTo(0.0);
        assertThat(game.getGameState().getCurrentBalance()).as("New balance amount")
                .isEqualTo(oldBalance + (fromLines + 0.0));
    }

}
