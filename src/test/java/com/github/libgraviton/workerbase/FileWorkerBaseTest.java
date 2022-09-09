package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.gdk.GravitonFileEndpoint;
import com.github.libgraviton.workerbase.gdk.api.Response;
import com.github.libgraviton.workerbase.gdk.data.GravitonBase;
import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatus;
import com.github.libgraviton.gdk.gravitondyn.eventworker.document.EventWorker;
import com.github.libgraviton.gdk.gravitondyn.file.document.FileMetadata;
import com.github.libgraviton.gdk.gravitondyn.file.document.FileMetadataAction;
import com.github.libgraviton.workerbase.helper.WorkerUtil;
import com.github.libgraviton.workerbase.lib.TestFileQueueWorker;
import com.github.libgraviton.workerbase.model.GravitonRef;
import com.github.libgraviton.workerbase.model.QueueEvent;
import mockit.*;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

public class FileWorkerBaseTest extends WorkerBaseTestCase {

    protected Response response1;

    protected Response response2;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {    
        baseMock();
        
        // GET /file metadata mock

        String metadata1 = FileUtils.readFileToString(
                new File("src/test/resources/json/fileResource.json"), StandardCharsets.UTF_8);
        response1 = spy(Response.class);
        response1.setObjectMapper(objectMapper);
        doReturn(metadata1).when(response1).getBody();
        doCallRealMethod().when(response1).getBodyItem(eq(GravitonBase.class));
        when(gravitonApi.get(contains("/file/16f52c4b00523e2ba27480ce6905ed1d")).execute())
                .thenReturn(response1);

        String metadata2 = FileUtils.readFileToString(
                new File("src/test/resources/json/fileResourceWithAction.json"), StandardCharsets.UTF_8);
        response2 = spy(Response.class);
        response2.setObjectMapper(objectMapper);
        doReturn(metadata2).when(response2).getBody();
        doCallRealMethod().when(response2).getBodyItem(eq(GravitonBase.class));
        when(gravitonApi.get(contains("/file/16f52c4b00523e2ba27480ce6905ed1e")).execute())
                .thenReturn(response2);
    }

    @Test
    public void testBasicFileHandling() throws Exception {
        TestFileQueueWorker testWorker = prepareTestWorker(new TestFileQueueWorker());
        worker = getWrappedWorker(testWorker);
        worker.run();

        String message = FileUtils.readFileToString(
                new File("src/test/resources/json/queueFileEvent.json"), StandardCharsets.UTF_8);

        workerConsumer.consume("34343", message);

        // register
        verify(gravitonApi, times(1)).put(isA(EventWorker.class));

        verify(gravitonApi, times(2)).get(contains("/event/status/"));
        verify(gravitonApi, times(1)).get(contains("/file/"));
        // working update & done update
        verify(gravitonApi, times(2)).patch(isA(EventStatus.class));

        assertEquals("16f52c4b00523e2ba27480ce6905ed1d", testWorker.fileObj.getId());
        assertEquals("dude", testWorker.fileObj.getLinks().get(0).getType());
        assertEquals("http://localhost:8000/dude", testWorker.fileObj.getLinks().get(0).get$ref());
        assertEquals(Integer.valueOf(200), testWorker.fileObj.getMetadata().getSize());
        assertEquals("image/jpeg", testWorker.fileObj.getMetadata().getMime());

        DateFormat dateFormat = objectMapper.getDateFormat();
        Date createDate = testWorker.fileObj.getMetadata().getCreateDate();
        Date modificationDate = testWorker.fileObj.getMetadata().getModificationDate();

        assertEquals("2015-09-28T06:46:15+0000", dateFormat.format(createDate));
        assertEquals("2015-09-28T06:46:15+0000", dateFormat.format(modificationDate));
        assertEquals("hans.txt", testWorker.fileObj.getMetadata().getFilename());
        assertEquals("6129ec9222853b13723e07a2404091b9e8bbe6728e4836cd8a0ea06939e74fb7",
                testWorker.fileObj.getMetadata().getHash());

        assertEquals(0, testWorker.fileObj.getMetadata().getAction().size());

        assertTrue(testWorker.fileObj.getMetadata() != null);
        
        assertFalse(testWorker.actionPresent);
    }
    
