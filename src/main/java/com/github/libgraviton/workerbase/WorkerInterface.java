package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.exception.WorkerException;

public interface WorkerInterface {

  String getWorkerId();

  void onStartUp() throws WorkerException;

  boolean doHealthCheck() throws WorkerException;
}
