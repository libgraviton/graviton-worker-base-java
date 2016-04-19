package com.github.libgraviton.workerbase.mq;

import com.github.libgraviton.workerbase.WorkerAbstract;
import com.github.libgraviton.workerbase.helper.PropertiesLoader;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class WorkerQueueConnectorTest {

    private WorkerQueueManager queueManager;

    private WorkerAbstract worker;

    private Properties properties;

    @Before
    public void setup() throws Exception {
        properties = PropertiesLoader.load();

        worker = mock(WorkerAbstract.class);
        when(worker.getWorkerId()).thenReturn("java-test");
        queueManager = spy(new WorkerQueueManager(properties));
        queueManager.setWorker(worker);
    }

    @Test
    public void shouldInitializeQueueManager() {
        assertEquals("java-test", queueManager.getQueueName());
        assertEquals("aaaaaaaaaaaaaa", queueManager.getFactory().getHost());
        assertEquals(5672, queueManager.getFactory().getPort());
        assertEquals(1, queueManager.getRetryAfterSeconds());
    }

    @Test
    public void shouldInitializeQueueConnector() {
        WorkerQueueConnector queueConnector = (WorkerQueueConnector) queueManager.getQueueConnector();
        assertEquals("java-test",queueConnector.getQueueName());
        assertEquals(worker, queueConnector.getWorker());
        assertNotNull(queueConnector.getFactory());
        assertEquals(new Integer(1), queueConnector.getPrefetchCount());
    }

    @Test
    public void shouldTryConnectingToQueue() {
        WorkerQueueConnector queueConnector = spy(new WorkerQueueConnector());
        queueConnector.setRetryAfterSeconds(queueManager.getRetryAfterSeconds());
        queueConnector.setWorker(queueManager.getWorker());
        queueConnector.setFactory(queueManager.getFactory());
        queueConnector.setQueueName(queueManager.getQueueName());

        doCallRealMethod().doReturn(true).when(queueConnector).connectAttempt();
        doReturn(queueConnector).when(queueManager).getQueueConnector();

        verify(queueConnector, times(0)).connectAttempt();
        queueManager.connect();
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        verify(queueConnector, times(2)).connectAttempt();
    }
}
