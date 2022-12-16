package com.github.libgraviton.workerbase.messaging.strategy.rabbitmq;

import com.github.libgraviton.workerbase.messaging.MessageAcknowledger;
import com.github.libgraviton.workerbase.messaging.consumer.Consumeable;
import com.github.libgraviton.workerbase.messaging.consumer.WorkerConsumer;
import com.github.libgraviton.workerbase.messaging.exception.CannotAcknowledgeMessage;
import com.github.libgraviton.workerbase.messaging.exception.CannotRegisterConsumeable;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Wraps an instance of {@link WorkerConsumer} in order to consume from an AMQP RabbitMQ queue. Moreover, this consumer does
 * also connection recovery if an com.github.libgraviton.workerbase.messaging.exception on the channel occurred (exception.g. remote channel close).
 */
class RabbitMqConsumer extends DefaultConsumer implements MessageAcknowledger {

    static final private boolean ACK_PREV_MESSAGES = false;

    static final private boolean NACK_REDELIVER = true;

    static final private Logger LOG = LoggerFactory.getLogger(RabbitMqConsumer.class);

    private final RabbitMqConnection connection;

    private final WorkerConsumer consumer;

    RabbitMqConsumer(RabbitMqConnection connection, Consumeable consumeable) {
        super(connection.getChannel());
        this.consumer = new WorkerConsumer(consumeable, this);
        this.connection = connection;
    }

    public int getPrefetchCount() {
        return 1;
    }

    @Override
    public void handleDelivery(
            String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body
    ) throws IOException {
        final String deliveryTag = String.valueOf(envelope.getDeliveryTag());
        final String message = new String(body, StandardCharsets.UTF_8);
        LOG.info(
                "Message '{}' received on queue '{}': '{}'",
                deliveryTag,
                connection.getConnectionName(),
                message
        );
        consumer.consume(deliveryTag, message);
    }

    @Override
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
        LOG.warn(String.format("Lost connection to message queue '%s'.", connection.getConnectionName()));
        // "Automatic recovery only covers TCP connectivity issues and server-sent connection.close. It does not try to
        // recover channels that were closed due to a channel exception or an application-level exception, by design."
        // - RabbitMQ Documentation
        // So we need to recover channel closings only.
        if(sig.getReference() instanceof Channel) {
            LOG.info("Recovering connection to queue '{}'...", connection.getConnectionName());
            connection.close();
            try {
                connection.consume(consumer.getConsumeable());
            } catch (CannotRegisterConsumeable e) {
                LOG.error("Connection recovery for queue '{}' failed.", connection.getConnectionName());
            }
        }
    }

    @Override
    public void acknowledge(String messageId) throws CannotAcknowledgeMessage {
        try {
            getChannel().basicAck(Long.parseLong(messageId), ACK_PREV_MESSAGES);
            LOG.debug("Reported basicAck to message queue with delivery tag '{}'.", messageId);
        } catch (IOException e) {
            throw new CannotAcknowledgeMessage(this, messageId, e);
        }
    }

    @Override
    public void acknowledgeFail(String messageId) throws CannotAcknowledgeMessage {
        try {
            getChannel().basicNack(Long.parseLong(messageId), ACK_PREV_MESSAGES, NACK_REDELIVER);
            LOG.debug("Reported basicNack to message queue with delivery tag '{}' and redeliver = {}.", messageId, NACK_REDELIVER);
        } catch (IOException e) {
            throw new CannotAcknowledgeMessage(this, messageId, e);
        }
    }
}
