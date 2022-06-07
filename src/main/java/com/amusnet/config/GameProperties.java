package com.amusnet.config;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

// TODO constraints
@Slf4j
public class GameProperties {
    public static Properties properties;    // read-only

    static {
        properties = new Properties();
        try {
            properties.load(new InputStreamReader(new FileInputStream("src/main/resources/game.properties")));
        } catch (IOException e) {
            log.error("Error reading .properties file", e);
            throw new RuntimeException(e);
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}
