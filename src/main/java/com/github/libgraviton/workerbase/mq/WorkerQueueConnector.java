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
public class WorkerQueueConnector extends QueueConnector {

    private static final Logger LOG = LoggerFactory.getLogger(WorkerQueueConnector.class);

    private WorkerAbstract worker;

    private ConnectionFactory factory;

    private String exchangeName;

    private List<String> bindKeys;

    @Override
    protected void connect() throws QueueConnectionException {
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
        } catch (IOException e) {
            throw new QueueConnectionException("Cannot connect to message queue.", exchangeName, e);
        }
    }

    @Override
    public String getQueueName() {
        return exchangeName;
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
