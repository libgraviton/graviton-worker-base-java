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

/**
 * @author List of contributors
 *         <https://github.com/libgraviton/graviton/graphs/contributors>
 * @link http://swisscom.ch
 */
public class WorkerConsumer extends DefaultConsumer {
    
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
     * handles a delivery
     * 
     * @param consumerTag consumer tag
     * @param envelope envelope object
     * @param properties delivery props
     * @param body message body
     */
    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
            throws IOException {
        String message = new String(body, "UTF-8");
        System.out.println(" [x] Received '" + envelope.getRoutingKey() + "':'" + message + "'");

        // deserialize        
        QueueEvent qevent = JSON.std.beanFrom(QueueEvent.class, message);
        
        // give to worker
        this.worker.handleDelivery(consumerTag, qevent);
    }



}
