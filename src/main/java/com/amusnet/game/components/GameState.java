package com.amusnet.game.components;

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
