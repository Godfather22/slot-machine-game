package com.amusnet;

import com.amusnet.game.Game;
import org.junit.jupiter.api.Test;

import java.util.List;

public class GameBehaviourTest {

    // Testing a game of Integer cards with Integer currency (money) type
    @SuppressWarnings("unchecked")
    private static final Game<Integer, Integer> game = (Game<Integer, Integer>) Game.getInstance();

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
        game.setScreen(List.of( List.of(1, 1, 0, 1, 5),
                                List.of(7, 1, 0, 1, 5),
                                List.of(4, 7, 1, 7, 0)
        ));
    }

    private void ThenShouldWin100FromLinesAnd500FromScatters() {
        Integer winAmount = game.calculateWinAmount();
    }

}
