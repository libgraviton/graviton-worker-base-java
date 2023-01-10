package com.github.libgraviton.workerbase.messaging;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.mockito.Mockito.*;

public class QueueConnectionBuilderTest {

    private QueueConnection.Builder builder;

    private Properties properties;

    @BeforeEach
    public void setUp() {
        builder = mock(QueueConnection.Builder.class, CALLS_REAL_METHODS);
        properties = new Properties();
        properties.setProperty("host", "testhost");
        properties.setProperty("port", "3333");
        properties.setProperty("user", "myuser");
        properties.setProperty("password", "mypass");
        properties.setProperty("queue.name", "myqueuename");

        properties.setProperty("context.host", "context.testhost");
        properties.setProperty("context.port", "6666");
        properties.setProperty("context.user", "context.myuser");
        properties.setProperty("context.password", "context.mypass");
        properties.setProperty("context.queue.name", "context.myqueuename");

        properties = spy(properties);
        doCallRealMethod().when(properties).getProperty(anyString(), anyString());
    }

    @Test
    public void testProperties() {
        builder.applyProperties(properties);
        Assertions.assertEquals("testhost", builder.host);
        Assertions.assertEquals(3333, builder.port);
        Assertions.assertEquals("myuser", builder.user);
        Assertions.assertEquals("mypass", builder.password);
        Assertions.assertEquals("myqueuename", builder.queueName);
    }

    @Test
    public void testPrefixedProperties() {
        builder.applyProperties(properties, "context.");
        verify(properties, times(7)).getProperty(matches("^context\\..*$"));

        Assertions.assertEquals("context.testhost", builder.host);
        Assertions.assertEquals(6666, builder.port);
        Assertions.assertEquals("context.myuser", builder.user);
        Assertions.assertEquals("context.mypass", builder.password);
        Assertions.assertEquals("context.myqueuename", builder.queueName);
    }
}