    @Test
    public void testActionHandling() throws Exception {
        TestFileQueueWorker testWorker = prepareTestWorker(new TestFileQueueWorker());
        worker = getWrappedWorker(testWorker);
        worker.run();

        String message = FileUtils.readFileToString(
                new File("src/test/resources/json/queueFileEventWithAction.json"), StandardCharsets.UTF_8);
        workerConsumer.consume("34343", message);

        // register
        verify(gravitonApi, times(1)).put(isA(EventWorker.class));

        verify(gravitonApi, times(2)).get(contains("/event/status/"));
        verify(gravitonApi, times(1)).get(contains("/file/"));
        // working update & done update
        verify(gravitonApi, times(2)).patch(isA(EventStatus.class));

        assertEquals("16f52c4b00523e2ba27480ce6905ed1e", testWorker.fileObj.getId());
        
        // see action stuff
        assertEquals(3, testWorker.fileObj.getMetadata().getAction().size());
        assertTrue(testWorker.actionPresent);

    }

    @Test
    public void testCachedGravitonFileFetching() throws Exception {
        com.github.libgraviton.gdk.gravitondyn.file.document.File file = new com.github.libgraviton.gdk.gravitondyn.file.document.File();
        file.setId("someTestId");

        new MockUp<WorkerUtil>() {
            @Mock
            public com.github.libgraviton.gdk.gravitondyn.file.document.File getGravitonFile(Invocation invocation, GravitonFileEndpoint fileEndpoint, String fileUrl) {
                return file;
            }
        };

        String file1Url = "testFile1";
        TestFileQueueWorker testFileWorker = prepareTestWorker(new TestFileQueueWorker());

        com.github.libgraviton.gdk.gravitondyn.file.document.File firstFile = testFileWorker.getGravitonFile(file1Url);
        assertEquals(file, firstFile);
    }

    @Test
    public void testShouldHandleRequestWithoutActions() throws Exception {
        TestFileQueueWorker testFileWorker = spy(prepareTestWorker(new TestFileQueueWorker()));
        testFileWorker.shouldHandleRequestMocked = false;
        QueueEvent queueEvent = new QueueEvent();
        GravitonRef gravitonRef = new GravitonRef();
        String documentUrl = "testDocumentUrl";
        gravitonRef.set$ref(documentUrl);
        queueEvent.setDocument(new GravitonRef());
        queueEvent.setCoreUserId("someId123");
        List<String> noActions = Arrays.asList();

        doReturn(noActions).when(testFileWorker).getActionsOfInterest(queueEvent);
        doNothing().when(testFileWorker).removeFileActionCommand(any(), anyList());

        assertFalse(testFileWorker.shouldHandleRequest(queueEvent));
        verify(testFileWorker, never()).removeFileActionCommand(any(), anyList());
    }

    @Test
    public void testShouldHandleRequestWithActions() throws Exception {
        TestFileQueueWorker testFileWorker = spy(prepareTestWorker(new TestFileQueueWorker()));
        testFileWorker.shouldHandleRequestMocked = false;
        QueueEvent queueEvent = new QueueEvent();
        GravitonRef gravitonRef = new GravitonRef();
        String documentUrl = "testDocumentUrl";
        gravitonRef.set$ref(documentUrl);
        queueEvent.setDocument(gravitonRef);
        queueEvent.setCoreUserId("someId123");
        String action1 = "action1";
        String action2 = "action2";
        List<String> noActions = Arrays.asList(action1, action2);

        com.github.libgraviton.gdk.gravitondyn.file.document.File file = new com.github.libgraviton.gdk.gravitondyn.file.document.File();
        file.setId("someTestId");
        FileMetadata metadata = new FileMetadata();
        FileMetadataAction metadataAction1 = new FileMetadataAction();
        metadataAction1.setCommand(action1);
        FileMetadataAction metadataAction2 = new FileMetadataAction();
        metadataAction2.setCommand(action2);
        metadata.setAction(Arrays.asList(metadataAction1, metadataAction2));
        file.setMetadata(metadata);

        mockStatic(WorkerUtil.class);
        when(WorkerUtil.getGravitonFile(any(GravitonFileEndpoint.class), anyString())).thenReturn(file);
        when(WorkerUtil.encodeRql(any())).thenCallRealMethod();

        doReturn(noActions).when(testFileWorker).getActionsOfInterest(queueEvent);
        doNothing().when(testFileWorker).removeFileActionCommand(eq(file), anyList());

        assertTrue(testFileWorker.shouldHandleRequest(queueEvent));
        verify(testFileWorker, times(1)).removeFileActionCommand(eq(file), anyList());

        assertEquals("someId123", queueEvent.getCoreUserId());
    }

    private <T extends FileQueueWorkerAbstract> T prepareTestWorker(T worker) {
        worker.gravitonApi = gravitonApi;
        worker.fileEndpoint = new GravitonFileEndpoint(gravitonApi);
        return worker;
    }

}
