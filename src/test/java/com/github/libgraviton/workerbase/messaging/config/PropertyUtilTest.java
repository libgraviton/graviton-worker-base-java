package com.github.libgraviton.workerbase.messaging.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.mockito.Mockito.spy;

public class PropertyUtilTest {

    private Properties properties;

    @BeforeEach
    public void setUp() {
        properties = spy(new Properties());
    }

    @Test
    public void testBooleanProperty() {
        properties.setProperty("boolean-true", "true");
        properties.setProperty("boolean-false", "false");

        Assertions.assertTrue(PropertyUtil.getBoolean(properties, "boolean-true", false));
        Assertions.assertTrue(PropertyUtil.getBoolean(properties, "boolean-true-inexistent", true));
        Assertions.assertFalse(PropertyUtil.getBoolean(properties, "boolean-false", true));
        Assertions.assertFalse(PropertyUtil.getBoolean(properties, "boolean-false-inexistent", false));
    }

    @Test
    public void testIntProperty() {
        properties.setProperty("int-0", "0");
        properties.setProperty("int-7", "7");

        Assertions.assertEquals(0, PropertyUtil.getInteger(properties, "int-0", 1));
        Assertions.assertEquals(0, PropertyUtil.getInteger(properties, "int-0-inexistent", 0));
        Assertions.assertEquals(7, PropertyUtil.getInteger(properties, "int-7", 1));
        Assertions.assertEquals(7, PropertyUtil.getInteger(properties, "int-7-inexistent", 7));
    }

    @Test
    public void testDoubleProperty() {
        properties.setProperty("double-0", "0.0");
        properties.setProperty("double-7", "7");

        Assertions.assertEquals(0.0, PropertyUtil.getDouble(properties, "double-0", 1), 0);
        Assertions.assertEquals(0.0, PropertyUtil.getDouble(properties, "double-0-inexistent", 0), 0);
        Assertions.assertEquals(7.0, PropertyUtil.getDouble(properties, "double-7", 1), 0);
        Assertions.assertEquals(7.0, PropertyUtil.getDouble(properties, "double-7-inexistent", 7), 0);
    }

}
