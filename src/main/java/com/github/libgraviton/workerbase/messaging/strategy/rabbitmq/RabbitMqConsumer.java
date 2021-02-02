package com.github.libgraviton.workerbase.messaging.strategy.rabbitmq;

import com.github.libgraviton.workerbase.messaging.MessageAcknowledger;
import com.github.libgraviton.workerbase.messaging.consumer.AcknowledgingConsumer;
import com.github.libgraviton.workerbase.messaging.consumer.Consumer;
import com.github.libgraviton.workerbase.messaging.exception.CannotAcknowledgeMessage;
import com.github.libgraviton.workerbase.messaging.exception.CannotRegisterConsumer;
import com.github.libgraviton.workerbase.messaging.strategy.rabbitmq.RabbitMqConnection;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Wraps an instance of {@link Consumer} in order to consume from an AMQP RabbitMQ queue. Moreover, this consumer does
 * also connection recovery if an com.github.libgraviton.workerbase.messaging.exception on the channel occurred (exception.g. remote channel close).
 *
 * If the wrapped {@link Consumer} is an {@link AcknowledgingConsumer}, this class will receive the acknowledgment and
 * do the basicAck on the queue.
 */
class RabbitMqConsumer extends DefaultConsumer implements MessageAcknowledger {

    static final private boolean ACK_PREV_MESSAGES = false;

    static final private Logger LOG = LoggerFactory.getLogger(com.github.libgraviton.workerbase.messaging.strategy.rabbitmq.RabbitMqConsumer.class);

    private RabbitMqConnection connection;

    private Consumer consumer;

    RabbitMqConsumer(RabbitMqConnection connection, Consumer consumer) {
        super(connection.getChannel());
        this.consumer = consumer;
        this.connection = connection;
    }

    RabbitMqConsumer(RabbitMqConnection connection, AcknowledgingConsumer consumer) {
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
                connection.getConnectionName(),
                message
        ));
        consumer.consume(String.valueOf(deliveryTag), message);
    }

    @Override
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
        LOG.warn(String.format("Lost connection to message queue '%s'.", connection.getConnectionName()));
        // "Automatic recovery only covers TCP connectivity issues and server-sent connection.close. It does not try to
        // recover channels that were closed due to a channel exception or an application-level exception, by design."
        // - RabbitMQ Documentation
        // So we need to recover channel closings only.
        if(sig.getReference() instanceof Channel) {
            LOG.info("Recovering connection to queue '%s'...", connection.getConnectionName());
            connection.close();
            try {
                connection.consume(consumer);
            } catch (CannotRegisterConsumer e) {
                LOG.error("Connection recovery for queue '%s' failed.", connection.getConnectionName());
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

    Consumer getConsumer() {
        return consumer;
    }
}
