package com.github.libgraviton.workerbase.mq;

import com.github.libgraviton.workerbase.PropertiesLoader;
import com.github.libgraviton.workerbase.WorkerAbstract;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

public class QueueManagerTest {

    private QueueManager queueManager;

    private Properties properties;

    private WorkerAbstract workerAbstract;

    @Before
    public void setup() throws Exception {
        properties = PropertiesLoader.loadProperties();
        workerAbstract = mock(WorkerAbstract.class);
    }

    @Test
    public void shouldInitializeQueueManager() {
        queueManager = new QueueManager(properties);
        assertEquals("document.core.app.*", queueManager.getBindKeys().get(0));
        assertEquals("graviton", queueManager.getExchangeName());
        assertEquals("aaaaaaaaaaaaaa", queueManager.getFactory().getHost());
        assertEquals(5672, queueManager.getFactory().getPort());
        assertEquals(1, queueManager.getRetryAfterSeconds());
    }

    @Test
    public void shouldTryConnectingToQueue() {
        queueManager = spy(new QueueManager(properties));
        QueueConnector queueConnector = spy(new QueueConnector());
        doCallRealMethod().doReturn(true).when(queueConnector).connect();
        doReturn(queueConnector).when(queueManager).getQueueConnector();

        verify(queueConnector, times(0)).connect();
        queueManager.connect(workerAbstract);
        verify(queueConnector, times(2)).connect();
    }
}
