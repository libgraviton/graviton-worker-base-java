package com.github.libgraviton.workerbase;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {

    private PropertiesLoader() {

    }

    public static Properties loadProperties() throws IOException {
        Properties properties = new Properties();
        // load defaults
        try (InputStream defaultProperties = PropertiesLoader.class.getClassLoader().getResourceAsStream("default.properties")) {
            properties.load(defaultProperties);
        }

        return properties;
    }
}
