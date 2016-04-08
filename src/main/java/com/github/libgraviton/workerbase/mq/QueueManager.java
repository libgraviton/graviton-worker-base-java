package com.github.libgraviton.workerbase.mq;

import java.util.Properties;

/**
 * <p>QueueManager</p>
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/graviton-worker-base-java/graphs/contributors>}
 * @version $Id: $Id
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 */
public abstract class QueueManager {

    int retryAfterSeconds;

    public QueueManager(Properties properties) {
        retryAfterSeconds = Integer.parseInt(properties.getProperty("queue.connecting.retryAfterSeconds"));
    }

    /**
     * Async connection to queue.
     */
    public void connect() {
        QueueConnector queueConnector = getQueueConnector();
        queueConnector.setRetryAfterSeconds(retryAfterSeconds);
        new Thread(getQueueConnector()).start();
    }

    protected abstract QueueConnector getQueueConnector();

    public int getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
