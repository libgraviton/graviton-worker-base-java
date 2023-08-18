package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.helper.WorkerScope;

/**
 * a standalone worker that has nothing to do with graviton events can implement this
 */
abstract public class StandaloneWorker extends BaseWorker implements StandaloneWorkerInterface {
    public StandaloneWorker(WorkerScope workerScope) {
        super(workerScope);
    }

    public boolean doHealthCheck() {
        LOG.warn(
          "This is a standalone worker, but there is no healthcheck defined! Please define one by implementing doHealthCheck() in your worker class!"
        );
        return true;
    }
}
