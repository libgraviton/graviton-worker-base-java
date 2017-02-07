package com.github.libgraviton.workerbase.mq.exception;

import com.github.libgraviton.workerbase.mq.Consumer;

import java.io.IOException;

public class CannotRegisterConsumer extends IOException {

    private Consumer consumer;

    public CannotRegisterConsumer(Consumer consumer, Exception cause) {
        super(String.format("Cannot register consumer: '%s'", consumer), cause);
        this.consumer = consumer;
    }

    public Consumer getConsumer() {
        return consumer;
    }

}
