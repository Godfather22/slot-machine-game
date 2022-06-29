package com.amusnet.config;

public class Jdbi {

    private static volatile Jdbi instance;
    private final org.jdbi.v3.core.Jdbi jdbi;

    private Jdbi() {
        jdbi = org.jdbi.v3.core.Jdbi.create("jdbc:mysql://localhost:3306/reel_game?createDatabaseIfNotExist=true",
                "root", "root");
    }

    public static Jdbi getInstance() {
        Jdbi result = instance;
        if (result != null)
            return result;
        synchronized (Jdbi.class) {
            if (instance == null)
                instance = new Jdbi();
            return instance;
        }
    }

    public org.jdbi.v3.core.Jdbi jdbi() {
        return this.jdbi;
    }

}
