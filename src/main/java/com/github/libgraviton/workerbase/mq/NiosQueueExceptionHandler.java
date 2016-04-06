package com.github.libgraviton.workerbase.mq;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.impl.DefaultExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>NiosQueueExceptionHandler</p>
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/graviton-worker-base-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
public class NiosQueueExceptionHandler extends DefaultExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(NiosQueueExceptionHandler.class);

    @Override
    public void handleConnectionRecoveryException(Connection conn, Throwable exception) {
        //super.handleConnectionRecoveryException(conn, exception);
        LOG.warn("Message queue connection recovery not yet successful. Retry again...");
    }
}
