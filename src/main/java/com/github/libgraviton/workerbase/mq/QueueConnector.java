package com.github.libgraviton.workerbase.mq;

import com.github.libgraviton.workerbase.WorkerAbstract;
import com.github.libgraviton.workerbase.WorkerConsumer;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * <p>QueueConnector</p>
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/graviton-worker-base-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
public class QueueConnector implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(QueueConnector.class);

    private int retryAfterSeconds = 10;

    private WorkerAbstract worker;

    private ConnectionFactory factory;

    private String exchangeName;

    private List<String> bindKeys;

    @Override
    public void run() {
        LOG.debug("Start connecting to to message queue at '" + factory.getHost() + ":" + factory.getPort() + "'.");

        Boolean isConnected = Boolean.FALSE;
        while (!isConnected) {
            isConnected = connect();
        }
    }

    /**
     * Connecting to message queue. Will be retried until successful.
     */
    protected Boolean connect() {
        try {
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            channel.exchangeDeclare(exchangeName, "topic", true);
            String queueName = channel.queueDeclare().getQueue();

            for (String bindKey : bindKeys) {
                channel.queueBind(queueName, exchangeName, bindKey);
                LOG.info("Subscribed on topic exchange '" + exchangeName + "' using binding key '" + bindKey + "'.");
            }

            channel.basicQos(2);
            channel.basicConsume(queueName, true, new WorkerConsumer(channel, worker));

            // successfully connected
            return true;
        } catch (IOException e) {
            LOG.warn("Unable to connect to message queue at '" + factory.getHost() + ":" + factory.getPort() + "'. Retry again...");
            try {
                Thread.sleep(retryAfterSeconds * 1000);
            } catch (InterruptedException ie) {
                LOG.debug("Sleep interrupted", ie);
            }
        }

        return false;
    }

    public void setRetryAfterSeconds(int retryAfterSeconds) {
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public void setWorker(WorkerAbstract worker) {
        this.worker = worker;
    }

    public void setFactory(ConnectionFactory factory) {
        this.factory = factory;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public void setBindKeys(List<String> bindKeys) {
        this.bindKeys = bindKeys;
    }
}
