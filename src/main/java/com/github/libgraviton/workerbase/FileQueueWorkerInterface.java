package com.github.libgraviton.workerbase;

import com.github.libgraviton.gdk.gravitondyn.file.document.File;
import com.github.libgraviton.workerbase.exception.GravitonCommunicationException;
import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.helper.QueueEventScope;

public interface FileQueueWorkerInterface {
  void handleFileRequest(File file, QueueEventScope queueEventScope) throws WorkerException, GravitonCommunicationException;
}
