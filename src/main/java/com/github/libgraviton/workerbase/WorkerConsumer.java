/**
 * queue consumer - here we have the main logic
 */

package com.github.libgraviton.workerbase;

import com.fasterxml.jackson.jr.ob.JSON;
import com.github.libgraviton.workerbase.model.QueueEvent;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * <p>WorkerConsumer class.</p>
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/graviton-worker-base-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
public class WorkerConsumer extends DefaultConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(WorkerConsumer.class);

    /**
     * worker
     */
    private WorkerAbstract worker;

    /**
     * constructor
     *
     * @param channel channel
     * @param worker worker
     */
    public WorkerConsumer(Channel channel, WorkerAbstract worker) {
        super(channel);                
        this.worker = worker;
    }

    /**
     * {@inheritDoc}
     *
     * handles a delivery
     */
    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
            throws IOException {
        String message = new String(body, "UTF-8");
        LOG.info("Received '" + envelope.getRoutingKey() + "':'" + message + "'");

        // deserialize
        QueueEvent queueEvent = JSON.std.beanFrom(QueueEvent.class, message);
        
        // give to worker
        worker.handleDelivery(consumerTag, queueEvent);
    }

    @Override
    public void handleConsumeOk(String consumerTag) {
        LOG.info("Connection to message queue established.");
        LOG.info("Waiting for messages...");
    }

    @Override
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
        LOG.info("Lost connection to message queue. Starting connection recovery.");
    }

}
