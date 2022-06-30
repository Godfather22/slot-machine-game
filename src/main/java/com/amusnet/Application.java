package com.amusnet;

import com.amusnet.game.Game;

public class Application {

    public static void main(String[] args) {
        new Game().play();
        System.out.println("game over");
    }

}
