package com.github.libgraviton.workerbase.messaging;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.mockito.Mockito.*;

public class QueueConnectionBuilderTest {

    private QueueConnection.Builder builder;

    private Properties properties;

    @Before
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
        assertEquals("testhost", builder.host);
        assertEquals(3333, builder.port);
        assertEquals("myuser", builder.user);
        assertEquals("mypass", builder.password);
        assertEquals("myqueuename", builder.queueName);
    }

    @Test
    public void testPrefixedProperties() {
        builder.applyProperties(properties, "context.");
        verify(properties, times(7)).getProperty(matches("^context\\..*$"));

        assertEquals("context.testhost", builder.host);
        assertEquals(6666, builder.port);
        assertEquals("context.myuser", builder.user);
        assertEquals("context.mypass", builder.password);
        assertEquals("context.myqueuename", builder.queueName);
    }
}
