package com.github.libgraviton.workerbase.mq.strategy.rabbitmq.direct;

import com.github.libgraviton.workerbase.mq.AcknowledgingConsumer;
import com.github.libgraviton.workerbase.mq.Consumer;
import com.github.libgraviton.workerbase.mq.MessageAcknowledger;
import com.github.libgraviton.workerbase.mq.exception.CannotAcknowledgeMessage;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class RabbitMqConsumer extends DefaultConsumer implements MessageAcknowledger {

    final private boolean ACK_PREV_MESSAGES = false;

    private Consumer consumer;

    final private Logger LOG = LoggerFactory.getLogger(getClass());

    public RabbitMqConsumer(Channel channel, Consumer consumer) {
        super(channel);
        this.consumer = consumer;
    }

    public RabbitMqConsumer(Channel channel, AcknowledgingConsumer consumer) {
        this(channel, (Consumer) consumer);
        consumer.setAcknowledger(this);
    }

    @Override
    public void handleDelivery(
            String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body
    ) throws IOException {
        long deliveryTag = envelope.getDeliveryTag();
        String message = new String(body, StandardCharsets.UTF_8);
        LOG.info(String.format("Received '%d': '%s'", deliveryTag, message));
        consumer.consume(String.valueOf(deliveryTag), message);
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
