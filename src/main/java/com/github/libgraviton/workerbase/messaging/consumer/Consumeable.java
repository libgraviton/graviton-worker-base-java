package com.github.libgraviton.workerbase.messaging.consumer;

import com.github.libgraviton.workerbase.messaging.MessageAcknowledger;

@FunctionalInterface
public interface Consumeable {
    void onMessage(String messageId, String message, MessageAcknowledger acknowledger);
}
