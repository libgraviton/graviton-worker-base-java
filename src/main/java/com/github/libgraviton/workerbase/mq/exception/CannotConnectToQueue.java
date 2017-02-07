/**
 * Exception to be thrown on a feedback XML processing / event status update failure.
 */

package com.github.libgraviton.workerbase.mq.exception;

/**
 * <p>CannotConnectToQueue</p>
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/graviton-worker-base-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
public class CannotConnectToQueue extends Exception {

    private String queueName;

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public CannotConnectToQueue() {
        super();
    }

    public CannotConnectToQueue(String message) {
        super(message);
    }

    public CannotConnectToQueue(String message, String queueName) {
        super(message);
        this.queueName = queueName;
    }

    public CannotConnectToQueue(String message, Throwable cause) {
        super(message, cause);
    }

    public CannotConnectToQueue(String message, String queueName, Throwable cause) {
        super(message, cause);
        this.queueName = queueName;
    }

    public CannotConnectToQueue(Throwable cause) {
        super(cause);
    }

    protected CannotConnectToQueue(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
