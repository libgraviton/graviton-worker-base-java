package javaworker;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.io.File;
import java.net.URL;

import com.github.libgraviton.workerbase.exception.GravitonCommunicationException;
import com.github.libgraviton.workerbase.model.QueueEvent;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalMatchers;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.HttpRequestWithBody;
import com.mashape.unirest.request.body.RequestBodyEntity;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;

import javaworker.lib.TestWorker;
import javaworker.lib.TestWorkerException;
import javaworker.lib.TestWorkerNoAuto;

@RunWith(PowerMockRunner.class)
@PrepareForTest({com.rabbitmq.client.ConnectionFactory.class,Unirest.class})
public class WorkerBaseTest extends WorkerBaseTestCase {

    @Before
    public void setUp() throws Exception {    
        this.baseMock();                    
    }

    @Test
    public void testBasicExecution() throws Exception {
        TestWorker testWorker = new TestWorker();
        worker = getWrappedWorker(testWorker);
        worker.run();
        
        Envelope envelope = new Envelope(new Long(34343), false, "graviton", "documents.core.app.update");
        URL jsonFile = this.getClass().getClassLoader().getResource("json/queueEvent.json");
        String message = FileUtils.readFileToString(new File(jsonFile.getFile()));
        workerConsumer.handleDelivery("documents.core.app.update", envelope, new AMQP.BasicProperties(), message.getBytes());
        
        verify(stringResponse, times(4)).getStatus();
        
        assertTrue(testWorker.shouldHandleRequestCalled);

        QueueEvent queueEvent = testWorker.getHandledQueueEvent();
        assertEquals("documents.core.app.create", queueEvent.getEvent());
        assertEquals("http://localhost/core/app/admin", queueEvent.getDocument().get$ref());
        assertEquals("http://localhost/event/status/mystatus", queueEvent.getStatus().get$ref());
        
        // register
        verify(requestBodyMock, times(1)).body(contains("{\"id\":\"java-test\""));
        // working
        verify(requestBodyMock, times(1)).body(contains("\"status\":\"working\",\"workerId\":\"java-test\""));
        // done
        verify(requestBodyMock, times(1)).body(contains("\"status\":\"done\",\"workerId\":\"java-test\""));
    }

    @Test
    public void testStatusUpdateOnShouldNotHandleRequest() throws Exception {
        TestWorker testWorker = spy(new TestWorker());
        doReturn(false).when(testWorker).shouldHandleRequest(any(QueueEvent.class));
        worker = getWrappedWorker(testWorker);
        worker.run();

        Envelope envelope = new Envelope(new Long(34343), false, "graviton", "documents.core.app.update");
        URL jsonFile = this.getClass().getClassLoader().getResource("json/queueEvent.json");
        String message = FileUtils.readFileToString(new File(jsonFile.getFile()));
        workerConsumer.handleDelivery("documents.core.app.update", envelope, new AMQP.BasicProperties(), message.getBytes());

        verify(stringResponse, times(3)).getStatus();

        verify(testWorker, times(1)).shouldHandleRequest(any(QueueEvent.class));

        // register
        verify(requestBodyMock, times(1)).body(contains("{\"id\":\"java-test\""));
        // working
        verify(requestBodyMock, never()).body(contains("\"status\":\"working\",\"workerId\":\"java-test\""));
        // done
        verify(requestBodyMock, times(1)).body(contains("\"status\":\"done\",\"workerId\":\"java-test\""));
    }
    
    @Test
    public void testNoAutoWorker() throws Exception {
        TestWorkerNoAuto testWorker = new TestWorkerNoAuto(); 
        worker = getWrappedWorker(testWorker);
        worker.run();
        
        Envelope envelope = new Envelope(new Long(34343), false, "graviton", "documents.core.app.update");
        URL jsonFile = this.getClass().getClassLoader().getResource("json/queueEvent.json");
        String message = FileUtils.readFileToString(new File(jsonFile.getFile()));
        workerConsumer.handleDelivery("documents.core.app.update", envelope, new AMQP.BasicProperties(), message.getBytes());
        
        assertTrue(testWorker.concerningRequestCalled);
        assertTrue(testWorker.handleRequestCalled);
        
        verify(requestBodyMock, times(0)).body(anyString());
        
        // again, this time no request concern
        testWorker.handleRequestCalled = false;
        testWorker.isConcerningRequest = false;
        
        workerConsumer.handleDelivery("documents.core.app.update", envelope, new AMQP.BasicProperties(), message.getBytes());
        
        assertTrue(testWorker.concerningRequestCalled);
        assertFalse(testWorker.handleRequestCalled);        
    }
    
    @Test
    public void testWorkerException() throws Exception {
        TestWorkerException testWorker = new TestWorkerException();        
        worker = getWrappedWorker(testWorker);
        worker.run();
        
        // let worker throw WorkerException        
        Envelope envelope = new Envelope(new Long(34343), false, "graviton", "documents.core.app.update");
        URL jsonFile = this.getClass().getClassLoader().getResource("json/queueEvent.json");
        String message = FileUtils.readFileToString(new File(jsonFile.getFile()));
        workerConsumer.handleDelivery("documents.core.app.update", envelope, new AMQP.BasicProperties(), message.getBytes());
        
        // register
        verify(requestBodyMock, times(1)).body(contains("{\"id\":\"java-test\""));
        // working update
        verify(requestBodyMock, times(1)).body(contains("\"status\":\"working\",\"workerId\":\"java-test\""));
        // failed update
        verify(requestBodyMock, times(1)).body(
                AdditionalMatchers.and(
                        contains("\"status\":\"failed\",\"workerId\":\"java-test\""),
                        contains("\"content\":\"com.github.libgraviton.workerbase.exception.WorkerException: Something bad happened!\"")
                        )
                );        
    }
    
