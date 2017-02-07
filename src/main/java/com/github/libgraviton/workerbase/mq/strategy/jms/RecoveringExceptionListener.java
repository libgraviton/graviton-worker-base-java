package com.github.libgraviton.workerbase.mq.strategy.jms;

import com.github.libgraviton.workerbase.mq.exception.CannotConnectToQueue;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author List of contributors
 *         <https://github.com/libgraviton/graviton/graphs/contributors>
 * @link http://swisscom.ch
 */
public class RecoveringExceptionListener implements ExceptionListener {

    protected static final Logger LOG = LoggerFactory.getLogger(RecoveringExceptionListener.class);

    protected JmsConnection connection;

    public RecoveringExceptionListener(JmsConnection connection) {
        this.connection = connection;
    }

    @Override
    public void onException(JMSException e) {
        LOG.warn(String.format(
                "Connection encountered an exception with code '%s' and message '%s'.",
                e.getErrorCode(),
                e.getMessage()
        ));
        LOG.info("Reconnecting...");

        try {
            connection.open();
        } catch (CannotConnectToQueue recoverException) {
            LOG.error(String.format("Connection recovery failed: %s", recoverException.getMessage()));
        }
    }
}
