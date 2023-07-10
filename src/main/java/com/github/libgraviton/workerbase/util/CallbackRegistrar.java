package com.github.libgraviton.workerbase.util;

import com.github.libgraviton.workerbase.WorkerRunnable;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class CallbackRegistrar {
  private final Map<Integer, WorkerRunnable.AfterCompleteCallback> afterCompleteCallbacks = new TreeMap<>();
  private final Map<Integer, WorkerRunnable.AfterExceptionCallback> afterExceptionCallbacks = new TreeMap<>();
  private final Map<Integer, WorkerRunnable.AfterStatusChangeCallback> afterStatusChangeCallbacks = new TreeMap<>();

  public void addAfterCompleteCallback(WorkerRunnable.AfterCompleteCallback callback, Integer priority) {
    afterCompleteCallbacks.put(priority, callback);
  }

  public void addExceptionCallback(WorkerRunnable.AfterExceptionCallback callback, Integer priority) {
    afterExceptionCallbacks.put(priority, callback);
  }

  public void addStatusChangeCallback(WorkerRunnable.AfterStatusChangeCallback callback, Integer priority) {
    afterStatusChangeCallbacks.put(priority, callback);
  }

  public Collection<WorkerRunnable.AfterCompleteCallback> getAndAddAfterCompleteCallback(WorkerRunnable.AfterCompleteCallback callback, Integer priority) {
    Map<Integer, WorkerRunnable.AfterCompleteCallback> callbacks = new TreeMap<>(this.afterCompleteCallbacks);
    callbacks.put(priority, callback);
    return callbacks.values();
  }

  public Collection<WorkerRunnable.AfterExceptionCallback> getAndAddAfterExceptionCallback(WorkerRunnable.AfterExceptionCallback callback, Integer priority) {
    Map<Integer, WorkerRunnable.AfterExceptionCallback> callbacks = new TreeMap<>(this.afterExceptionCallbacks);
    callbacks.put(priority, callback);
    return callbacks.values();
  }

  public Collection<WorkerRunnable.AfterStatusChangeCallback> getAndAddAfterStatusChangeCallback(WorkerRunnable.AfterStatusChangeCallback callback, Integer priority) {
    Map<Integer, WorkerRunnable.AfterStatusChangeCallback> callbacks = new TreeMap<>(this.afterStatusChangeCallbacks);
    callbacks.put(priority, callback);
    return callbacks.values();
  }
}
