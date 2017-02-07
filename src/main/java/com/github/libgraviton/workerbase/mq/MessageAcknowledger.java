package com.github.libgraviton.workerbase.mq;

import com.github.libgraviton.workerbase.mq.exception.CannotAcknowledgeMessage;

public interface MessageAcknowledger {

    public void acknowledge(String messageId) throws CannotAcknowledgeMessage;

}
