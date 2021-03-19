package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.exception.WorkerException;
import java.util.Properties;

public interface WorkerInterface {
  void initialize(Properties properties) throws Exception;

  void onStartUp() throws WorkerException;
}
