package com.github.libgraviton.workerbase.messaging.strategy.jms;

import com.github.libgraviton.workerbase.messaging.consumer.Consumeable;
import com.github.libgraviton.workerbase.messaging.exception.CannotRegisterConsumeable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;

class ReRegisteringExceptionListener extends RecoveringExceptionListener {

    private static final Logger LOG = LoggerFactory.getLogger(com.github.libgraviton.workerbase.messaging.strategy.jms.ReRegisteringExceptionListener.class);

    private final Consumeable consumeable;

    ReRegisteringExceptionListener(JmsConnection connection, Consumeable consumeable) {
        super(connection);
        this.consumeable = consumeable;
    }

    @Override
    public void onException(JMSException e) {
        super.onException(e);
        try {
            LOG.info("Re-registering consumer '{}' on queue '{}'...",
                    consumeable,
                    connection.getConnectionName()
            );
            connection.consume(consumeable);
            LOG.info("Successfully re-registered consumer '{}' on queue '{}'.",
                    consumeable,
                    connection.getConnectionName()
            );
        } catch (CannotRegisterConsumeable registerException) {
            LOG.error("Re-registration of consumer '{}' on queue '{}' failed: {}",
                    consumeable,
                    connection.getConnectionName(),
                    registerException.getMessage()
            );
        }
    }
}
