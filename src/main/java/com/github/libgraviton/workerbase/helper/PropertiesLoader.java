package com.github.libgraviton.workerbase.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * Load Properties with fallback mechanism
 *
 * @author List of contributors
 *         https://github.com/libgraviton/graviton/graphs/contributors
 * @see <a href="http://swisscom.chm">http://swisscom.ch</a>
 */
public class PropertiesLoader {

    private static final Logger LOG = LoggerFactory.getLogger(PropertiesLoader.class);

    private static final String DEFAULT_PROPERTIES_PATH = "default.properties";

    private static final String OVERWRITE_PROPERTIES_PATH = "app.properties";

    private static final String OVERWRITE_PROPERTIES_JAR_PATH = "app_jar.properties";

    private static final String SYSTEM_PROPERTY = "propFile";

    private static final String ENV_PREFIX = "worker_";

    public static Properties load() throws IOException {
        return load(PropertiesLoader.class, System.getenv());
    }

    public static Properties load(Object initClass) throws IOException {
        return load(initClass, System.getenv());
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
    public static Properties load(Object initClass, Map<String, String> env) throws IOException {
        Properties properties = new Properties();

        try (InputStream defaultProperties = PropertiesLoader.class.getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES_PATH)) {
            LOG.info("Loading default properties from file '{}'", DEFAULT_PROPERTIES_PATH);
            properties.load(defaultProperties);
        }

        try (InputStream overwriteProperties = PropertiesLoader.class.getClassLoader().getResourceAsStream(OVERWRITE_PROPERTIES_PATH)) {
            if (overwriteProperties != null) {
                LOG.info("Loading overwrite properties from file '{}'", OVERWRITE_PROPERTIES_PATH);
                properties.load(overwriteProperties);
            }
        }

        String systemPropertiesPath = System.getProperty(SYSTEM_PROPERTY);
        if (systemPropertiesPath != null) {
            try (InputStream overwriteProperties = new FileInputStream(systemPropertiesPath)) {
                LOG.info("Loading system properties from file '{}'", SYSTEM_PROPERTY);
                properties.load(overwriteProperties);

            }
        }

        try (InputStream overwriteJarProperties = PropertiesLoader.class.getClassLoader().getResourceAsStream(OVERWRITE_PROPERTIES_JAR_PATH)) {
            if (overwriteJarProperties != null && WorkerUtil.isJarContext(initClass)) {
                LOG.info("Loading JAR runtime default properties from file '{}'", OVERWRITE_PROPERTIES_JAR_PATH);
                properties.load(overwriteJarProperties);
            }
        }

        LOG.info("Loading system properties (command line args)");
        properties.putAll(System.getProperties());

        LOG.info("Loading from ENV");
        addFromEnvironment(properties, env);

        return properties;
    }

    /**
     * parses and adds stuff from a map (mostly the environment by default) and adds them as properties
     * @param properties
     * @param map
     * @return
     */
    public static void addFromEnvironment(Properties properties, Map<String, String> map) {
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
    public static Properties addFromResource(Properties properties, String resourcePath, Boolean doOverride) throws Exception {
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
