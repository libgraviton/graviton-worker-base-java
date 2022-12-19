package com.github.libgraviton.workerbase.lib;

import com.github.libgraviton.gdk.gravitondyn.file.document.File;
import com.github.libgraviton.workerbase.FileQueueWorkerAbstract;
import com.github.libgraviton.workerbase.annotation.GravitonWorker;
import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.gdk.api.Response;
import com.github.libgraviton.workerbase.helper.QueueEventScope;
import com.github.libgraviton.workerbase.helper.WorkerProperties;
import com.github.libgraviton.workerbase.helper.WorkerScope;
import com.github.libgraviton.workerbase.model.QueueEvent;
import io.activej.inject.annotation.Inject;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;

import java.nio.charset.StandardCharsets;
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
    public void handleFileRequest(File file, QueueEventScope queueEventScope) throws WorkerException {
        fileObj = file;

        Response simpleFetchResponse = null;
        Response uploadFileResponse = null;
        java.io.File downloadFile = null;
        String tmpFileDestination = "/tmp/tmpfile-testing";

        try {
            // test transient header handling
            simpleFetchResponse = queueEventScope.getGravitonApi().get(WorkerProperties.GRAVITON_BASE_URL.get()+"/core/app/hans").execute();


            // upload file with metadata
            File uploadFile = new File();
            uploadFile.setId("test-grv-file");

            String fileContent = "theContent";
            uploadFileResponse = queueEventScope.getFileEndpoint().put(fileContent.getBytes(), uploadFile).execute();

            // get a files content! -> to File
            downloadFile = queueEventScope.getFileEndpoint().getTempFile();
            queueEventScope.getFileEndpoint().writeFileContentToDisk("test-grv-file", downloadFile);

            // and to other file
            queueEventScope.getFileEndpoint().writeFileContentToDisk("test-grv-file", tmpFileDestination);

        } catch (Throwable t) {
            throw new WorkerException("hans");
        }

        Assertions.assertEquals(200, simpleFetchResponse.getCode());
        Assertions.assertEquals(201, uploadFileResponse.getCode());

        try {
            String content = FileUtils.readFileToString(downloadFile, StandardCharsets.UTF_8);
            Assertions.assertEquals("THIS-IS-THE-CONTENT", content);

            java.io.File fileContent = new java.io.File(tmpFileDestination);
            String content2 = FileUtils.readFileToString(fileContent, StandardCharsets.UTF_8);
            Assertions.assertEquals("THIS-IS-THE-CONTENT", content2);
        } catch (Throwable t) {
            throw new WorkerException("Error", t);
        }

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
