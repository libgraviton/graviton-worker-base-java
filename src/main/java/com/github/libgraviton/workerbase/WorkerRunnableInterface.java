package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.exception.GravitonCommunicationException;
import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.model.QueueEvent;

@FunctionalInterface
public interface WorkerRunnableInterface {
    void doWork(QueueEvent queueEvent) throws WorkerException, GravitonCommunicationException;
}
