/**
 * queue consumer - here we have the main logic
 */

package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.messaging.MessageAcknowledger;
import com.github.libgraviton.workerbase.messaging.consumer.AcknowledgingConsumer;
import com.github.libgraviton.workerbase.messaging.consumer.Consumeable;
import com.github.libgraviton.workerbase.messaging.exception.CannotConsumeMessage;

/**
 * <p>WorkerConsumer class.</p>
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/graviton-worker-base-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
public class WorkerConsumer implements AcknowledgingConsumer {

    private final Consumeable consumeable;

    private MessageAcknowledger acknowledger;

    /**
     * constructor
     *
     * @param worker worker
     */
    public WorkerConsumer(final Consumeable consumeable) {
        this.consumeable = consumeable;
    }

    @Override
    public void consume(String messageId, String message) throws CannotConsumeMessage {
        consumeable.onMessage(messageId, message, acknowledger);

    }

    @Override
    public void setAcknowledger(MessageAcknowledger acknowledger) {
        this.acknowledger = acknowledger;
    }
}
