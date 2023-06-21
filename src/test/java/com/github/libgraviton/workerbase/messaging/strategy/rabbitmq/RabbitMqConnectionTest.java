package com.github.libgraviton.workerbase.messaging.strategy.rabbitmq;

import com.github.libgraviton.workerbase.messaging.exception.CannotConnectToQueue;
import com.github.libgraviton.workerbase.messaging.exception.CannotPublishMessage;
import com.rabbitmq.client.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.*;

public class RabbitMqConnectionTest {

    private RabbitMqConnection connection;

    private Channel rabbitChannel;

    private Connection rabbitConnection;

    private ConnectionFactory rabbitFactory;

    @BeforeEach
    public void setUp() throws Exception{
        rabbitConnection = mock(Connection.class);

        AMQP.Queue.DeclareOk declareOk = mock(AMQP.Queue.DeclareOk.class);
        doReturn("generated-queue-name").when(declareOk).getQueue();

        rabbitChannel = mock(Channel.class);
        doReturn(rabbitChannel).when(rabbitConnection).createChannel();
        doReturn(true).when(rabbitChannel).isOpen();
        doReturn(declareOk).when(rabbitChannel).queueDeclare();
        doReturn(true).when(rabbitConnection).isOpen();

        rabbitFactory = mock(ConnectionFactory.class);
        doReturn(rabbitConnection).when(rabbitFactory).newConnection();

        connection = new RabbitMqConnection.Builder()
                .exchangeName("exchange")
                .routingKey("routingKey")
                .queueName("queue")
                .connectionAttempts(1)
                .connectionFactory(rabbitFactory)
                .build();
        connection = spy(connection);
    }

    @AfterEach
    public void tearDown() {
        connection.close();
    }

    @Test
    public void testIsOpen() throws Exception {
        Assertions.assertFalse(connection.isOpen());
        connection.open();
        Assertions.assertTrue(connection.isOpen());
        connection.close();
        Assertions.assertFalse(connection.isOpen());
    }

    @Test
    public void testConnectionName() {
        Assertions.assertEquals("exchange - queue", connection.getConnectionName());
    }

    @Test
    public void testConnect() throws Exception {
        final boolean queueDurable= true;
        final boolean queueExclusive = false;
        final boolean queueAutoDelete = false;
        final boolean exchangeDurable = false;

        connection.open();

        verify(rabbitChannel).queueDeclare("queue", queueDurable, queueExclusive, queueAutoDelete, null);
        verify(rabbitChannel).exchangeDeclare("exchange", "direct", exchangeDurable);
        verify(rabbitChannel).queueBind("queue", "exchange", "routingKey");
    }

    @Test
    public void testConnectGeneratedQueue() throws Exception {
        connection = new RabbitMqConnection.Builder().queueName(null).connectionFactory(rabbitFactory).build();

        connection.open();

        verify(rabbitChannel).queueDeclare();
    }


    @Test
    public void testCustomConfig() throws Exception {
        final boolean queueDurable= false;
        final boolean queueExclusive = true;
        final boolean queueAutoDelete = true;
        final boolean exchangeDurable = true;

        connection = new RabbitMqConnection.Builder()
                .queueName("custom-queue")
                .queueAutoDelete(queueAutoDelete)
                .queueDurable(queueDurable)
                .queueExclusive(queueExclusive)
                .exchangeName("custom-exchange")
                .exchangeType("topic")
                .exchangeDurable(exchangeDurable)
                .routingKey("custom-routing-key")
                .connectionAttempts(1)
                .connectionFactory(rabbitFactory)
                .build();

        connection = spy(connection);

        connection.open();
        verify(rabbitChannel).queueDeclare("custom-queue", queueDurable, queueExclusive, queueAutoDelete, null);
        verify(rabbitChannel).exchangeDeclare("custom-exchange", "topic", exchangeDurable);
        verify(rabbitChannel).queueBind("custom-queue", "custom-exchange", "custom-routing-key");
    }
    
    @Test
    public void testConnectDefaultExchange() throws Exception {
        connection = new RabbitMqConnection.Builder()
                .queueName("queue")
                .exchangeName(null)
                .connectionFactory(rabbitFactory)
                .connectionAttempts(1)
                .build();
        connection = spy(connection);

        connection.open();
        verify(rabbitChannel).queueDeclare("queue", true, false, false, null);
        verify(rabbitChannel, never()).exchangeDeclare(anyString(), anyString(), anyBoolean());
        verify(rabbitChannel, never()).queueBind(anyString(), anyString(), anyString());
    }

    @Test
    public void testPublishTextMessage() throws Exception {
        connection.publish("gugus");
        verify(rabbitChannel).basicPublish(
                "exchange",
                "routingKey",
                MessageProperties.PERSISTENT_TEXT_PLAIN,
                "gugus".getBytes(StandardCharsets.UTF_8)
        );
    }

    @Test
    public void testPublishTextMessageFailed() throws Exception {
        doThrow(new IOException()).when(rabbitChannel).basicPublish(
                "exchange",
                "routingKey",
                MessageProperties.PERSISTENT_TEXT_PLAIN,
                "gugus".getBytes(StandardCharsets.UTF_8)
        );

        Assertions.assertThrows(CannotPublishMessage.class, () -> {
            connection.publish("gugus");
        });
    }

    @Test
    public void testPublishBytesMessage() throws Exception {
        byte[] bytesMessage = new byte[]{1,2,3,4};
        connection.publish(bytesMessage);
        verify(rabbitChannel).basicPublish(
                "exchange",
                "routingKey",
                MessageProperties.PERSISTENT_TEXT_PLAIN,
                bytesMessage
        );
    }

    @Test
    public void testPublishBytesMessageFailed() throws Exception {
        byte[] bytesMessage = new byte[]{1,2,3,4};

        doThrow(new IOException()).when(rabbitChannel).basicPublish(
                "exchange",
                "routingKey",
                MessageProperties.PERSISTENT_TEXT_PLAIN,
                bytesMessage
        );

        Assertions.assertThrows(CannotPublishMessage.class, () -> {
            connection.publish(bytesMessage);
        });
    }

    @Test
    public void testCloseConnection() throws Exception {
        connection.open();
        verify(rabbitChannel, never()).close();
        verify(rabbitConnection, never()).close();
        connection.close();
        verify(rabbitChannel).close();
        verify(rabbitConnection).close();
    }

    @Test
    public void testCloseConnectionFailed() throws Exception {
        doThrow(new IOException()).when(rabbitChannel).close();

        connection.open();
        connection.close();

        // Expects no com.github.libgraviton.workerbase.messaging.exception. If any is thrown, this test would fail.
    }
}
