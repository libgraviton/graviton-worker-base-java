package com.github.libgraviton.workerbase.mq.strategy.rabbitmq.direct;

import com.github.libgraviton.workerbase.mq.AcknowledgingConsumer;
import com.github.libgraviton.workerbase.mq.Consumer;
import com.github.libgraviton.workerbase.mq.MessageAcknowledger;
import com.github.libgraviton.workerbase.mq.exception.CannotAcknowledgeMessage;
import com.github.libgraviton.workerbase.mq.exception.CannotConnectToQueue;
import com.github.libgraviton.workerbase.mq.exception.CannotRegisterConsumer;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class RabbitMqConsumer extends DefaultConsumer implements MessageAcknowledger {

    final private boolean ACK_PREV_MESSAGES = false;

    final private Logger LOG = LoggerFactory.getLogger(getClass());

    private RabbitMqConnection connection;

    private Consumer consumer;

    public RabbitMqConsumer(RabbitMqConnection connection, Consumer consumer) {
        super(connection.getChannel());
        this.consumer = consumer;
        this.connection = connection;
    }

    public RabbitMqConsumer(RabbitMqConnection connection, AcknowledgingConsumer consumer) {
        this(connection, (Consumer) consumer);
        consumer.setAcknowledger(this);
    }

    @Override
    public void handleDelivery(
            String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body
    ) throws IOException {
        long deliveryTag = envelope.getDeliveryTag();
        String message = new String(body, StandardCharsets.UTF_8);
        LOG.info(String.format(
                "Message '%d' received on queue '%s': '%s'",
                deliveryTag,
                connection.getQueueName(),
                message
        ));
        consumer.consume(String.valueOf(deliveryTag), message);
        throw new IOException("test");
    }

    @Override
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
        LOG.warn(String.format("Lost connection to message queue '%s'.", connection.getQueueName()));
        // "Automatic recovery only covers TCP connectivity issues and server-sent connection.close. It does not try to
        // recover channels that were closed due to a channel exception or an application-level exception, by design."
        // - RabbitMQ Documentation
        // So we need to recover channel closings only.
        if(sig.getReference() instanceof Channel) {
            LOG.info("Recovering connection to queue '%s'...", connection.getQueueName());
            connection.close();
            try {
                connection.consume(consumer);
            } catch (CannotRegisterConsumer e) {
                LOG.error("Connection recovery for queue '%s' failed.", connection.getQueueName());
            }
        }
    }

    @Override
    public void acknowledge(String messageId) throws CannotAcknowledgeMessage {
        try {
            getChannel().basicAck(Long.parseLong(messageId), ACK_PREV_MESSAGES);
            LOG.debug(String.format("Reported basicAck to message queue with delivery tag '%s'.", messageId));
        } catch (IOException e) {
            throw new CannotAcknowledgeMessage(this, messageId, e);
        }
    }

    public Consumer getConsumer() {
        return consumer;
    }
}
