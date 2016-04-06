package com.github.libgraviton.workerbase.mq;

import com.github.libgraviton.workerbase.WorkerAbstract;
import com.rabbitmq.client.ConnectionFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * <p>QueueManager</p>
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/graviton-worker-base-java/graphs/contributors>}
 * @version $Id: $Id
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 */
public class QueueManager {

    private ConnectionFactory factory;

    private String exchangeName;

    private List<String> bindKeys;

    private int retryAfterSeconds;


    public QueueManager(Properties properties) {
        exchangeName = properties.getProperty("queue.exchangeName");
        bindKeys = Arrays.asList(properties.getProperty("queue.bindKey").split(","));
        retryAfterSeconds = Integer.parseInt(properties.getProperty("queue.connecting.retryAfterSeconds"));
        factory = getFactory(properties);
    }

    /**
     * Async connection to queue.
     *
     * @param worker will be attached as queue consumer
     */
    public void connect(WorkerAbstract worker) {
        QueueConnector queueConnector = getQueueConnector();
        queueConnector.setBindKeys(bindKeys);
        queueConnector.setExchangeName(exchangeName);
        queueConnector.setFactory(factory);
        queueConnector.setWorker(worker);
        queueConnector.setRetryAfterSeconds(retryAfterSeconds);

        queueConnector.run();
    }

    protected ConnectionFactory getFactory(Properties properties) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(properties.getProperty("queue.host"));
        factory.setPort(Integer.parseInt(properties.getProperty("queue.port")));
        factory.setUsername(properties.getProperty("queue.username"));
        factory.setPassword(properties.getProperty("queue.password"));
        factory.setVirtualHost(properties.getProperty("queue.vhost"));
        factory.setAutomaticRecoveryEnabled(true);
        factory.setNetworkRecoveryInterval(retryAfterSeconds * 1000);
        factory.setExceptionHandler(new QueueExceptionHandler());

        return factory;
    }

    protected QueueConnector getQueueConnector() {
        return new QueueConnector();
    }

    public ConnectionFactory getFactory() {
        return factory;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public List<String> getBindKeys() {
        return bindKeys;
    }

    public int getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
