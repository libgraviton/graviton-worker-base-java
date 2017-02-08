package com.github.libgraviton.workerbase.mq;

import com.github.libgraviton.workerbase.mq.exception.CannotCloseConnection;
import com.github.libgraviton.workerbase.mq.exception.CannotConnectToQueue;
import com.github.libgraviton.workerbase.mq.exception.CannotPublishMessage;
import com.github.libgraviton.workerbase.mq.exception.CannotRegisterConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract public class QueueConnection {

    protected static final Logger LOG = LoggerFactory.getLogger(QueueConnection.class);

    protected String queueName;

    private int connectionAttempts = 0;

    private int connectionAttemptSleep = 1;

    private Consumer consumer;

    public QueueConnection(String queueName) {
        this.queueName = queueName;
    }

    public void setConnectionAttempts(int connectionAttempts) {
        this.connectionAttempts = connectionAttempts;
    }

    public void setConnectionAttemptSleep(int connectionAttemptSleep) {
        this.connectionAttemptSleep = connectionAttemptSleep;
    }

    public void open() throws CannotConnectToQueue {
        int connectionAttempts = this.connectionAttempts;
        boolean retryEndless = connectionAttempts == 0;
        LOG.info(String.format("Connecting to queue '%s'...", queueName));
        while (connectionAttempts > 0 || retryEndless) {
            try {
                openConnection();
                break;
            } catch (CannotConnectToQueue e) {
                LOG.error(String.format("Unable to open to queue '%s': '%s'", queueName, e.getMessage()));
                // Last try failed
                if (1 == connectionAttempts) {
                    throw e;
                }
            }
            LOG.warn(String.format(
                    "Connection to queue '%s' failed. Retrying in '%s' seconds.",
                    queueName,
                    connectionAttemptSleep
            ));
            try {
                Thread.sleep(connectionAttemptSleep * 1000);
            } catch (InterruptedException e) {
                LOG.warn(String.format("Thread sleep interrupted: %s", e.getMessage()));
            }
            if (!retryEndless) {
                connectionAttempts--; // avoid integer range overflow in endless mode
            }
        }
        LOG.info(String.format("Connection to queue '%s' successfully established.", queueName));
    }

    public void close() {
        LOG.info(String.format("Closing connection to queue '%s'...", queueName));
        consumer = null;
        try {
            closeConnection();
            LOG.info(String.format("Connection to queue '%s' successfully closed.", queueName));
        } catch (CannotCloseConnection e) {
            LOG.warn(String.format("Cannot successfully close queue '%s': '%s'", queueName, e.getMessage()));
        }
    }

    public void consume(Consumer consumer) throws CannotRegisterConsumer {
        LOG.info(String.format("Registering consumer on queue '%s'...", queueName));
        // This allows easier consumer recovery on queue exceptions
        if (null != this.consumer) {
            throw new CannotRegisterConsumer(
                    consumer,
                    "Another consumer is already registered. " +
                            "Please register a composite consumer If you want to use multiple consumers."
            );
        }
        try {
            open();
        } catch (CannotConnectToQueue e) {
            throw new CannotRegisterConsumer(consumer, e);
        }
        registerConsumer(consumer);
        this.consumer = consumer;
        LOG.info(String.format("Consumer successfully registered on queue '%s'. Waiting for messages...", queueName));
    }

    public void publish(String message) throws CannotPublishMessage {
        LOG.info(String.format("Publishing message on queue '%s': '%s", queueName, message));
        try {
            open();
            publishMessage(message);
        } catch (CannotConnectToQueue e) {
            throw new CannotPublishMessage(message, e);
        } finally {
            close();
        }
        LOG.info(String.format("Message successfully published on queue '%s'.", queueName));
    }

    public String getQueueName() {
        return queueName;
    }

    abstract public boolean isOpen();

    abstract protected void publishMessage(String message) throws CannotPublishMessage;

    abstract protected void registerConsumer(Consumer consumer) throws CannotRegisterConsumer;

    abstract protected void openConnection() throws CannotConnectToQueue;

    abstract protected void closeConnection() throws CannotCloseConnection;

}
