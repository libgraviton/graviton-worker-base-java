package com.github.libgraviton.workerbase.mq.strategy.jms;

import com.github.libgraviton.workerbase.mq.Consumer;
import com.github.libgraviton.workerbase.mq.QueueConnection;
import com.github.libgraviton.workerbase.mq.exception.CannotCloseConnection;
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
    protected void publishMessage(String message) throws CannotPublishMessage {
        try {
            MessageProducer producer = session.createProducer(queue);
            BytesMessage bytesMessage = session.createBytesMessage();
            bytesMessage.writeBytes(message.getBytes(StandardCharsets.UTF_8));
            producer.send(bytesMessage);
        } catch (JMSException e) {
            throw new CannotPublishMessage(message, e);
        }
    }

    @Override
    protected void registerConsumer(Consumer consumer) throws CannotRegisterConsumer {
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
            throw new CannotRegisterConsumer(consumer, e);
        }
    }

    @Override
    protected void openConnection() throws CannotConnectToQueue {
        try {
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE); // AutoAck is done by JmsConsumer
            queue = session.createQueue(queueName);
        } catch (JMSException e) {
            throw new CannotConnectToQueue(queueName, e);
        }
    }

    @Override
    protected void closeConnection() throws CannotCloseConnection {
        try {
            if (session != null) {
                session.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (JMSException e) {
            throw new CannotCloseConnection(queueName, e);
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
