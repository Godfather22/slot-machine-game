package com.amusnet;

import com.amusnet.game.Game;
import com.amusnet.game.impl.NumberCard;
import com.amusnet.game.Screen;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

public class GameBehaviourTest {

    // Testing a game of Integer cards with Integer currency (money) type
    @SuppressWarnings("unchecked")
    private static final Game<NumberCard, Integer> game = (Game<NumberCard, Integer>) Game.getInstance();

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
        Mockito.when(game.generateScreen()).thenReturn(screenAsList);
    }

    private void ThenShouldWin100FromLinesAnd500FromScatters() {
        Integer winAmount = game.calculateWinAmount();
    }

}
