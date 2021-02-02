package com.github.libgraviton.workerbase.messaging.consumer;

import com.github.libgraviton.workerbase.messaging.exception.CannotConsumeMessage;

public interface Consumer {

    void consume(String messageId, String message) throws CannotConsumeMessage;

}
