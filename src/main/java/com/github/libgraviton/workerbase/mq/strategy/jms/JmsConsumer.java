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

public class JmsConsumer implements MessageListener, MessageAcknowledger {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private Consumer consumer;

    private HashMap<String, Message> messages;

    public JmsConsumer(Consumer consumer) {
        this.consumer = consumer;
        messages = new HashMap<>();
    }

    /**
     * Entry point of an incoming queue message. Starts processing the feedback XML.
     *
     * @param jmsMessage The incoming message
     */
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
                BytesMessage bytesMessage = (BytesMessage) jmsMessage;
                byte[] messageBytes = new byte[(int) bytesMessage.getBodyLength()];
                bytesMessage.readBytes(messageBytes);
                message = new String(messageBytes, StandardCharsets.UTF_8);
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
}
