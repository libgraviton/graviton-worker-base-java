package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.messaging.QueueConnection;
import com.github.libgraviton.workerbase.messaging.exception.CannotConnectToQueue;
import com.github.libgraviton.workerbase.messaging.exception.CannotRegisterConsumer;
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

    private QueueConnection connection;

    public QueueManager(Properties properties) {
        connection = new RabbitMqConnection.Builder()
                .queueName(properties.getProperty("graviton.workerId"))
                .routingKey(properties.getProperty("graviton.workerId"))
                .applyProperties(properties, "queue.")
                .build();
    }

    /**
     * Async connection to queue.
     *
     * @param worker the worker
     *
     * @throws CannotConnectToQueue if connection to queue cannot be established
     * @throws CannotRegisterConsumer if connection was successfully established, but consumer registration failed
     */
    public void connect(WorkerAbstract worker) throws CannotConnectToQueue, CannotRegisterConsumer {
        connection.consume(new WorkerConsumer(worker));
    }
}
