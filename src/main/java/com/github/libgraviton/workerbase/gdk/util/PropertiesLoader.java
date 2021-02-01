package com.github.libgraviton.workerbase.gdk.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * Loading properties (with the ability to overwrite values).
 */
public class PropertiesLoader {

    private static final String DEFAULT_PROPERTIES_PATH = "default-gdk.properties";

    private static final String OVERWRITE_PROPERTIES_PATH = "app.properties";

    private static final String SYSTEM_PROPERTY = "propFile";

    private static final String ENV_PREFIX = "worker_";

    /**
     * Loads the Properties in the following order (if a property entry ia already loaded, it will be overridden with the new value).
     * 1.) Default Properties (resource path)
     *     Minimal needed properties for the gdk
     *
     * 2.) Overwrite Properties (resource path)
     *     Usually projects that make use of the gdk library will define these properties.
     *
     * 3.) Overwrite Properties (system property path)
     *     Whenever the project needs to run as a jar file with an external properties file,
     *     it's required to pass the SYSTEM_PROPERTY key with the path to the properties file as value. (e.g. -DpropFile=/app.properties)
     *
     * 4.) System Properties
     *     Projects that use the gdk library could be deployed to several environments that require different property values.
     *     The easiest way at this point is to just redefine those properties as system properties.
     *
     * @return loaded Properties
     * @throws IOException whenever the properties from a given path could not be loaded
     */
    public static Properties load() throws IOException {
        Properties properties = new Properties();

        try (InputStream defaultProperties = com.github.libgraviton.workerbase.gdk.util.PropertiesLoader.class.getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES_PATH)) {
            properties.load(defaultProperties);
        }

        try (InputStream overwriteProperties = com.github.libgraviton.workerbase.gdk.util.PropertiesLoader.class.getClassLoader().getResourceAsStream(OVERWRITE_PROPERTIES_PATH)) {
            if (overwriteProperties != null) {
                properties.load(overwriteProperties);
            }
        }

        String systemPropertiesPath = System.getProperty(SYSTEM_PROPERTY);
        if (systemPropertiesPath != null) {
            try (InputStream overwriteProperties = new FileInputStream(systemPropertiesPath)) {
                properties.load(overwriteProperties);
            }
        }

        properties.putAll(System.getProperties());

        properties = addFromEnvironment(properties, System.getenv());

        return properties;
    }

    public static String load(String key) {
        try {
            Properties properties = load();
            return properties.getProperty(key);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load properties files.", e);
        }
    }

    /**
     * parses and adds stuff from a map (mostly the environment by default) and adds them as properties
     * @param properties
     * @param map
     * @return
     */
    public static Properties addFromEnvironment(Properties properties, Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getKey().startsWith(ENV_PREFIX)) {
                String propName = entry.getKey().substring(ENV_PREFIX.length());

                // replace "__" with "." for propname
                properties.put(propName.replace("__", "."), entry.getValue());
            }
        }

        return properties;
    }
}
