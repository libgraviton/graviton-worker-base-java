package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.exception.WorkerException;

public interface WorkerRunnableInterface {
    void doWork() throws WorkerException;
}
