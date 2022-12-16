package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.helper.WorkerProperties;
import com.github.libgraviton.workerbase.messaging.QueueConnection;
import com.github.libgraviton.workerbase.messaging.consumer.Consumeable;
import com.github.libgraviton.workerbase.messaging.exception.CannotConnectToQueue;
import com.github.libgraviton.workerbase.messaging.exception.CannotPublishMessage;
import com.github.libgraviton.workerbase.messaging.exception.CannotRegisterConsumeable;
import com.github.libgraviton.workerbase.messaging.strategy.rabbitmq.RabbitMqConnection;

import java.util.Properties;

/**
 * <p>QueueManager</p>
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/graviton-worker-base-java/graphs/contributors>}
 * @version $Id: $Id
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 */
public class QueueManager {

    private final QueueConnection connection;

    public QueueManager(Properties properties) {
        connection = new RabbitMqConnection.Builder()
                .queueName(properties.getProperty(WorkerProperties.WORKER_ID))
                .routingKey(properties.getProperty(WorkerProperties.WORKER_ID))
                .applyProperties(properties, "queue.")
                .build();
    }

    /**
     * Async connection to queue.
     *
     * @throws CannotConnectToQueue if connection to queue cannot be established
     * @throws CannotRegisterConsumeable if connection was successfully established, but consumer registration failed
     */
    public void connect(final Consumeable consumeable) throws CannotConnectToQueue, CannotRegisterConsumeable {
        connection.consume(consumeable);
    }

    public void publish(String message) throws CannotPublishMessage {
        connection.publish(message);
    }

    public void close() {
        connection.close();
    }
}
