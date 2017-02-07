package com.github.libgraviton.workerbase.mq.strategy.jms;

import com.github.libgraviton.workerbase.mq.Consumer;
import com.github.libgraviton.workerbase.mq.exception.CannotRegisterConsumer;

import javax.jms.JMSException;

public class ReRegisteringExceptionListener extends RecoveringExceptionListener {

    private Consumer consumer;

    public ReRegisteringExceptionListener(JmsConnection connection, Consumer consumer) {
        super(connection);
        this.consumer = consumer;
    }

    @Override
    public void onException(JMSException e) {
        super.onException(e);
        try {
            connection.consume(consumer);
        } catch (CannotRegisterConsumer registerException) {
            LOG.error(String.format("Consumer re-registration failed: %s", registerException.getMessage()));
        }
    }
}
