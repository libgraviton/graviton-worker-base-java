package com.github.libgraviton.workerbase.lib;

import com.github.libgraviton.gdk.gravitondyn.file.document.File;
import com.github.libgraviton.workerbase.FileQueueWorkerAbstract;
import com.github.libgraviton.workerbase.annotation.GravitonWorker;
import com.github.libgraviton.workerbase.helper.QueueEventScope;
import com.github.libgraviton.workerbase.helper.WorkerScope;
import com.github.libgraviton.workerbase.model.QueueEvent;
import io.activej.inject.annotation.Inject;

import java.util.List;

@GravitonWorker
public class TestFileQueueWorker extends FileQueueWorkerAbstract {

    public boolean onStartupCalled = false;

    public File fileObj;
    public boolean actionPresent;

    @Inject
    public TestFileQueueWorker(WorkerScope workerScope) {
        super(workerScope);
    }

    /**
     * worker logic is implemented here
     */
    public void handleFileRequest(File file, QueueEventScope queueEventScope) {
        fileObj = file;
        actionPresent = isActionCommandPresent(this.fileObj, getActionsOfInterest(queueEventScope.getQueueEvent()).get(1));
    }

    @Override
    public List<String> getActionsOfInterest(QueueEvent queueEvent) {
        return List.of("doNotIgnoreThis", "doYourStuff");
    }

    @Override
    public void onStartUp() {
        onStartupCalled = true;
    }
}
