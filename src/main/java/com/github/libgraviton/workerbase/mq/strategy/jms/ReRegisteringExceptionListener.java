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
            LOG.info(String.format(
                    "Re-registering consumer '%s' on queue '%s'...",
                    consumer,
                    connection.getQueueName())
            );
            connection.consume(consumer);
            LOG.info(String.format(
                    "Successfully re-registered consumer '%s' on queue '%s'.",
                    consumer,
                    connection.getQueueName())
            );
        } catch (CannotRegisterConsumer registerException) {
            LOG.error(String.format(
                    "Re-registration of consumer '%s' on queue '%s' failed: %s",
                    consumer,
                    connection.getQueueName(),
                    registerException.getMessage()
            ));
        }
    }
}
