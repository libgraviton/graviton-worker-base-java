package com.github.libgraviton.workerbase.messaging.consumer;

import com.github.libgraviton.workerbase.messaging.MessageAcknowledger;
import com.github.libgraviton.workerbase.messaging.exception.CannotConsumeMessage;

final public class WorkerConsumer {

    private final Consumeable consumeable;
    private final MessageAcknowledger acknowledger;

    public WorkerConsumer(final Consumeable consumeable, final MessageAcknowledger acknowledger) {
        this.consumeable = consumeable;
        this.acknowledger = acknowledger;
    }

    public Consumeable getConsumeable() {
        return consumeable;
    }

    public MessageAcknowledger getAcknowledger() {
        return acknowledger;
    }

    public void consume(String messageId, String message) throws CannotConsumeMessage {
        consumeable.onMessage(messageId, message, acknowledger);
    }
}
