package com.github.libgraviton.workerbase.mq;

import com.github.libgraviton.workerbase.mq.exception.CannotConnectToQueue;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class WorkerQueueManagerTest {

    private WorkerQueueConnector queueConnector = new WorkerQueueConnector();

    private ConnectionFactory connectionFactory;


    @Before
    public void setup() throws Exception {
        queueConnector.setQueueName("testQueueName");
    }

    @Test(expected = CannotConnectToQueue.class)
    public void shouldConnectWithoutConnectionThenReturnException() throws CannotConnectToQueue {
        connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("queue.host");
        connectionFactory.setPort(1234);
        connectionFactory.setUsername("queue.username");
        connectionFactory.setPassword("queue.password");
        connectionFactory.setVirtualHost("queue.vhost");

        queueConnector.setFactory(connectionFactory);

        queueConnector.connect();
    }

    @Test
    public void shouldConnect() throws Exception {
        connectionFactory = mock(ConnectionFactory.class);
        Connection connection = mock(Connection.class);
        Channel channel = mock(Channel.class, RETURNS_DEEP_STUBS);
        when(channel.queueDeclare().getQueue()).thenReturn("omg");
        when(connection.createChannel()).thenReturn(channel);
        when(connectionFactory.newConnection()).thenReturn(connection);

        queueConnector.setFactory(connectionFactory);
        queueConnector.connect();
    }
}
