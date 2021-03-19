package com.github.libgraviton.workerbase;

/**
 * a standalone worker that has nothing to do with graviton events can implement this
 */
abstract public class StandaloneWorker extends BaseWorker {

  abstract public void run() throws Exception;

}
