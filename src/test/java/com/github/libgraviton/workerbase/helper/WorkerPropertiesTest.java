package com.github.libgraviton.workerbase.helper;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class WorkerPropertiesTest {

    @BeforeClass
    static void setup() throws IOException {
        WorkerProperties.load();
    }

    @Test
    public void testHandling() throws Exception {
        // was set in test scope
        assertEquals("dude", WorkerProperties.getProperty("addition"));
    }

}
