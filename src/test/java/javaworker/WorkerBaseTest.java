package javaworker;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalMatchers;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.mashape.unirest.http.Unirest;
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
        
        verify(jsonResponse, times(1)).getStatus();
        
        assertTrue(testWorker.concerningRequestCalled);
        
        assertTrue(this.outContent.toString().contains("Worker register response code: 204"));
        assertTrue(this.outContent.toString().contains("Subscribed on topic exchange 'graviton' using binding key 'document.core.app.*'"));
        assertTrue(this.outContent.toString().contains("the testworker has been executed!"));
        assertTrue(this.outContent.toString().contains("EVENT = documents.core.app.create"));
        assertTrue(this.outContent.toString().contains("DOCUMENT = http://localhost/core/app/admin"));
        assertTrue(this.outContent.toString().contains("STATUS = http://localhost/event/status/mystatus"));
        
        // register
        verify(requestBodyMock, times(1)).body(contains("{\"id\":\"java-test\""));
        // working
        verify(requestBodyMock, times(1)).body(contains("\"status\":\"working\",\"workerId\":\"java-test\""));
        // done
        verify(requestBodyMock, times(1)).body(contains("\"status\":\"done\",\"workerId\":\"java-test\""));
    }
    
    @Test
    public void testNoAutoWorker() throws IOException {
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
    public void testWorkerException() throws IOException {
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
                        contains("\"content\":\"com.github.libgraviton.workerbase.WorkerException: Something bad happened!\"")
                        )
                );        
    }
    
    @Test
    public void testWorkerExceptionNoAuto() throws IOException {
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
    
    @Test
    public void testVcapConfiguration() throws IOException {
        
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

}
