package com.github.libgraviton.workerbase.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>QueueConnector</p>
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/graviton-worker-base-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
public abstract class QueueConnector implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(QueueConnector.class);

    private int retryAfterSeconds = 10;

    @Override
    public void run() {
        LOG.debug("Start connecting to to message queue '" + getQueueName() + "'.");

        Boolean isConnected = Boolean.FALSE;
        while (!isConnected) {
            isConnected = connectAttempt();
        }
    }

    /**
     * Connecting to message queue. Will be retried until successful.
     * @return true if connection attempt was successful
     */
    public Boolean connectAttempt() {
        try {
            connect();
            // successfully connected
            return true;
        } catch (QueueConnectionException e) {
            LOG.warn("Unable to connect to message queue '" + e.getQueueName() + "'. Retry again...");
            try {
                Thread.sleep(retryAfterSeconds * 1000);
            } catch (InterruptedException ie) {
                LOG.debug("Sleep interrupted", ie);
            }
        }

        return false;
    }

    public void setRetryAfterSeconds(int retryAfterSeconds) {
        this.retryAfterSeconds = retryAfterSeconds;
    }

    protected abstract void connect() throws QueueConnectionException;

    public abstract String getQueueName();
}
