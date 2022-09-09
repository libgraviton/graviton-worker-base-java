package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.gdk.exception.UnsuccessfulResponseException;
import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatus;
import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatusStatus;
import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatusStatusAction;
import com.github.libgraviton.gdk.gravitondyn.eventstatusaction.document.EventStatusAction;
import com.github.libgraviton.gdk.gravitondyn.eventworker.document.EventWorker;
import com.github.libgraviton.gdk.gravitondyn.eventworker.document.EventWorkerSubscription;
import com.github.libgraviton.workerbase.lib.TestQueueWorker;
import com.github.libgraviton.workerbase.lib.TestQueueWorkerException;
import com.github.libgraviton.workerbase.lib.TestQueueWorkerNoAuto;
import com.github.libgraviton.workerbase.model.QueueEvent;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class WorkerBaseTest extends WorkerBaseTestCase {

    @Before
    public void setUp() throws Exception {    
        baseMock();
    }

    @Test
    public void testRegistrationPreparation() throws Exception {
        TestQueueWorker testWorker = prepareTestWorker(new TestQueueWorker());
        // to initialize worker
        worker = getWrappedWorker(testWorker);
        List<EventWorkerSubscription> subscriptions = testWorker.getSubscriptions();
        assertEquals(1, subscriptions.size());

        assertEquals("document.core.app.*", subscriptions.get(0).getEvent());
    }

    @Test
    public void testGetWorkerAction() throws Exception {
        when(gravitonApi
                .getEndpointManager()
                .getEndpoint(EventStatusAction.class.getName())
                .getUrl())
                .thenReturn("http://localhost:8000/event/action/");


        TestQueueWorker testWorker = prepareTestWorker(new TestQueueWorker());
        // to initialize worker
        worker = getWrappedWorker(testWorker);
        EventStatusStatusAction actionRef = testWorker.getWorkerAction();

        assertEquals("http://localhost:8000/event/action/java-test-default", actionRef.get$ref());
    }

    @Test
    public void testBasicExecution() throws Exception {
        TestQueueWorker testWorker = prepareTestWorker(new TestQueueWorker());
        worker = getWrappedWorker(testWorker);
        // execution should work even if message queue ack is not successful
        doThrow(new IOException()).when(queueChannel).basicAck(any(Long.class), any(Boolean.class));
        worker.run();

        URL jsonFile = this.getClass().getClassLoader().getResource("json/queueEvent.json");
        String message = FileUtils.readFileToString(new File(jsonFile.getFile()), Charset.forName("UTF-8"));
        workerConsumer.consume("34343", message);

        assertTrue(testWorker.shouldHandleRequestCalled);

        QueueEvent queueEvent = testWorker.getHandledQueueEvent();
        assertEquals("documents.core.app.create", queueEvent.getEvent());
        assertEquals("http://localhost/core/app/admin", queueEvent.getDocument().get$ref());
        assertEquals("http://localhost/event/status/mystatus", queueEvent.getStatus().get$ref());

        // register
        verify(gravitonApi, times(1)).put(isA(EventWorker.class));

        // 1 execution is due to the mock statement
        // check if event status will be fetched before every update
        verify(gravitonApi, times(3)).get(anyString());
        // working & done
        verify(gravitonApi, times(2)).patch(isA(EventStatus.class));
    }

    @Test
    public void testStatusUpdateOnShouldNotHandleRequest() throws Exception {
        TestQueueWorker testWorker = spy(prepareTestWorker(new TestQueueWorker()));
        doReturn(false).when(testWorker).shouldHandleRequest(any(QueueEvent.class));
        worker = getWrappedWorker(testWorker);
        worker.run();

        URL jsonFile = this.getClass().getClassLoader().getResource("json/queueEvent.json");
        String message = FileUtils.readFileToString(new File(jsonFile.getFile()), Charset.forName("UTF-8"));
        workerConsumer.consume("34343", message);

        verify(testWorker, times(1)).shouldHandleRequest(any(QueueEvent.class));

        // register
        verify(gravitonApi, times(1)).put(isA(EventWorker.class));

        // 1 execution is due to the mock statement
        // check if event status will be fetched before every update
        verify(gravitonApi, times(2)).get(anyString());
        // ignored
        verify(gravitonApi, times(1)).patch(isA(EventStatus.class));
    }
    
    @Test
    public void testNoAutoWorker() throws Exception {
        TestQueueWorkerNoAuto testWorker = spy(prepareTestWorker(new TestQueueWorkerNoAuto()));
        worker = getWrappedWorker(testWorker);
        worker.run();

        URL jsonFile = this.getClass().getClassLoader().getResource("json/queueEvent.json");
        String message = FileUtils.readFileToString(new File(jsonFile.getFile()), Charset.forName("UTF-8"));
        workerConsumer.consume("34343", message);
        
        assertTrue(testWorker.concerningRequestCalled);
        assertTrue(testWorker.handleRequestCalled);

        // register
        verify(gravitonApi, times(0)).put(isA(EventWorker.class));

        // 1 execution is due to the mock statement
        verify(gravitonApi, times(1)).get(anyString());
        // no /event/status update
        verify(gravitonApi, times(0)).patch(isA(EventStatus.class));
        
        // again, this time no request concern
        testWorker.handleRequestCalled = false;
        testWorker.isConcerningRequest = false;

        workerConsumer.consume("34343", message);
        
        assertTrue(testWorker.concerningRequestCalled);
        assertFalse(testWorker.handleRequestCalled);        
    }
    
    @Test
    public void testWorkerException() throws Exception {
        TestQueueWorkerException testWorker = prepareTestWorker(new TestQueueWorkerException());
        worker = getWrappedWorker(testWorker);
        worker.run();
        
        // let worker throw WorkerException
        URL jsonFile = this.getClass().getClassLoader().getResource("json/queueEvent.json");
        String message = FileUtils.readFileToString(new File(jsonFile.getFile()), Charset.forName("UTF-8"));
        workerConsumer.consume("34343", message);

        // register
        verify(gravitonApi, times(1)).put(isA(EventWorker.class));

        // 1 execution is due to the mock statement
        // check if event status will be fetched before every update
        verify(gravitonApi, times(3)).get(anyString());
        // working update & failed update
        verify(gravitonApi, times(2)).patch(isA(EventStatus.class));
    }
    
    @Test
    public void testWorkerExceptionNoAuto() throws Exception {
        TestQueueWorkerException testWorker = prepareTestWorker(new TestQueueWorkerException());
        testWorker.doAutoStuff = false;
        worker = getWrappedWorker(testWorker);
        worker.run();
        
        // let worker throw WorkerException
        URL jsonFile = this.getClass().getClassLoader().getResource("json/queueEvent.json");
        String message = FileUtils.readFileToString(new File(jsonFile.getFile()), Charset.forName("UTF-8"));
        workerConsumer.consume("34343", message);

        // register
        verify(gravitonApi, times(0)).put(isA(EventWorker.class));

        // 1 execution is due to the mock statement
        verify(gravitonApi, times(1)).get(anyString());
        // no /event/status update
        verify(gravitonApi, times(0)).patch(isA(EventStatus.class));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testBackendStatusUpdateError() throws Exception {
        
        /*** change mocking so we get an unsuccessful response on /event/status update -> worker shall throw GravitonCommunicationException ***/
        when(gravitonApi.patch(any(EventStatus.class)).execute()).thenThrow(UnsuccessfulResponseException.class);
        
        TestQueueWorker testWorker = prepareTestWorker(new TestQueueWorker());
        worker = getWrappedWorker(testWorker);
        worker.run();
        
        // let worker throw CommunicationException
        URL jsonFile = this.getClass().getClassLoader().getResource("json/queueEvent.json");
        String message = FileUtils.readFileToString(new File(jsonFile.getFile()), Charset.forName("UTF-8"));
        workerConsumer.consume("34343", message);
    }

    @Test
    public void testIsTerminatedState() throws Exception {
        TestQueueWorker testWorker = prepareTestWorker(new TestQueueWorker());

        assertTrue(testWorker.isTerminatedState(EventStatusStatus.Status.FAILED));
        assertTrue(testWorker.isTerminatedState(EventStatusStatus.Status.DONE));
        assertTrue(testWorker.isTerminatedState(EventStatusStatus.Status.IGNORED));
        assertFalse(testWorker.isTerminatedState(EventStatusStatus.Status.OPENED));
        assertFalse(testWorker.isTerminatedState(EventStatusStatus.Status.WORKING));
    }

    private <T extends QueueWorkerAbstract> T prepareTestWorker(T worker) {
        worker.gravitonApi = gravitonApi;
        return worker;
    }

}
