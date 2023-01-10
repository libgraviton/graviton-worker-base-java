package com.github.libgraviton.workerbase.messaging.exception;

import com.github.libgraviton.workerbase.messaging.consumer.Consumeable;

import java.io.IOException;

public class CannotRegisterConsumeable extends IOException {

    public CannotRegisterConsumeable(Consumeable consumeable, String reason) {
        this(consumeable, reason, null);
    }

    public CannotRegisterConsumeable(Consumeable consumeable, Exception cause) {
        this(consumeable, "An Exception occurred.", cause);
    }

    private CannotRegisterConsumeable(Consumeable consumeable, String reason, Throwable cause) {
        super(String.format("Cannot register consumeable '%s'. Reason: '%s'", consumeable.getClass().getName(), reason), cause);
    }

}
