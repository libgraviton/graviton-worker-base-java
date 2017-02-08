/**
 * queue consumer - here we have the main logic
 */

package com.github.libgraviton.workerbase;

import com.fasterxml.jackson.jr.ob.JSON;
import com.github.libgraviton.workerbase.model.QueueEvent;
import com.github.libgraviton.workerbase.mq.AcknowledgingConsumer;
import com.github.libgraviton.workerbase.mq.MessageAcknowledger;
import com.github.libgraviton.workerbase.mq.exception.CannotConsumeMessage;
import java.io.IOException;

/**
 * <p>WorkerConsumer class.</p>
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/graviton-worker-base-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
public class WorkerConsumer implements AcknowledgingConsumer {

    private WorkerAbstract worker;

    private MessageAcknowledger acknowledger;

    /**
     * constructor
     *
     * @param worker worker
     */
    public WorkerConsumer(WorkerAbstract worker) {
        this.worker = worker;
    }

    @Override
    public void consume(String messageId, String message) throws CannotConsumeMessage {
        QueueEvent queueEvent;
        try {
            queueEvent = JSON.std.beanFrom(QueueEvent.class, message);
        } catch (IOException e) {
            throw new CannotConsumeMessage(messageId, message, e);
        }
        worker.handleDelivery(queueEvent, messageId, acknowledger);
    }

    @Override
    public void setAcknowledger(MessageAcknowledger acknowledger) {
        this.acknowledger = acknowledger;
    }
}
