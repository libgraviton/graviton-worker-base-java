package com.github.libgraviton.workerbase.mq.strategy.rabbitmq.jms;

import com.github.libgraviton.workerbase.mq.strategy.jms.JmsConnection;
import com.rabbitmq.jms.admin.RMQConnectionFactory;

public class ConnectionFactory {

    static JmsConnection createConnection(
            String queueName,
            String host,
            String virtualHost,
            int port,
            String user,
            String password,
            int onMessageTimeout
    ) {
        RMQConnectionFactory factory = new RMQConnectionFactory();
        factory.setVirtualHost(virtualHost);
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(user);
        factory.setPassword(password);
        factory.setOnMessageTimeoutMs(onMessageTimeout);
        return new JmsConnection(queueName, factory);
    }

}
