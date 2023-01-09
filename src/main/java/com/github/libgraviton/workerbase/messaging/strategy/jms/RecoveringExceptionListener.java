package com.github.libgraviton.workerbase.messaging.strategy.jms;

import com.github.libgraviton.workerbase.messaging.exception.CannotConnectToQueue;
import com.github.libgraviton.workerbase.messaging.strategy.jms.JmsConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;

class RecoveringExceptionListener implements ExceptionListener {

    private static final Logger LOG = LoggerFactory.getLogger(com.github.libgraviton.workerbase.messaging.strategy.jms.RecoveringExceptionListener.class);

    protected final JmsConnection connection;

    RecoveringExceptionListener(JmsConnection connection) {
        this.connection = connection;
    }

    @Override
    public void onException(JMSException e) {
        LOG.warn(String.format(
                "Connection to queue '%s' encountered an com.github.libgraviton.workerbase.messaging.exception with code '%s' and message '%s'.",
                connection.getConnectionName(),
                e.getErrorCode(),
                e.getMessage()
        ));
        LOG.info(String.format("Recovering connection to queue '%s'...", connection.getConnectionName()));

        try {
            connection.close();
            connection.open();
            LOG.info(String.format("Connection to queue '%s' successfully re-established.", connection.getConnectionName()));
        } catch (CannotConnectToQueue recoverException) {
            LOG.error(String.format(
                    "Connection recovery for queue '%s' failed: %s",
                    connection.getConnectionName(),
                    recoverException.getMessage())
            );
        }
    }
}
