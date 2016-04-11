/**
 * Exception to be thrown on a feedback XML processing / event status update failure.
 */

package com.github.libgraviton.workerbase.mq;

/**
 * <p>QueueConnectionException</p>
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/graviton-worker-base-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
public class QueueConnectionException extends Exception {

    private String queueName;

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public QueueConnectionException() {
        super();
    }

    public QueueConnectionException(String message) {
        super(message);
    }

    public QueueConnectionException(String message, String queueName) {
        super(message);
        this.queueName = queueName;
    }

    public QueueConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public QueueConnectionException(String message, String queueName, Throwable cause) {
        super(message, cause);
        this.queueName = queueName;
    }

    public QueueConnectionException(Throwable cause) {
        super(cause);
    }

    protected QueueConnectionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
