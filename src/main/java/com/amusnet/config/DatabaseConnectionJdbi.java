package com.amusnet.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Container for the database connection data, using JDBI.
 */
public class DatabaseConnectionJdbi {

    private static volatile DatabaseConnectionJdbi instance;
    private final org.jdbi.v3.core.Jdbi jdbi;

    private DatabaseConnectionJdbi() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream("src/main/resources/db.properties"));
        jdbi = org.jdbi.v3.core.Jdbi.create
                (properties.getProperty("url"), properties);
    }

    /**
     * Fetch the singleton instance of the container.
     *
     * @return The singleton instance of the jdbi connection container.
     */
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

    /**
     * Retrieve the jdbi main entry point to the database.
     *
     * @return Main entry point to database JDBI object
     */
    public org.jdbi.v3.core.Jdbi jdbi() {
        return this.jdbi;
    }

}
