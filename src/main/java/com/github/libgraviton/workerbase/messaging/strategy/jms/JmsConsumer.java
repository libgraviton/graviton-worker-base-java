package com.github.libgraviton.workerbase.messaging.strategy.jms;

import com.github.libgraviton.workerbase.messaging.MessageAcknowledger;
import com.github.libgraviton.workerbase.messaging.consumer.Consumeable;
import com.github.libgraviton.workerbase.messaging.consumer.WorkerConsumer;
import com.github.libgraviton.workerbase.messaging.exception.CannotAcknowledgeMessage;
import com.github.libgraviton.workerbase.messaging.exception.CannotConsumeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * Wraps an instance of {@link WorkerConsumer} in order to consume from a JMS based queue.
 *
 * Moreover, this class does also the automatic message acknowledgment after the
 * {@link WorkerConsumer#consume(String, String)} terminated. Except if the wrapped {@link WorkerConsumer}
 * wrapped consumer.
 */
class JmsConsumer implements MessageListener, MessageAcknowledger {

    private static final Logger LOG = LoggerFactory.getLogger(com.github.libgraviton.workerbase.messaging.strategy.jms.JmsConsumer.class);

    private final WorkerConsumer consumer;

    private final HashMap<String, Message> messages;

    JmsConsumer(Consumeable consumeable) {
        consumer = new WorkerConsumer(consumeable, this);
        // onMessage() can be called by several threads.
        messages = new HashMap<>();
    }

    @Override
    public void onMessage(Message jmsMessage) {
        LOG.debug("Received message of type '{}' from queue.", jmsMessage.getClass().getName());
        String message;
        String messageId = null;
        try {
            messageId = jmsMessage.getJMSMessageID();
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
        } catch (JMSException | CannotConsumeMessage e) {
            LOG.error("Could not process feedback message.", e);
        } catch (Exception e) {
            // Catch com.github.libgraviton.workerbase.messaging.exception to avoid endless loop because the message will trigger 'onMessage' again and again.
            LOG.error("Unexpected error occurred while processing queue feedback message.", e);
        } finally {
            try {
                acknowledge(messageId);
            } catch (CannotAcknowledgeMessage cam) {
                LOG.error(cam.getMessage());
            }
        }
    }

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

    public void acknowledgeFail(String messageId) {
        throw new RuntimeException("This acknowledge mode is not enabled in this module");
    }

    String extractBody(BytesMessage message) throws JMSException {
        byte[] messageBytes = new byte[(int) message.getBodyLength()];
        message.readBytes(messageBytes);
        return new String(messageBytes, StandardCharsets.UTF_8);
    }
}
