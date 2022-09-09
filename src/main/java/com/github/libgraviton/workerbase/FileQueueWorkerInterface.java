package com.github.libgraviton.workerbase;

import com.github.libgraviton.gdk.gravitondyn.file.document.File;
import com.github.libgraviton.workerbase.exception.GravitonCommunicationException;
import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.model.QueueEvent;

public interface FileQueueWorkerInterface {
  void handleFileRequest(QueueEvent body, File file) throws WorkerException, GravitonCommunicationException;
}
