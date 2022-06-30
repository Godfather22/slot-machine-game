package com.amusnet;

import com.amusnet.game.Game;

public class Application {

    public static void main(String[] args) {
        new Game().setHistoryTracking(true).play();
        System.out.println("game over");
    }

}
