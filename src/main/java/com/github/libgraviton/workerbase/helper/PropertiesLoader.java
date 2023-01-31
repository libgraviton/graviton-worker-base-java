package com.github.libgraviton.workerbase.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Load Properties with fallback mechanism
 *
 * @author List of contributors
 *         https://github.com/libgraviton/graviton/graphs/contributors
 * @see <a href="http://swisscom.chm">http://swisscom.ch</a>
 */
class PropertiesLoader {

    private static final Logger LOG = LoggerFactory.getLogger(PropertiesLoader.class);

    private static final String ENV_PREFIX = "worker_";

    static Properties load() throws IOException {
        return load(System.getenv());
    }

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
    static Properties load(Map<String, String> env) throws IOException {
        Properties properties = new Properties();

        // do in this sequence
        Map<String, Integer> loadedSources = new LinkedHashMap<>();

        List<String> propertiesFileOrder = List.of(
                "workerbase.properties",
                "default.properties",
                "app.properties",
                "test.properties",
                "runtime.properties"
        );

        int startCounter = 0;
        for (String propertiesFile : propertiesFileOrder) {
            loadSingleFile(properties, propertiesFile);
            int addedCount = properties.size() - startCounter;
            if (addedCount > 0) {
                loadedSources.put(propertiesFile, addedCount);
            }
            startCounter = properties.size();
        }

        // Loading system properties (command line args)
        properties.putAll(System.getProperties());

        int addedCount = properties.size() - startCounter;
        if (addedCount > 0) {
            loadedSources.put("system", addedCount);
        }
        startCounter = properties.size();

        // environment
        addFromEnvironment(properties, env);

        addedCount = properties.size() - startCounter;
        if (addedCount > 0) {
            loadedSources.put("ENV", addedCount);
        }

        LOG.info(
                "Loaded a total of '{}' properties. Sources: {}",
                properties.size(),
                loadedSources
        );

        return properties;
    }

    private static void loadSingleFile(Properties properties, String propertiesFile) throws IOException {
        try (InputStream propertiesStream = PropertiesLoader.class.getClassLoader().getResourceAsStream(propertiesFile)) {
            if (propertiesStream != null && propertiesStream.available() > 0) {
                properties.load(propertiesStream);
            }
        }
    }

    /**
     * parses and adds stuff from a map (mostly the environment by default) and adds them as properties
     * @param properties
     * @param map
     * @return
     */
    static void addFromEnvironment(Properties properties, Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getKey().startsWith(ENV_PREFIX)) {
                String propName = entry.getKey().substring(ENV_PREFIX.length());

                // replace "__" with "." for propname
                properties.put(propName.replace("__", "."), entry.getValue());
            }
        }
    }

    /**
     * you can add additional resource files from the path, specifiying if exiting props should be overridden or not
     * @param properties
     * @param resourcePath
     * @param doOverride
     * @return
     */
    static Properties addFromResource(Properties properties, String resourcePath, boolean doOverride) throws Exception {
        InputStream is = PropertiesLoader.class.getClassLoader().getResourceAsStream(resourcePath);
        if (is == null) {
            throw new Exception("Could not load resource '"+resourcePath+"' to load in PropertiesLoader!");
        }

        LOG.info("Loaded from resource path '{}'", resourcePath);

        if (doOverride) {
            properties.load(is);
            return properties;
        }

        // no override? go through them
        Properties newProps = new Properties();
        newProps.load(is);

        for (String propName : newProps.stringPropertyNames()) {
            if (!properties.containsKey(propName)) {
                properties.put(propName, newProps.getProperty(propName));
            }
        }

        return properties;
    }
}