    @Test
    public void testWorkerExceptionNoAuto() throws Exception {
        TestWorkerException testWorker = new TestWorkerException();
        testWorker.doAutoStuff = false;
        worker = getWrappedWorker(testWorker);
        worker.run();
        
        // let worker throw WorkerException        
        Envelope envelope = new Envelope(new Long(34343), false, "graviton", "documents.core.app.update");
        URL jsonFile = this.getClass().getClassLoader().getResource("json/queueEvent.json");
        String message = FileUtils.readFileToString(new File(jsonFile.getFile()));
        workerConsumer.handleDelivery("documents.core.app.update", envelope, new AMQP.BasicProperties(), message.getBytes());
        
        // should be NO call on requestBodyMock
        verify(requestBodyMock, times(0)).body(anyString());
    }
    
    @SuppressWarnings("unchecked")
    @Test (expected = GravitonCommunicationException.class)
    public void testWorkerRegistrationError() throws Exception {
        
        /*** change mocking so we get 400 status on worker registration -> worker shall throw GravitonCommunicationException ***/
        RequestBodyEntity bodyEntityRegister = mock(RequestBodyEntity.class);
        
        HttpResponse<String> stringResponseRegister = (HttpResponse<String>) mock(HttpResponse.class);
        when(stringResponseRegister.getStatus())
            .thenReturn(400);            
        when(bodyEntityRegister.asString())
        .thenReturn(stringResponseRegister); 

        HttpRequestWithBody registerBodyMock = mock(HttpRequestWithBody.class);
        
        when(registerBodyMock.routeParam(anyString(), anyString()))
            .thenReturn(registerBodyMock);
        when(registerBodyMock.header(anyString(), anyString()))
        .thenReturn(registerBodyMock);
        when(registerBodyMock.body(anyString()))
            .thenReturn(bodyEntityRegister);

        // PUT mock
        when(Unirest.put(contains("/event/worker")))
            .thenReturn(registerBodyMock);    
        
        TestWorker testWorker = new TestWorker();
        worker = getWrappedWorker(testWorker);
        worker.run();
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testBackenStatusUpdateError() throws Exception {
        
        /*** change mocking so we get 400 status on worker registration -> worker shall throw GravitonCommunicationException ***/
        RequestBodyEntity bodyEntityRegister = mock(RequestBodyEntity.class);
        
        HttpResponse<String> stringResponseRegister = (HttpResponse<String>) mock(HttpResponse.class);
        when(stringResponseRegister.getStatus())
            .thenReturn(400);            
        when(bodyEntityRegister.asString())
        .thenReturn(stringResponseRegister); 

        HttpRequestWithBody registerBodyMock = mock(HttpRequestWithBody.class);
        
        when(registerBodyMock.routeParam(anyString(), anyString()))
            .thenReturn(registerBodyMock);
        when(registerBodyMock.header(anyString(), anyString()))
        .thenReturn(registerBodyMock);
        when(registerBodyMock.body(anyString()))
            .thenReturn(bodyEntityRegister);

        // PUT mock
        when(Unirest.put(contains("/event/status")))
            .thenReturn(registerBodyMock);    
        
        TestWorker testWorker = new TestWorker();
        worker = getWrappedWorker(testWorker);
        worker.run();
        
        // let worker throw CommunicationException
        Envelope envelope = new Envelope(new Long(34343), false, "graviton", "documents.core.app.update");
        URL jsonFile = this.getClass().getClassLoader().getResource("json/queueEvent.json");
        String message = FileUtils.readFileToString(new File(jsonFile.getFile()));
        workerConsumer.handleDelivery("documents.core.app.update", envelope, new AMQP.BasicProperties(), message.getBytes()); 
    }
    
    @Test
    public void testVcapConfiguration() throws Exception {
        
        TestWorker testWorker = new TestWorker();
        worker = getWrappedWorker(testWorker);
        
        String vcapCreds = "{\"rabbitmq-3.0\": [{\"credentials\": {\"host\": \"test\", \"port\": 32321,"+
                "\"username\": \"hansuser\", \"password\": \"hans22\", \"vhost\": \"hanshost\"}}]}";
        
        when(worker.getVcap())
        .thenReturn(vcapCreds);        
        
        worker.run();

        assertEquals("test", worker.getProperties().getProperty("queue.host"));
        assertEquals("32321", worker.getProperties().getProperty("queue.port"));
        assertEquals("hansuser", worker.getProperties().getProperty("queue.username"));
        assertEquals("hans22", worker.getProperties().getProperty("queue.password"));
        assertEquals("hanshost", worker.getProperties().getProperty("queue.vhost"));        
    }

    @Test(expected = GravitonCommunicationException.class)
    public void testFailedWorkerRegistration() throws Exception {
        when(Unirest.put(contains("/event/worker")).body(anyString()).asString())
                .thenThrow(new UnirestException("Something strange but beautiful happened"));

        TestWorkerException testWorker = new TestWorkerException();
        worker = getWrappedWorker(testWorker);
        worker.run();

    }

}
