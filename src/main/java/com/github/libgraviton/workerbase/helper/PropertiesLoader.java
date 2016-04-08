package com.github.libgraviton.workerbase.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {

    private static final Logger LOG = LoggerFactory.getLogger(PropertiesLoader.class);

    private static final String DEFAULT_APPLICATION_PROPERTIES_PATH = "etc/app.properties";
    private static final String SYSTEM_PROPERTY = "propFile";

    private PropertiesLoader() {

    }

    public static Properties load() throws IOException {
        Properties properties = new Properties();
        // load defaults
        try (InputStream defaultProperties = PropertiesLoader.class.getClassLoader().getResourceAsStream("default.properties")) {
            properties.load(defaultProperties);
        }

        // overrides?
        String propertiesPath = System.getProperty(SYSTEM_PROPERTY);
        if (propertiesPath == null) {
            propertiesPath = DEFAULT_APPLICATION_PROPERTIES_PATH;
        }

        try (FileInputStream appProperties = new FileInputStream(propertiesPath)) {
            properties.load(appProperties);
        } catch (IOException e) {
            LOG.debug("No overriding properties found at '" + propertiesPath + "'.");
        }

        // let system properties override everything..
        properties.putAll(System.getProperties());

        LOG.info("Loaded app.properties from " + propertiesPath);
        return properties;
    }
}
