package com.github.libgraviton.workerbase.messaging.consumer;

import com.github.libgraviton.workerbase.messaging.MessageAcknowledger;
import com.github.libgraviton.workerbase.messaging.consumer.Consumer;

public interface AcknowledgingConsumer extends Consumer {

    void setAcknowledger(MessageAcknowledger acknowledger);

}
