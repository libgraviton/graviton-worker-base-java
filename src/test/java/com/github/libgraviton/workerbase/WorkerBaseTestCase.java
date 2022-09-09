package com.github.libgraviton.workerbase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.libgraviton.workerbase.gdk.GravitonAuthApi;
import com.github.libgraviton.workerbase.gdk.api.Response;
import com.github.libgraviton.workerbase.gdk.data.GravitonBase;
import com.github.libgraviton.workerbase.gdk.serialization.mapper.GravitonObjectMapper;
import com.github.libgraviton.workerbase.gdk.util.PropertiesLoader;
import com.github.libgraviton.workerbase.messaging.MessageAcknowledger;
import com.rabbitmq.client.Channel;
import org.apache.commons.io.FileUtils;
import org.junit.Before;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static org.mockito.Mockito.*;

public abstract class WorkerBaseTestCase {

    protected Worker worker;
    protected WorkerConsumer workerConsumer;
    protected Channel queueChannel;
    protected GravitonAuthApi gravitonApi;
    protected Response response;
    protected ObjectMapper objectMapper;

    @SuppressWarnings("unchecked")
    @Before
    public void baseMock() throws Exception {

        Properties properties = PropertiesLoader.load();
        objectMapper = GravitonObjectMapper.getInstance(properties);

        response = mock(Response.class);
        gravitonApi = mock(GravitonAuthApi.class, RETURNS_DEEP_STUBS);

        // PUT mock
        when(gravitonApi.put(any(GravitonBase.class)).execute()).thenReturn(response);

        // PATCH mock
        when(gravitonApi.patch(any(GravitonBase.class)).execute()).thenReturn(response);
        
        // GET /event/status mock
        String body = FileUtils.readFileToString(
                new File("src/test/resources/json/statusResponse.json"), StandardCharsets.UTF_8);
        Response eventStatusResponse = spy(Response.class);
        eventStatusResponse.setObjectMapper(objectMapper);
        doReturn(body).when(eventStatusResponse).getBody();
        doCallRealMethod().when(eventStatusResponse).getBodyItem(eq(GravitonBase.class));
        when(gravitonApi.get(contains("/event/status")).execute()).thenReturn(eventStatusResponse);

        // url mock
        when(gravitonApi.getBaseUrl()).thenReturn("http://localhost:8080/");
    }
    
    protected Worker getWrappedWorker(QueueWorkerAbstract testWorker) throws Exception {
        worker = spy(new Worker(testWorker));
        queueChannel = mock(Channel.class);
        workerConsumer = spy(new WorkerConsumer(testWorker));
        workerConsumer.setAcknowledger(mock(MessageAcknowledger.class));

        QueueManager queueManager = mock(QueueManager.class);
        //doReturn(queueManager).when(worker).getQueueManager();
        doNothing().when(queueManager).connect(any(QueueWorkerAbstract.class));

        return worker;
    }
}
