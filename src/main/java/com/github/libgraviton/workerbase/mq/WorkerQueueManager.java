package com.github.libgraviton.workerbase.mq;

import com.github.libgraviton.workerbase.WorkerAbstract;
import com.rabbitmq.client.ConnectionFactory;

import java.util.Properties;

/**
 * <p>QueueManager</p>
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/graviton-worker-base-java/graphs/contributors>}
 * @version $Id: $Id
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 */
public class WorkerQueueManager extends QueueManager {

    private ConnectionFactory factory;

    private WorkerAbstract worker;

    private Integer prefetchCount;


    public WorkerQueueManager(Properties properties) {
        super(properties);
        prefetchCount = Integer.parseInt(properties.getProperty("queue.prefetchCount"));
        factory = getFactory(properties);
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

    @Override
    protected QueueConnector getQueueConnector() {
        WorkerQueueConnector queueConnector = new WorkerQueueConnector();
        queueConnector.setQueueName(getQueueName());
        queueConnector.setFactory(factory);
        queueConnector.setWorker(worker);
        queueConnector.setRetryAfterSeconds(retryAfterSeconds);
        queueConnector.setPrefetchCount(prefetchCount);
        return queueConnector;
    }

    public void setWorker(WorkerAbstract worker) {
        this.worker = worker;
    }

    public ConnectionFactory getFactory() {
        return factory;
    }

    public WorkerAbstract getWorker() {
        return worker;
    }

    public String getQueueName() {
        return worker.getWorkerId();
    }
}
