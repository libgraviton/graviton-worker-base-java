package com.github.libgraviton.workerbase.helper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertiesLoaderTest {

    @Test
    public void testEnvParsing() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("worker_hans", "dude");
        map.put("worker_entrY__subKey", "the.value__");

        Properties props = PropertiesLoader.load(map);

        Assertions.assertEquals("dude", props.getProperty("hans"));
        Assertions.assertEquals("the.value__", props.getProperty("entrY.subKey"));

        // and now try to add some from other files
        props = PropertiesLoader.addFromResource(props, "addedProps.properties", false);

        // should not be overridden!
        Assertions.assertEquals("dude", props.getProperty("hans"));
        Assertions.assertEquals("value", props.getProperty("addedProp"));

        // and now should be overridden!
        props = PropertiesLoader.addFromResource(props, "addedProps.properties", true);
        Assertions.assertEquals("OVERRIDEVALUE", props.getProperty("hans"));
    }
}
