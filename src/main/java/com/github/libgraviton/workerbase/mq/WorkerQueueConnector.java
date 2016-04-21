package com.github.libgraviton.workerbase.mq;

import com.github.libgraviton.workerbase.WorkerAbstract;
import com.github.libgraviton.workerbase.WorkerConsumer;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * <p>QueueConnector</p>
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/graviton-worker-base-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
public class WorkerQueueConnector extends QueueConnector {

    private WorkerAbstract worker;

    private ConnectionFactory factory;

    private String queueName;

    private Boolean durable = Boolean.TRUE;

    private Boolean exclusive = Boolean.FALSE;

    private Boolean autoDelete = Boolean.FALSE;

    private Boolean autoAck = Boolean.FALSE;

    // how many messages should this worker handle at a time?
    private Integer prefetchCount = 2;

    @Override
    protected void connect() throws QueueConnectionException {
        try {
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            Map<String, Object> arguments = null;
            channel.queueDeclare(queueName, durable, exclusive, autoDelete, arguments);

            channel.basicQos(prefetchCount);
            channel.basicConsume(queueName, autoAck, new WorkerConsumer(channel, worker, queueName));
        } catch (IOException | TimeoutException e) {
            throw new QueueConnectionException("Cannot connect to message queue.", queueName, e);
        }
    }

    @Override
    public String getQueueName() {
        return queueName;
    }

    public void setWorker(WorkerAbstract worker) {
        this.worker = worker;
    }

    public void setFactory(ConnectionFactory factory) {
        this.factory = factory;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public WorkerAbstract getWorker() {
        return worker;
    }

    public ConnectionFactory getFactory() {
        return factory;
    }

    public Integer getPrefetchCount() {
        return prefetchCount;
    }

    public void setPrefetchCount(Integer prefetchCount) {
        this.prefetchCount = prefetchCount;
    }
}
