package com.amusnet.game;

public class GameState {
    private double currentBalance;

    private double lastWinFromLines, lastWinFromScatters;

    public GameState(double currentBalance, double lastWinFromLines, double lastWinFromScatters) {
        this.currentBalance = currentBalance;
        this.lastWinFromLines = lastWinFromLines;
        this.lastWinFromScatters = lastWinFromScatters;
    }

    public double getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(double currentBalance) {
        this.currentBalance = currentBalance;
    }

    public double getLastWinFromLines() {
        return lastWinFromLines;
    }

    public void setLastWinFromLines(double lastWinFromLines) {
        this.lastWinFromLines = lastWinFromLines;
    }

    public double getLastWinFromScatters() {
        return lastWinFromScatters;
    }

    public void setLastWinFromScatters(double lastWinFromScatters) {
        this.lastWinFromScatters = lastWinFromScatters;
    }

    public void addToBalance(double amount) {
        this.currentBalance += amount;
    }

    public void subtractFromBalance(double amount) {
        this.currentBalance -= amount;
    }

}
