package com.github.libgraviton.workerbase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.libgraviton.gdk.GravitonApi;
import com.github.libgraviton.gdk.api.GravitonResponse;
import com.github.libgraviton.gdk.data.GravitonBase;
import com.github.libgraviton.workerbase.mq.WorkerQueueManager;
import com.rabbitmq.client.Channel;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.powermock.api.mockito.PowerMockito;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import static org.mockito.Mockito.*;

public abstract class WorkerBaseTestCase {
    
    protected final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    protected final ByteArrayOutputStream errContent = new ByteArrayOutputStream();    
    
    protected Worker worker;
    protected WorkerConsumer workerConsumer;
    protected Channel queueChannel;
    protected GravitonApi gravitonApi;
    protected GravitonResponse response;
    protected ObjectMapper objectMapper;

    @SuppressWarnings("unchecked")
    @Before
    public void baseMock() throws Exception {
        
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));

        objectMapper = initObjectMapper();

        response = mock(GravitonResponse.class);
        gravitonApi = mock(GravitonApi.class, RETURNS_DEEP_STUBS);

        // PUT mock
        when(gravitonApi.put(any(GravitonBase.class)).execute()).thenReturn(response);

        // PATCH mock
        when(gravitonApi.patch(any(GravitonBase.class)).execute()).thenReturn(response);
        
        // GET /event/status mock
        String body = FileUtils.readFileToString(
                new File("src/test/resources/json/statusResponse.json"));
        GravitonResponse eventStatusResponse = spy(GravitonResponse.class);
        eventStatusResponse.setObjectMapper(objectMapper);
        doReturn(body).when(eventStatusResponse).getBody();
        doCallRealMethod().when(eventStatusResponse).getBodyItem(eq(GravitonBase.class));
        when(gravitonApi.get(contains("/event/status")).execute()).thenReturn(eventStatusResponse);
    }
    
    protected Worker getWrappedWorker(WorkerAbstract testWorker) throws Exception {
        worker = spy(new Worker(testWorker));
        queueChannel = mock(Channel.class);
        workerConsumer = PowerMockito.spy(new WorkerConsumer(queueChannel, testWorker, "testQueueName"));

        WorkerQueueManager queueManager = mock(WorkerQueueManager.class);
        when(worker.getQueueManager()).thenReturn(queueManager);
        doNothing().when(queueManager).connect();
        
        return worker;
    }

    protected ObjectMapper initObjectMapper() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return new ObjectMapper().setDateFormat(dateFormat);
    }

}
