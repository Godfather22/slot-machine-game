package com.amusnet.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DatabaseConnectionJdbi {

    private static volatile DatabaseConnectionJdbi instance;
    private final org.jdbi.v3.core.Jdbi jdbi;

    private DatabaseConnectionJdbi() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream("src/main/resources/db.properties"));
        jdbi = org.jdbi.v3.core.Jdbi.create
                (properties.getProperty("url"), properties);
    }

    public static DatabaseConnectionJdbi getInstance() {
        DatabaseConnectionJdbi result = instance;
        if (result != null)
            return result;
        synchronized (DatabaseConnectionJdbi.class) {
            if (instance == null) {
                try {
                    instance = new DatabaseConnectionJdbi();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return instance;
        }
    }

    public org.jdbi.v3.core.Jdbi jdbi() {
        return this.jdbi;
    }

}
