package com.github.libgraviton.workerbase.gdk;

import com.github.libgraviton.gdk.gravitondyn.file.document.File;
import com.github.libgraviton.gdk.gravitondyn.file.document.FileMetadata;
import com.github.libgraviton.gdk.gravitondyn.file.document.FileMetadataAction;
import com.github.libgraviton.workerbase.FileQueueWorkerAbstract;
import com.github.libgraviton.workerbase.exception.GravitonCommunicationException;
import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.gdk.api.Request;
import com.github.libgraviton.workerbase.gdk.api.multipart.Part;
import com.github.libgraviton.workerbase.gdk.exception.SerializationException;
import com.github.libgraviton.workerbase.model.QueueEvent;
import com.github.libgraviton.workertestbase.BaseWorkerTest;
import org.junit.Test;

import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.*;

public class GravitonFileEndpointTest extends BaseWorkerTest {

    private FileQueueWorkerAbstract sut;

    @FunctionalInterface
    private interface FileHandler {
        void handleFileRequest(GravitonFileEndpoint fileEndpoint, QueueEvent body, File file) throws SerializationException, MalformedURLException;
    }

    private FileQueueWorkerAbstract getFileTestWorker(FileHandler fileHandler) {
        return new FileQueueWorkerAbstract() {
            @Override
            public List<String> getActionsOfInterest(QueueEvent queueEvent) {
                return List.of("test");
            }

            @Override
            public void handleFileRequest(QueueEvent body, File file) throws WorkerException, GravitonCommunicationException {
                try {
                    fileHandler.handleFileRequest(fileEndpoint, body, file);
                    assertTrue(true);
                } catch (Throwable t) {
                    throw new WorkerException("Errror", t);
                }
            }
        };
    }

    @Test
    public void testFileHandling() throws Exception {

        File eventFile = new File();
        eventFile.setId("test-fileId");
        FileMetadata fileMetadata = new FileMetadata();
        FileMetadataAction action = new FileMetadataAction();
        action.setCommand("test");
        fileMetadata.setAction(List.of(action));
        eventFile.setMetadata(fileMetadata);

        sut = getFileTestWorker((fileEndpoint, body, file) -> {
            // by comparing this we know that it was fetched through Wiremock and passed to the worker..
            // and by this we're testing FileEndpoint
            assertEquals(eventFile.getId(), file.getId());

            /** test PUT **/
            String data = "SPECIALINFORMATION";
            Request request = fileEndpoint.put(data.getBytes(StandardCharsets.UTF_8), file).build();
            List<Part> parts = request.getParts();
            assertEquals(2, parts.size());

            Part part1 = parts.get(0);
            assertEquals("upload", part1.getFormName());
            assertEquals(data, new String(part1.getBody()));

            Part part2 = parts.get(1);
            assertEquals("metadata", part2.getFormName());
            assertTrue(new String(part2.getBody()).contains("\"id\":\"test-fileId\""));
        });

        prepareWorker(sut);

        produceQueueEvent(sut, eventFile);
    }
}
