package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.exception.GravitonCommunicationException;
import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.helper.QueueEventScope;
import com.github.libgraviton.workerbase.model.QueueEvent;

@FunctionalInterface
public interface WorkerRunnableInterface {
    void doWork(QueueEvent queueEvent, QueueEventScope queueEventScope) throws WorkerException, GravitonCommunicationException;
}
