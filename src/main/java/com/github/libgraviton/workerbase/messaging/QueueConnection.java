package com.github.libgraviton.workerbase.messaging;

import com.github.libgraviton.workerbase.messaging.config.ContextProperties;
import com.github.libgraviton.workerbase.messaging.config.PropertyUtil;
import com.github.libgraviton.workerbase.messaging.consumer.Consumeable;
import com.github.libgraviton.workerbase.messaging.exception.*;
import com.github.libgraviton.workerbase.util.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Represents a connection to a queue of any queue system.
 */
abstract public class QueueConnection {

  protected static final Logger LOG = LoggerFactory.getLogger(com.github.libgraviton.workerbase.messaging.QueueConnection.class);

  protected final String queueName;

  protected QueueConnection(Builder builder) {
    queueName = builder.queueName;
  }

  /**
   * @see Builder#connectionAttempts(int)
   * @see Builder#connectionAttemptsWait(double)
   *
   * @throws CannotConnectToQueue If connecting to the queue failed.
   */
  public void open() throws CannotConnectToQueue {
    LOG.info("Connecting to queue '{}'...", getConnectionName());

    try {
      RetryRegistry.retrySomethingForever(
        () -> {
          openConnection();
          LOG.info("Connection to queue '{}' successfully established.", getConnectionName());
          return null;
        },
        (event) -> LOG.warn("Error connecting to the queue: {}", event.getLastThrowable() == null ? "?" : event.getLastThrowable().getMessage())
      );
    } catch (Throwable e) {
      LOG.error(
        "Unable to open to queue '{}': '{}' ('{}'), exhausted tries.",
        getConnectionName(),
        e.getMessage(),
        e.getCause() != null ? e.getCause().getMessage() : "[no cause]",
        e
      );
      throw new CannotConnectToQueue("Exhausted tries to connect to queue.", e);
    }
  }

  /**
   * Closes the connection
   */
  public void close() {
    LOG.info("Closing connection to queue '{}'...", getConnectionName());
    try {
      closeConnection();
      LOG.info("Connection to queue '{}' successfully closed.", getConnectionName());
    } catch (CannotCloseConnection e) {
      LOG.warn("Cannot successfully close queue '{}': '{}'",
        getConnectionName(),
        e.getCause().getMessage()
      );
    }
  }

  /**
   * Registers a consumer on the queue. Please not that you can only register one consumer per queue. This is intended
   * per design to make queue abstraction easier. If you need multiple consumers on the same queue for some reason,
   * please use a composite consumer.
   *
   * @throws CannotRegisterConsumeable If the consumer cannot be registered for some reason.
   */
  public void consume(Consumeable consumeable) throws CannotRegisterConsumeable {
    LOG.info("Registering consumer on queue '{}'...", getConnectionName());
    try {
      openIfClosed();
    } catch (CannotConnectToQueue e) {
      throw new CannotRegisterConsumeable(consumeable, e);
    }
    registerConsumer(consumeable);
    LOG.info("Consumer successfully registered on queue '{}'. Waiting for messages...",
      getConnectionName()
    );
  }

  /**
   * Publishes a text message on the queue. If the queue has not yet been opened, it will be opened, the message published
   * and then closed again. If the queue has already been opened, it won't be closed after publishing the message.
   *
   * @param message The message to publish
   *
   * @throws CannotPublishMessage If the message cannot be published for some reason.
   */
  public void publish(String message) throws CannotPublishMessage {
    LOG.debug("Publishing text message on queue '{}': '{}", getConnectionName(), message);
    boolean wasClosed = false;
    try {
      wasClosed = openIfClosed();
      publishMessage(message);
    } catch (CannotConnectToQueue e) {
      throw new CannotPublishMessage(message, e);
    } finally {
      if (wasClosed) {
        close();
      }
    }
    LOG.info("Message successfully published on queue '{}'.", getConnectionName());
  }

  /**
   * Publishes a bytes message on the queue. If the queue has not yet been opened, it will be opened, the message published
   * and then closed again. If the queue has already been opened, it won't be closed after publishing the message.
   *
   * @param message The message to publish
   *
   * @throws CannotPublishMessage If the message cannot be published for some reason.
   */
  public void publish(byte[] message) throws CannotPublishMessage {
    LOG.debug("Publishing bytes message on queue '{}': '{}", getConnectionName(), new String(message));
    boolean wasClosed = false;
    try {
      wasClosed = openIfClosed();
      publishMessage(message);
    } catch (CannotConnectToQueue e) {
      throw new CannotPublishMessage(new String(message), e);
    } finally {
      if (wasClosed) {
        close();
      }
    }
    LOG.info("Message successfully published on queue '{}'.", getConnectionName());
  }

  /**
   * Opens the connection if it's currently closed.
   *
   * @return true if it was closed, otherwise false.
   *
   * @throws CannotConnectToQueue If the connection to the queue cannot be established.
   */
  public boolean openIfClosed() throws CannotConnectToQueue {
    if (!isOpen()) {
      open();
      return true;
    }
    LOG.info("Connection to queue '{}' has already been opened. Skipping...",
      getConnectionName()
    );
    return false;
  }

