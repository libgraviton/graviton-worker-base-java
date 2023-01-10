package com.github.libgraviton.workerbase.lib;

import com.github.libgraviton.gdk.gravitondyn.file.document.File;
import com.github.libgraviton.workerbase.FileQueueWorkerAbstract;
import com.github.libgraviton.workerbase.annotation.GravitonWorker;
import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.helper.QueueEventScope;
import com.github.libgraviton.workerbase.helper.WorkerScope;
import com.github.libgraviton.workerbase.model.QueueEvent;
import io.activej.inject.annotation.Inject;

import java.util.List;

@GravitonWorker
public class TestFileQueueWorkerIgnoredAction extends FileQueueWorkerAbstract {

    public boolean onStartupCalled = false;
    public boolean handleFileRequestCalled = false;

    @Inject
    public TestFileQueueWorkerIgnoredAction(WorkerScope workerScope) {
        super(workerScope);
    }

    public void handleFileRequest(File file, QueueEventScope queueEventScope) throws WorkerException {
        handleFileRequestCalled = true;
    }

    @Override
    public List<String> getActionsOfInterest(QueueEvent queueEvent) {
        return List.of("iWantThis", "orThat");
    }

    @Override
    public void onStartUp() {
        onStartupCalled = true;
    }
}
