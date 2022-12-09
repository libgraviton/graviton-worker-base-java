package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.exception.WorkerException;

public interface StandaloneWorkerInterface {
  void run() throws WorkerException;
}
