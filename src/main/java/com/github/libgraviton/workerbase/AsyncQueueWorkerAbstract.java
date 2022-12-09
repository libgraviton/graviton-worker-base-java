package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.helper.QueueEventScope;
import com.github.libgraviton.workerbase.model.QueueEvent;

import java.util.Properties;

public abstract class AsyncQueueWorkerAbstract extends QueueWorkerAbstract implements AsyncQueueWorkerInterface {

    @Override
    final public void handleRequest(QueueEvent body, QueueEventScope queueEventScope) {
    }
}
