package com.github.libgraviton.workerbase.mq.exception;

import com.github.libgraviton.workerbase.mq.MessageAcknowledger;

import java.io.IOException;

public class CannotAcknowledgeMessage extends IOException {

    private MessageAcknowledger acknowledger;

    private String messageId;

    public CannotAcknowledgeMessage(MessageAcknowledger acknowledger, String messageId, String reason) {
        this(acknowledger, messageId, reason, null);
    }

    public CannotAcknowledgeMessage(MessageAcknowledger acknowledger, String messageId, Exception cause) {
        this(acknowledger, messageId, "An exception occurred", cause);
    }

    private CannotAcknowledgeMessage(
            MessageAcknowledger acknowledger,
            String messageId,
            String reason,
            Exception cause
    ) {
        super(
                String.format(
                        "Acknowledger '%s' is unable to acknowledge message with id '%s'. Reason: '%s'.",
                        acknowledger,
                        messageId,
                        reason
                ),
                cause
        );
        this.acknowledger = acknowledger;
        this.messageId = messageId;
    }

    public MessageAcknowledger getAcknowledger() {
        return acknowledger;
    }

    public String getMessageId() {
        return messageId;
    }

}
