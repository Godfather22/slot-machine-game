package com.amusnet.game;

import com.amusnet.config.GameConfig;
import lombok.Data;
import org.javatuples.Pair;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Data
public class Game<C extends Card<?>, M extends Number> {

    private GameConfig<C, M> configuration;

    private M currentBalance;

    private Integer linesPlayed;
    private M betAmount;

    private List<List<C>> screen;

    private M lastWinFromLines, lastWinFromScatters;
    private boolean gameOver;

    // thread-safe singleton
    private Game() {
        this.screen = new CopyOnWriteArrayList<>();
        this.gameOver = false;
    }

    @SuppressWarnings("rawtypes")
    private static volatile Game instance;

    // instance is retrieved using double-checked locking (DCL)
    @SuppressWarnings("rawtypes")
    public static Game getInstance() {
        Game result = instance;
        if (result != null)
            return result;
        synchronized (Game.class) {
            if (instance == null)
                instance = new Game();
            return instance;
        }
    }

    public M calculateWinAmount() {
        return null;
//        for (var row : screen) {
//            for (C card : row) {
//                var lines = configuration.getLines();
//                for (int i = 0; i < lines.size(); i++) {
//                    Pair<C, Integer> occurs = getOccurrencesForLine(lines.get(i));
//                    // TODO finish algorithm
//
//                }
//            }
//        }
    }

    public void quit() {
        this.gameOver = true;
    }

    //*******************
    //* UTILITY METHODS *
    //*******************

    public void prompt() {
        System.out.printf("Balance: %s | Lines available: 1-%d | Bets per lines available: 1-%s%n",
                currentBalance, configuration.getLines().size(), configuration.getMaxBetAmount());
        System.out.println("Please enter lines you want to play on and a bet per line:");
    }

    private Pair<C, Integer> getOccurrencesForLine(List<C> line) {
        return null;
    }

}
