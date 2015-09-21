/**
 * connects to the queue and subscribes the WorkerConsumer on the queue
 */

package ch.swisscom.graviton.javaworker;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSONObjectException;
import com.fasterxml.jackson.jr.ob.impl.DeferredMap;
import com.rabbitmq.client.*;

/**
 * @author List of contributors
 *         <https://github.com/libgraviton/graviton/graphs/contributors>
 * @license http://opensource.org/licenses/gpl-license.php GNU Public License
 * @link http://swisscom.ch
 */
public class Worker {

    /**
     * exchange name
     */
    private static final String EXCHANGE_NAME = "graviton";

    /**
     * properties
     */
    private Properties properties;

    /**
     * initializes all
     * 
     * @return void
     */
    public void run() {

        try {
            this.loadProperties();
            this.applyVcapConfig();
            this.connectToQueue();
        } catch (IOException e) {
            System.out.println("Problem connecting to the queue: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Could not parse VCAP configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * connects to the queue
     * 
     * @throws java.io.IOException
     * 
     * @return void
     */
    private void connectToQueue() throws IOException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(this.properties.getProperty("queue.host"));
        factory.setPort(Integer.parseInt(this.properties.getProperty("queue.port")));
        factory.setUsername(this.properties.getProperty("queue.username"));
        factory.setPassword(this.properties.getProperty("queue.password"));
        factory.setVirtualHost(this.properties.getProperty("queue.vhost"));
        factory.setAutomaticRecoveryEnabled(true);

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "topic", true);
        String queueName = channel.queueDeclare().getQueue();

        channel.queueBind(queueName, EXCHANGE_NAME, "document.core.app.*");

        System.out.println(" [*] Waiting for messages.");

        WorkerConsumer consumer = new WorkerConsumer(channel, this.properties);

        channel.basicQos(2);
        channel.basicConsume(queueName, true, consumer);
    }

    /**
     * loads the properties
     * 
     * @return void
     */
    private void loadProperties() {
        this.properties = new Properties();
        try {
            
            // load defaults
            InputStream defaultProps = this.getClass().getClassLoader().getResourceAsStream("default.properties");
            this.properties.load(defaultProps);
            defaultProps.close();
            
            // overrides?
            InputStream appProps = this.getClass().getClassLoader().getResourceAsStream("app.properties");
            if (appProps != null) {
                this.properties.load(appProps);
                appProps.close();
            }
            
        } catch (Exception e1) {
            System.out.println("Could not load properties: " + e1.getMessage());
            e1.printStackTrace();
        }
    }

    /**
     * Let's see if we have VCAP ENV vars that we should apply to configuration
     * 
     * @return void
     * @throws IOException
     * @throws JSONObjectException
     * 
     * @return void
     */
    private void applyVcapConfig() throws Exception {
        String vcap = System.getenv("VCAP_SERVICES");
        if (vcap != null) {
            DeferredMap vcapConf = (DeferredMap) JSON.std.anyFrom(vcap);
            if (vcapConf.containsKey("rabbitmq-3.0")) {
                @SuppressWarnings("unchecked")
                DeferredMap vcapCreds = (DeferredMap) ((ArrayList<DeferredMap>) vcapConf.get("rabbitmq-3.0")).get(0);
                vcapCreds = (DeferredMap) vcapCreds.get("credentials");

                this.properties.setProperty("queue.host", vcapCreds.get("host").toString());
                this.properties.setProperty("queue.port", vcapCreds.get("port").toString());
                this.properties.setProperty("queue.username", vcapCreds.get("username").toString());
                this.properties.setProperty("queue.password", vcapCreds.get("password").toString());
                this.properties.setProperty("queue.vhost", vcapCreds.get("vhost").toString());
            }
        }
    }
}
