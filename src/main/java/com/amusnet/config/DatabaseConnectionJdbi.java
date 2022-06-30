package com.amusnet.config;

public class DatabaseConnectionJdbi {

    private static volatile DatabaseConnectionJdbi instance;
    private final org.jdbi.v3.core.Jdbi jdbi;

    private DatabaseConnectionJdbi() {
        jdbi = org.jdbi.v3.core.Jdbi.create
                ("jdbc:mysql://localhost:3306/reel_game?createDatabaseIfNotExist=true",
                        "root", "root");
    }

    public static DatabaseConnectionJdbi getInstance() {
        DatabaseConnectionJdbi result = instance;
        if (result != null)
            return result;
        synchronized (DatabaseConnectionJdbi.class) {
            if (instance == null)
                instance = new DatabaseConnectionJdbi();
            return instance;
        }
    }

    public org.jdbi.v3.core.Jdbi jdbi() {
        return this.jdbi;
    }

}
