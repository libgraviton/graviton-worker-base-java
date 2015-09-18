package ch.swisscom.graviton.javaworker;

import java.io.IOException;

import com.rabbitmq.client.*;

/**
 * Created by dn on 17.09.15.
 */
public class Worker {

    private static final String EXCHANGE_NAME = "graviton";

    public void run() {
        try {
            this.connectToQueue();
        } catch (IOException e) {
            System.out.println("Problem connecting to the queue: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * @throws java.io.IOException
     */
    private void connectToQueue() throws IOException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "topic", true);
        String queueName = channel.queueDeclare().getQueue();

        channel.queueBind(queueName, EXCHANGE_NAME, "document.core.#");

        System.out.println(" [*] Waiting for messages.");

        channel.basicConsume(queueName, true, new WorkerConsumer(channel));
    }    

}
