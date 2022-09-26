package com.github.libgraviton.workerbase.messaging;

import com.github.libgraviton.workerbase.messaging.exception.CannotAcknowledgeMessage;

public interface MessageAcknowledger {

    void acknowledge(String messageId) throws CannotAcknowledgeMessage;

    void acknowledgeFail(String messageId) throws CannotAcknowledgeMessage;

}
