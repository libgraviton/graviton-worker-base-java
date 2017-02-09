package com.github.libgraviton.workerbase.mq.strategy.jms;

import com.github.libgraviton.workerbase.mq.AcknowledgingConsumer;
import com.github.libgraviton.workerbase.mq.Consumer;
import com.github.libgraviton.workerbase.mq.MessageAcknowledger;
import com.github.libgraviton.workerbase.mq.exception.CannotAcknowledgeMessage;
import com.github.libgraviton.workerbase.mq.exception.CannotConsumeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.jms.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * Wraps an instance of {@link Consumer} in order to consume from a JMS based queue.
 *
 * Moreover, this class does also the automatic message acknowledgment after the
 * {@link Consumer#consume(String, String)} terminated. Except if the wrapped {@link Consumer} is an
 * {@link AcknowledgingConsumer}, it will do the JMS acknowledgment as soon as it receives the acknowledgment from the
 * wrapped consumer.
 */
class JmsConsumer implements MessageListener, MessageAcknowledger {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private Consumer consumer;

    private HashMap<String, Message> messages;

    JmsConsumer(Consumer consumer) {
        this.consumer = consumer;
        messages = new HashMap<>();
    }

    @Override
    public void onMessage(Message jmsMessage) {
        LOG.debug(String.format("Received message of type '%s' from queue.", jmsMessage.getClass().getName()));
        String message;
        try {
            String messageId = jmsMessage.getJMSMessageID();
            messages.put(messageId, jmsMessage);
            if (jmsMessage instanceof TextMessage) {
                message = ((TextMessage) jmsMessage).getText();
            } else if (jmsMessage instanceof BytesMessage) {
                message = extractBody((BytesMessage) jmsMessage);
            } else {
                LOG.warn(String.format(
                        "Message of type '%s' cannot be handled and got ignored.",
                        jmsMessage.getClass().getName()
                ));
                return;
            }
            consumer.consume(jmsMessage.getJMSMessageID(), message);
            if (!(consumer instanceof AcknowledgingConsumer)) {
                acknowledge(messageId);
            }
        } catch (JMSException | CannotConsumeMessage e) {
            LOG.error("Could not process feedback message.", e);
        } catch (Exception e) {
            // Catch exception to avoid endless loop because the message will trigger 'onMessage' again and again.
            LOG.error("Unexpected error occurred while processing queue feedback message.", e);
        }

    }

    @Override
    public void acknowledge(String messageId) throws CannotAcknowledgeMessage {
        Message jmsMessage = messages.get(messageId);
        if (null == jmsMessage) {
            throw new CannotAcknowledgeMessage(
                    this,
                    messageId,
                    String.format("Message with id '%s' is unknown.", messageId)
            );
        }
        try {
            jmsMessage.acknowledge();
        } catch (JMSException e) {
            throw new CannotAcknowledgeMessage(this, messageId, e);
        } finally {
            messages.remove(messageId);
        }
    }

    String extractBody(BytesMessage message) throws JMSException {
        byte[] messageBytes = new byte[(int) message.getBodyLength()];
        message.readBytes(messageBytes);
        return new String(messageBytes, StandardCharsets.UTF_8);
    }
}
