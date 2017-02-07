package com.github.libgraviton.workerbase.mq.strategy.jms;

import com.github.libgraviton.workerbase.mq.AcknowledgingConsumer;
import com.github.libgraviton.workerbase.mq.Consumer;
import com.github.libgraviton.workerbase.mq.QueueConnection;
import com.github.libgraviton.workerbase.mq.exception.CannotConnectToQueue;
import com.github.libgraviton.workerbase.mq.exception.CannotPublishMessage;
import com.github.libgraviton.workerbase.mq.exception.CannotRegisterConsumer;

import javax.jms.*;
import java.nio.charset.StandardCharsets;

public class JmsConnection extends QueueConnection {


    private ConnectionFactory connectionFactory;

    private Connection connection;

    private Session session;

    private String messageSelector;

    protected Queue queue;

    public JmsConnection(String queueName, ConnectionFactory connectionFactory) {
        super(queueName);
        this.connectionFactory = connectionFactory;
    }

    @Override
    public boolean isOpen() {
        return null != connection;
    }

    @Override
    public void publish(String message) throws CannotPublishMessage {
        try {
            open();
            MessageProducer producer = session.createProducer(queue);
            BytesMessage bytesMessage = session.createBytesMessage();
            bytesMessage.writeBytes(message.getBytes(StandardCharsets.UTF_8));
            producer.send(bytesMessage);
            LOG.info(String.format("Message successfully put to queue: '%s'.", queueName));
        } catch (JMSException | CannotConnectToQueue e) {
            throw new CannotPublishMessage(message, e);
        } finally {
            close();
        }
    }

    @Override
    public void consume(Consumer consumer) throws CannotRegisterConsumer {
        try {
            open();

        } catch (CannotConnectToQueue e) {
            throw new CannotRegisterConsumer(consumer, e);
        }
        MessageListener jmsConsumer = new JmsConsumer(consumer);
        MessageConsumer messageConsumer;
        try {
            connection.setExceptionListener(new ReRegisteringExceptionListener(this, consumer));
            if (null != messageSelector) {
                messageConsumer = session.createConsumer(queue, messageSelector);
            } else {
                messageConsumer = session.createConsumer(queue);
            }

            messageConsumer.setMessageListener(jmsConsumer);
            connection.start();
        } catch (JMSException e) {
            LOG.debug(e.getMessage());
            throw new CannotRegisterConsumer(consumer, e);
        }
        LOG.info("Waiting for messages...");
    }

    @Override
    public void consume(AcknowledgingConsumer consumer) throws CannotRegisterConsumer {

    }

    @Override
    protected void openConnection() throws CannotConnectToQueue {
        try {
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            queue = session.createQueue(queueName);
        } catch (JMSException e) {
            throw new CannotConnectToQueue(String.format(
                    "Unable to open connection to queue '%s': '%s'",
                    queueName,
                    e.getMessage()
            ));
        }
    }

    @Override
    protected void closeConnection() throws CannotConnectToQueue {
        try {
            if (session != null) {
                session.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (JMSException e) {
            throw new CannotConnectToQueue(
                    String.format("Cannot close connection to queue '%s'.", queueName),
                    e
            );
        } finally {
            session = null;
            connection = null;
            queue = null;
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public Session getSession() {
        return session;
    }

    public Queue getQueue() {
        return queue;
    }

    public String getMessageSelector() {
        return messageSelector;
    }

    public void setMessageSelector(String messageSelector) {
        this.messageSelector = messageSelector;
    }
}
