package com.github.libgraviton.workerbase.mq;

import com.github.libgraviton.workerbase.mq.exception.CannotConsumeMessage;

public interface Consumer {

    public void consume(String messageId, String message) throws CannotConsumeMessage;

}
