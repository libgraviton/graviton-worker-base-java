package com.github.libgraviton.workerbase.mq.strategy.jms;

import com.github.libgraviton.workerbase.mq.exception.CannotConnectToQueue;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RecoveringExceptionListener implements ExceptionListener {

    protected static final Logger LOG = LoggerFactory.getLogger(RecoveringExceptionListener.class);

    protected JmsConnection connection;

    RecoveringExceptionListener(JmsConnection connection) {
        this.connection = connection;
    }

    @Override
    public void onException(JMSException e) {
        LOG.warn(String.format(
                "Connection to queue '%s' encountered an exception with code '%s' and message '%s'.",
                connection.getQueueName(),
                e.getErrorCode(),
                e.getMessage()
        ));
        LOG.info(String.format("Recovering connection to queue '%s'...", connection.getQueueName()));

        try {
            connection.close();
            connection.open();
            LOG.info(String.format("Connection to queue '%s' successfully re-established.", connection.getQueueName()));
        } catch (CannotConnectToQueue recoverException) {
            LOG.error(String.format(
                    "Connection recovery for queue '%s' failed: %s",
                    connection.getQueueName(),
                    recoverException.getMessage())
            );
        }
    }
}
