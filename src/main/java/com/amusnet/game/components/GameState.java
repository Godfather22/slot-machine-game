package com.amusnet.game.components;

/**
 * Represents the state of the Game at a given time, i.e. the balance amount and the bets made.
 */
public class GameState {
    private double currentBalance;
    private final GameRound gameRound;

    public GameState(double currentBalance, GameRound gameRound) {
        this.currentBalance = currentBalance;
        this.gameRound = gameRound;
    }

    public double getCurrentBalance() {
        return currentBalance;
    }

    public void addToBalance(double amount) {
        this.currentBalance += amount;
    }

    public void subtractFromBalance(double amount) {
        this.currentBalance -= amount;
    }

    public GameRound getGameRound() {
        return gameRound;
    }
}
