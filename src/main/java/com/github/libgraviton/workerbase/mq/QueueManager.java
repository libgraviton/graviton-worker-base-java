package com.github.libgraviton.workerbase.mq;

import com.github.libgraviton.workerbase.WorkerAbstract;
import com.github.libgraviton.workerbase.WorkerConsumer;
import com.github.libgraviton.workerbase.mq.exception.CannotConnectToQueue;
import com.github.libgraviton.workerbase.mq.exception.CannotRegisterConsumer;
import com.github.libgraviton.workerbase.mq.strategy.rabbitmq.direct.RabbitMqConnection;
import com.rabbitmq.client.ConnectionFactory;
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
        int retrySleep = Integer.parseInt(properties.getProperty("queue.connecting.retryAfterSeconds")) * 1000;
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(properties.getProperty("queue.host"));
        factory.setPort(Integer.parseInt(properties.getProperty("queue.port")));
        factory.setUsername(properties.getProperty("queue.username"));
        factory.setPassword(properties.getProperty("queue.password"));
        factory.setVirtualHost(properties.getProperty("queue.vhost"));
        factory.setAutomaticRecoveryEnabled(true);
        factory.setNetworkRecoveryInterval(
            Integer.parseInt(properties.getProperty("queue.connecting.retryAfterSeconds")) * 1000
        );

        connection = new RabbitMqConnection(
            properties.getProperty("graviton.workerId"),
            properties.getProperty("queue.exchange", null),
            properties.getProperty("graviton.workerId"),
            factory
        );
        connection.setConnectionAttemptSleep(retrySleep);
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
