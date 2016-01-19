/**
 * queue consumer - here we have the main logic
 */

package com.github.libgraviton.workerbase;

import java.io.IOException;

import com.fasterxml.jackson.jr.ob.JSON;
import com.github.libgraviton.workerbase.model.QueueEvent;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        LOG.info("[x] Received '" + envelope.getRoutingKey() + "':'" + message + "'");

        // deserialize        
        QueueEvent qevent = JSON.std.beanFrom(QueueEvent.class, message);
        
        // give to worker
        this.worker.handleDelivery(consumerTag, qevent);
    }



}
