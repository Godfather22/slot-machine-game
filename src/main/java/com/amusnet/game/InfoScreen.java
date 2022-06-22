package com.amusnet.game;

import com.amusnet.config.GameConfig;

public class InfoScreen {

    private final GameConfig config;
    private final GameState gameState;

    public InfoScreen(GameConfig config, GameState gameState) {
        this.config = config;
        this.gameState = gameState;
    }

    public GameState getGameState() {
        return gameState;
    }

    /**
     * Prompt the user for input with an informative message.
     */
    public void print() {
        var cf = config.getCurrencyFormat();
        System.out.printf("Balance: %s | Lines available: 1-%d | Bets per lines available: %s-%s%n",
                cf.format(gameState.getCurrentBalance()), config.getLines().size(),
                cf.format(1), cf.format(config.getBetLimit()));
        System.out.println("Please enter lines you want to play on and a bet per line: ");
    }
}
