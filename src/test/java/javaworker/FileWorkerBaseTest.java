package javaworker;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Mockito.*;
import static org.hamcrest.CoreMatchers.instanceOf;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalMatchers;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.github.libgraviton.workerbase.model.GravitonFileMetadata;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.GetRequest;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;

import javaworker.lib.TestFileWorker;

@RunWith(PowerMockRunner.class)
@PrepareForTest({com.rabbitmq.client.ConnectionFactory.class,Unirest.class})
public class FileWorkerBaseTest extends WorkerBaseTestCase {

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {    
        this.baseMock();                
        
        // GET /file mock        
        URL fileResponseUrl = this.getClass().getClassLoader().getResource("json/fileResource.json");
        String fileResponseContent = FileUtils.readFileToString(new File(fileResponseUrl.getFile()));
        GetRequest getRequestStatus = mock(GetRequest.class);

        HttpResponse<String> statusResponse = (HttpResponse<String>) mock(HttpResponse.class);
        when(statusResponse.getBody())
            .thenReturn(fileResponseContent);        
        when(getRequestStatus.header(anyString(), anyString()))
            .thenReturn(getRequestStatus);
        when(getRequestStatus.asString())
            .thenReturn(statusResponse);        
        when(Unirest.get(contains("/file/16f52c4b00523e2ba27480ce6905ed1d")))
            .thenReturn(getRequestStatus);
        
        URL fileResponseActionUrl = this.getClass().getClassLoader().getResource("json/fileResourceWithAction.json");
        String fileResponseActionContent = FileUtils.readFileToString(new File(fileResponseActionUrl.getFile()));
        GetRequest getRequestActionStatus = mock(GetRequest.class);        
        
        HttpResponse<String> statusResponseAction = (HttpResponse<String>) mock(HttpResponse.class);
        when(statusResponseAction.getBody())
            .thenReturn(fileResponseActionContent);        
        when(getRequestActionStatus.header(anyString(), anyString()))
            .thenReturn(getRequestActionStatus);
        when(getRequestActionStatus.asString())
            .thenReturn(statusResponseAction);        
        when(Unirest.get(contains("/file/16f52c4b00523e2ba27480ce6905ed1e")))
            .thenReturn(getRequestActionStatus);                 
        
    }

    @Test
    public void testBasicFileHandling() throws Exception {
        TestFileWorker testWorker = new TestFileWorker();
        worker = getWrappedWorker(testWorker);
        worker.run();
        
        Envelope envelope = new Envelope(new Long(34343), false, "graviton", "document.file.file.create");
        URL jsonFile = this.getClass().getClassLoader().getResource("json/queueFileEvent.json");
        String message = FileUtils.readFileToString(new File(jsonFile.getFile()));
        workerConsumer.handleDelivery("document.file.file.create", envelope, new AMQP.BasicProperties(), message.getBytes());
        
        verify(stringResponse, times(3)).getStatus();
        
        assertEquals("16f52c4b00523e2ba27480ce6905ed1d", testWorker.fileObj.getId());
        assertEquals("dude", testWorker.fileObj.getLinks().get(0).getType());
        assertEquals("http://localhost:8000/dude", testWorker.fileObj.getLinks().get(0).get$ref());
        assertEquals(new Integer(200), testWorker.fileObj.metadata.getSize());
        assertEquals("image/jpeg", testWorker.fileObj.metadata.getMime());
        
        assertEquals("2015-09-28T06:46:15+0000", testWorker.fileObj.metadata.getCreateDate());
        assertEquals("2015-09-28T06:46:15+0000", testWorker.fileObj.metadata.getModificationDate());
        assertEquals("hans.txt", testWorker.fileObj.metadata.getFilename());
        
        assertEquals(0, testWorker.fileObj.metadata.getAction().size());
        
        assertThat(testWorker.fileObj.getMetadata(), instanceOf(GravitonFileMetadata.class));
        
        assertFalse(testWorker.actionPresent);
    }
    
    @Test
    public void testActionHandling() throws Exception {
        TestFileWorker testWorker = new TestFileWorker();
        worker = getWrappedWorker(testWorker);
        worker.run();
        
        Envelope envelope = new Envelope(new Long(34343), false, "graviton", "document.file.file.create");
        URL jsonFile = this.getClass().getClassLoader().getResource("json/queueFileEventWithAction.json");
        String message = FileUtils.readFileToString(new File(jsonFile.getFile()));
        workerConsumer.handleDelivery("document.file.file.create", envelope, new AMQP.BasicProperties(), message.getBytes());     
        
        verify(stringResponse, times(3)).getStatus();
        
        assertEquals("16f52c4b00523e2ba27480ce6905ed1e", testWorker.fileObj.getId());
        
        // see action stuff
        assertEquals(3, testWorker.fileObj.metadata.getAction().size());
        assertTrue(testWorker.actionPresent);
        
        // register
        verify(requestBodyMock, times(1)).body(contains("{\"id\":\"java-test\""));
        // working update
        verify(requestBodyMock, times(1)).body(contains("\"status\":\"working\",\"workerId\":\"java-test\""));
        // failed update
        verify(requestBodyMock, times(1)).body(
                AdditionalMatchers.and(
                        not(contains("\"command\":\"doYourStuff\"")),
                        contains("\"command\":\"anotherCommand\"")
                        )
                );           
    }

}
