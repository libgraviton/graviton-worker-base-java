package com.github.libgraviton.workerbase.helper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class WorkerPropertiesTest {

    @BeforeAll
    static void setup() throws IOException {
        WorkerProperties.load();
    }

    @Test
    public void testHandling() {
        // was set in test scope
        Assertions.assertEquals("dude", WorkerProperties.getProperty("addition"));
    }

}
