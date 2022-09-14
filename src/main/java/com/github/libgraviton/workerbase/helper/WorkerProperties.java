package com.github.libgraviton.workerbase.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * this is a singleton implementation/wrapper for properties handling
 * inside a worker. this was added as i found it frustrating that
 * 1) we pass around 'properties' mostly - but not always. still at
 * some points, static calls where made to PropertiesLoader. no clear concept.
 * 2) in tests; it is then very hard to set test specific properties
 * 3) accept both instance and static use - and still have the same properties
 */
public class WorkerProperties {

    private static final Logger LOG = LoggerFactory.getLogger(WorkerProperties.class);

    private static Properties loadedProperties = new Properties();
    private static boolean alreadyLoaded = false;

    private static final HashMap<String, String> propertyOverrides = new HashMap<>();

    private static class InnerProperties extends Properties {
        @Override
        public String getProperty(String name) {
            return WorkerProperties.getProperty(name);
        }
    }

    public static Properties load() throws IOException {
        if (!alreadyLoaded) {
            loadedProperties = PropertiesLoader.load();
            alreadyLoaded = true;
        }
        return new InnerProperties();
    }

    public static void addOverrides(Map<String, String> map) {
        propertyOverrides.putAll(map);
    }

    public static void setOverride(String name, String value) {
        propertyOverrides.put(name, value);
    }

    public static void clearOverride(String name) {
        propertyOverrides.remove(name);
    }

    public static void clearOverrides() {
        propertyOverrides.clear();
    }

    public static String getProperty(String name) {
        if (!alreadyLoaded) {
            LOG.warn("Properties were called here without explicit call to WorkerProperties.load() before! " +
                    "This is discouraged as the error handling is disabled here. Please fix that. Continuing ignoring errors.");

            try {
                WorkerProperties.load();
            } catch (IOException e) {
                LOG.error("Error on explicit loading properties", e);
            }
        }

        return propertyOverrides.getOrDefault(
                name,
                loadedProperties.getProperty(name)
        );
    }
}
