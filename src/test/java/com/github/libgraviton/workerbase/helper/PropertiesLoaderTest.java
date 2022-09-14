package com.github.libgraviton.workerbase.helper;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.*;

public class PropertiesLoaderTest {

    @Test
    public void testEnvParsing() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("worker_hans", "dude");
        map.put("worker_entrY__subKey", "the.value__");

        Properties props = PropertiesLoader.load(map);

        assertEquals("dude", props.getProperty("hans"));
        assertEquals("the.value__", props.getProperty("entrY.subKey"));

        // and now try to add some from other files
        props = PropertiesLoader.addFromResource(props, "addedProps.properties", false);

        // should not be overridden!
        assertEquals("dude", props.getProperty("hans"));
        assertEquals("value", props.getProperty("addedProp"));

        // and now should be overridden!
        props = PropertiesLoader.addFromResource(props, "addedProps.properties", true);
        assertEquals("OVERRIDEVALUE", props.getProperty("hans"));
    }
}
