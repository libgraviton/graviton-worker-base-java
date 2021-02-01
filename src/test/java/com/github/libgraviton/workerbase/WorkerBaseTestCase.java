package com.github.libgraviton.workerbase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.libgraviton.workerbase.gdk.api.Response;
import com.github.libgraviton.workerbase.gdk.data.GravitonBase;
import com.github.libgraviton.workerbase.gdk.serialization.mapper.GravitonObjectMapper;
import com.github.libgraviton.workerbase.gdk.util.PropertiesLoader;
import com.github.libgraviton.workerbase.messaging.MessageAcknowledger;
import com.rabbitmq.client.Channel;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.powermock.api.mockito.PowerMockito;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Properties;

import static org.mockito.Mockito.*;

public abstract class WorkerBaseTestCase {
    
    protected final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    protected final ByteArrayOutputStream errContent = new ByteArrayOutputStream();    
    
    protected Worker worker;
    protected WorkerConsumer workerConsumer;
    protected Channel queueChannel;
    protected GravitonAuthApi gravitonApi;
    protected Response response;
    protected ObjectMapper objectMapper;

    @SuppressWarnings("unchecked")
    @Before
    public void baseMock() throws Exception {
        
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));

        Properties properties = PropertiesLoader.load();
        objectMapper = new GravitonObjectMapper(properties);

        response = mock(Response.class);
        gravitonApi = mock(GravitonAuthApi.class, RETURNS_DEEP_STUBS);

        // PUT mock
        when(gravitonApi.put(any(GravitonBase.class)).execute()).thenReturn(response);

        // PATCH mock
        when(gravitonApi.patch(any(GravitonBase.class)).execute()).thenReturn(response);
        
        // GET /event/status mock
        String body = FileUtils.readFileToString(
                new File("src/test/resources/json/statusResponse.json"), Charset.forName("UTF-8"));
        Response eventStatusResponse = spy(Response.class);
        eventStatusResponse.setObjectMapper(objectMapper);
        doReturn(body).when(eventStatusResponse).getBody();
        doCallRealMethod().when(eventStatusResponse).getBodyItem(eq(GravitonBase.class));
        when(gravitonApi.get(contains("/event/status")).execute()).thenReturn(eventStatusResponse);

        // url mock
        when(gravitonApi.getBaseUrl()).thenReturn("http://localhost:8080/");
    }
    
    protected Worker getWrappedWorker(WorkerAbstract testWorker) throws Exception {
        worker = spy(new Worker(testWorker));
        queueChannel = mock(Channel.class);
        workerConsumer = PowerMockito.spy(new WorkerConsumer(testWorker));
        workerConsumer.setAcknowledger(mock(MessageAcknowledger.class));

        QueueManager queueManager = mock(QueueManager.class);
        doReturn(queueManager).when(worker).getQueueManager();
        doNothing().when(queueManager).connect(any(WorkerAbstract.class));

        return worker;
    }
}
