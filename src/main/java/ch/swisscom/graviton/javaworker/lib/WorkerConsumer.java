/**
 * queue consumer - here we have the main logic
 */

package ch.swisscom.graviton.javaworker.lib;

import java.io.IOException;
import java.util.Properties;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.impl.DeferredMap;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

/**
 * @author List of contributors
 *         <https://github.com/libgraviton/graviton/graphs/contributors>
 * @license http://opensource.org/licenses/gpl-license.php GNU Public License
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
     * @param properties properties
     * @param worker worker
     */
    public WorkerConsumer(Channel channel, Properties properties, WorkerAbstract worker) {
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
     * 
     * @return void
     */
    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
            throws IOException {
        String message = new String(body, "UTF-8");
        System.out.println(" [x] Received '" + envelope.getRoutingKey() + "':'" + message + "'");

        // deserialize
        DeferredMap jsonMessage = (DeferredMap) JSON.std.anyFrom(message);

        // give to worker
        this.worker.handleDelivery(consumerTag, jsonMessage);
    }



}