  /**
   * Gets the connection's name, which is used in log messages.
   *
   * @return The connection name
   */
  abstract public String getConnectionName();

  /**
   * Checks whether the connection is open or not.
   *
   * @return true if the connection has been opened, false if not.
   */
  abstract public boolean isOpen();

  /**
   * Does the queue system specific logic to establish a connection to the queue.
   *
   * @throws CannotConnectToQueue If the connection cannot be established.
   */
  abstract protected void openConnection() throws CannotConnectToQueue;

  /**
   * Does the queue system specific logic to register a consumer / listener.
   *
   * @throws CannotRegisterConsumeable If the consumer cannot be registered.
   */
  abstract protected void registerConsumer(Consumeable consumeable) throws CannotRegisterConsumeable;

  /**
   * Does the queue system specific logic to publish a text message on the queue.
   *
   * @param message The message to publish
   *
   * @throws CannotPublishMessage If the message cannot be published.
   */
  abstract protected void publishMessage(String message) throws CannotPublishMessage;

  /**
   * Does the queue system specific logic to publish a bytes message on the queue.
   *
   * @param message The message to publish
   *
   * @throws CannotPublishMessage If the message cannot be published.
   */
  abstract protected void publishMessage(byte[] message) throws CannotPublishMessage;

  /**
   * Does the queue specific logic to close the connection.
   *
   * @throws CannotCloseConnection If the connection cannot be closed.
   */
  abstract protected void closeConnection() throws CannotCloseConnection;

  abstract public static class Builder<ConcreteBuilder extends Builder> {

    protected String host = "localhost";

    protected int port;

    protected String user = "anonymous";

    protected String password;

    protected String queueName;

    private int connectionAttempts = -1;

    private double connectionAttemptsWait = 1;

    /**
     * Sets the host where RabbitMQ is accessible.
     *
     * @param host The host
     *
     * @return self
     */
    public ConcreteBuilder host(String host) {
      this.host = host;
      return (ConcreteBuilder) this;
    }

    /**
     * Sets the port where RabbitMQ is accessible.
     *
     * @param port The port
     *
     * @return self
     */
    public ConcreteBuilder port(int port) {
      this.port = port;
      return (ConcreteBuilder) this;
    }

    /**
     * Sets the user which will be used to establish the connection.
     *
     * @param user The user
     *
     * @return self
     */
    public ConcreteBuilder user(String user) {
      this.user = user;
      return (ConcreteBuilder) this;
    }

    /**
     * Sets the password which will be used to establish the connection.
     *
     * @param password The password
     *
     * @return self
     */
    public ConcreteBuilder password(String password) {
      this.password = password;
      return (ConcreteBuilder) this;
    }

    /**
     * Sets the queue name. Default is a random queue.
     *
     * @param queueName The queue name
     *
     * @return self
     */
    public ConcreteBuilder queueName(String queueName) {
      this.queueName = queueName;
      return (ConcreteBuilder) this;
    }

    /**
     * Sets the amount of connection attempts in order to connect to the queue. Set to -1, if it should try
     * connecting endlessly.
     *
     * @param connectionAttempts The amount of connection attempts
     *
     * @return self
     */
    public ConcreteBuilder connectionAttempts(int connectionAttempts) {
      this.connectionAttempts = connectionAttempts;
      return (ConcreteBuilder) this;
    }

    /**
     * Sets the amount of seconds to wait between each connection attempt. If you want to wait less than 1 second,
     * you can just pass the value as a decimal (exception.g. 0.5 for half a second).
     *
     * @param connectionAttemptWait The amount of seconds to wait.
     *
     * @return self
     */
    public ConcreteBuilder connectionAttemptsWait(double connectionAttemptWait) {
      this.connectionAttemptsWait = connectionAttemptWait;
      return (ConcreteBuilder) this;
    }

    /**
     * Applies property values of a given {@link Properties}.
     *
     * @param properties The properties instance
     *
     * @return self
     */
    public ConcreteBuilder applyProperties(Properties properties) {
      host(properties.getProperty("host", host))
        .port(Integer.parseInt(properties.getProperty("port", Integer.toString(port))))
        .user(properties.getProperty("user", user))
        .password(properties.getProperty("password", password))
        .queueName(properties.getProperty("queue.name", queueName))
        .connectionAttempts(PropertyUtil.getInteger(properties, "connection.attempts", connectionAttempts))
        .connectionAttemptsWait(
          PropertyUtil.getDouble(properties, "connection.attempts.wait", connectionAttemptsWait)
        );
      return (ConcreteBuilder) this;
    }

    /**
     * Applies the property values in a given context of a given {@link Properties}.
     *
     * @param properties The properties instance
     * @param context The properties context
     *
     * @return self
     */
    public ConcreteBuilder applyProperties(Properties properties, String context) {
      return applyProperties(new ContextProperties(properties, context));
    }

    /**
     * Builds the RabbitMQ Connection.
     *
     * @return The RabbitMQ Connection
     *
     */
    abstract public com.github.libgraviton.workerbase.messaging.QueueConnection build() throws CannotBuildConnection;

  }

}
