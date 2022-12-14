package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.exception.WorkerException;

import java.util.Properties;

public interface WorkerInterface {

  String getWorkerId();

  void onStartUp() throws WorkerException;
}
