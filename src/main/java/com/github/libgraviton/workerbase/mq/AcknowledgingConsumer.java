package com.github.libgraviton.workerbase.mq;

public interface AcknowledgingConsumer extends Consumer {

    public void setAcknowledger(MessageAcknowledger acknowledger);

}
