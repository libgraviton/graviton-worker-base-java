package com.github.libgraviton.workerbase.mq.strategy.rabbitmq.direct;

import com.github.libgraviton.workerbase.mq.AcknowledgingConsumer;
import com.github.libgraviton.workerbase.mq.Consumer;
import com.github.libgraviton.workerbase.mq.QueueConnection;
import com.github.libgraviton.workerbase.mq.exception.CannotConnectToQueue;
import com.github.libgraviton.workerbase.mq.exception.CannotPublishMessage;
import com.github.libgraviton.workerbase.mq.exception.CannotRegisterConsumer;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class RabbitMqConnection extends QueueConnection {

    private final Map<String, Object> QUEUE_ARGS = null;

    private boolean queueDurable = true;

    private boolean queueExclusive = false;

    private boolean queueAutoAck = false;

    private ConnectionFactory connectionFactory;

    private Connection connection;

    private Channel channel;

    private String exchange;

    private boolean exchangeDurable = false;

    private String exchangeType = "direct";

    private String routingKey;

    public RabbitMqConnection(
            String queueName,
            String exchange,
            String routingKey,
            ConnectionFactory connectionFactory
    ) {
        super(queueName);
        this.connectionFactory = connectionFactory;
        this.exchange = exchange;
        this.routingKey = routingKey;
        connectionFactory.setAutomaticRecoveryEnabled(true);
    }

    public Connection getConnection() {
        return connection;
    }

    public Channel getChannel() {
        return channel;
    }

    public String getExchange() {
        return exchange;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setQueueDurable(boolean queueDurable) {
        this.queueDurable = queueDurable;
    }

    public void setQueueExclusive(boolean queueExclusive) {
        this.queueExclusive = queueExclusive;
    }

    public void setQueueAutoAck(boolean queueAutoAck) {
        this.queueAutoAck = queueAutoAck;
    }

    public void setExchangeDurable(boolean exchangeDurable) {
        this.exchangeDurable = exchangeDurable;
    }

    public void setExchangeType(String exchangeType) {
        this.exchangeType = exchangeType;
    }

    @Override
    public boolean isOpen() {
        return connection != null && connection.isOpen();
    }

    @Override
    public void publish(String message) throws CannotPublishMessage {
        try {
            open();
            channel.basicPublish(
                    exchange,
                    routingKey,
                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                    message.getBytes(StandardCharsets.UTF_8)
            );
        } catch (IOException | CannotConnectToQueue e) {
            throw new CannotPublishMessage(message, e);
        } finally {
            close();
        }
    }

    public void consume(Consumer consumer) throws CannotRegisterConsumer {
        consume(new RabbitMqConsumer(channel, consumer), true);
    }

    public void consume(AcknowledgingConsumer consumer) throws CannotRegisterConsumer {
        RabbitMqConsumer rabbitMqConsumer = new RabbitMqConsumer(channel, consumer);
        consumer.setAcknowledger(rabbitMqConsumer);
        consume(rabbitMqConsumer, false);
    }

    private void consume(RabbitMqConsumer rabbitMqConsumer, boolean autoAck) throws CannotRegisterConsumer {
        try {
            open();
            channel.basicConsume(queueName, autoAck, rabbitMqConsumer);
        } catch (CannotConnectToQueue | IOException e) {
            throw new CannotRegisterConsumer(rabbitMqConsumer.getConsumer(), e);
        }
    }

    @Override
    protected void openConnection() throws CannotConnectToQueue {
        try {
            connection = connectionFactory.newConnection();
            channel = connection.createChannel();
            // Use specific exchange if defined, otherwise use default exchange
            if (null != exchange) {
                channel.exchangeDeclare(exchange, exchangeType, exchangeDurable);
            }
            channel.queueDeclare(queueName, queueDurable, queueExclusive, queueAutoAck, QUEUE_ARGS);
            channel.queueBind(queueName, exchange, routingKey);
        } catch (IOException | TimeoutException e) {
            throw new CannotConnectToQueue(String.format(
                "Unable to open connection to queue '%s': '%s'",
                queueName,
                e.getCause().getMessage()
            ));
        }
    }

    @Override
    protected void closeConnection() throws CannotConnectToQueue {
        try {
            if (channel != null) {
                channel.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (IOException | TimeoutException e) {
            throw new CannotConnectToQueue(
                String.format("Cannot close connection to queue '%s'.", queueName),
                e
            );
        }
    }
}
