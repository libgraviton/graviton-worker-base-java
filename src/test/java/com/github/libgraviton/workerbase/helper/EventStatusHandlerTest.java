package com.github.libgraviton.workerbase.helper;

import com.github.libgraviton.workerbase.exception.GravitonCommunicationException;
import com.github.libgraviton.workerbase.model.status.EventStatus;
import com.github.libgraviton.workerbase.model.status.Status;
import com.github.libgraviton.workerbase.model.status.WorkerStatus;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import com.mashape.unirest.request.body.RequestBodyEntity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * @author List of contributors
 *         <https://github.com/libgraviton/graviton/graphs/contributors>
 * @link http://swisscom.ch
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Unirest.class})
public class EventStatusHandlerTest {

    private String eventStatusUrl = "http://localhost/some/url/";
    private String filterTemplate = "?elemMatch(information,and(eq(content,{requestId}),eq(workerId,{workerId})))&elemMatch(status,and(ne(status,done),eq(workerId,{workerId})))";

    HttpRequestWithBody requestBodyMock;
    RequestBodyEntity bodyEntity;
    GetRequest getRequestStatus;
    @Before
    public void setup() throws Exception {
        mockStatic(Unirest.class);
        /**** UNIREST MOCKING ****/

        requestBodyMock = mock(HttpRequestWithBody.class);
        bodyEntity = mock(RequestBodyEntity.class);

        when(requestBodyMock.header(anyString(), anyString()))
                .thenReturn(requestBodyMock);
        when(requestBodyMock.body(anyString()))
                .thenReturn(bodyEntity);

        // GET mock
        getRequestStatus = mock(GetRequest.class);
        when(getRequestStatus.header(anyString(), anyString()))
                .thenReturn(getRequestStatus);
        when(Unirest.get(anyString()))
                .thenReturn(getRequestStatus);
    }

    @Test(expected = IllegalStateException.class)
    public void testStatusHandlerUpdateWithoutValidStatus() throws GravitonCommunicationException {
        EventStatusHandler statusHandler = spy(new EventStatusHandler(eventStatusUrl));
        EventStatus eventStatus = new EventStatus();
        WorkerStatus workerStatus = new WorkerStatus();
        workerStatus.setWorkerId("workerId");
        workerStatus.setStatus(Status.WORKING);

        statusHandler.update(eventStatus, workerStatus);
    }

    @Test(expected = GravitonCommunicationException.class)
    public void testStatusHandlerUpdateWithFailingBackend() throws Exception {
        EventStatusHandler statusHandler = spy(new EventStatusHandler(eventStatusUrl));
        EventStatus eventStatus = new EventStatus();
        WorkerStatus workerStatus = new WorkerStatus();
        workerStatus.setWorkerId("workerId");
        workerStatus.setStatus(Status.WORKING);
        eventStatus.setStatus(Arrays.asList(workerStatus));

        when(Unirest.put(anyString())).thenReturn(requestBodyMock);
        when(bodyEntity.asString())
                .thenThrow(new UnirestException("Something strange but beautiful happened"));

        statusHandler.update(eventStatus, workerStatus);
    }

    @Test(expected = GravitonCommunicationException.class)
    public void testGetEventStatusFromUrl() throws Exception {
        EventStatusHandler statusHandler = spy(new EventStatusHandler(eventStatusUrl));
        String url = "testUrl";
        when(getRequestStatus.asString())
                .thenThrow(new UnirestException("Ooop!"));
        statusHandler.getEventStatusFromUrl(url);
    }

    @Test(expected = GravitonCommunicationException.class)
    public void testGetEventStatusByFilterWithNoMatchingResponse() throws GravitonCommunicationException {
        EventStatusHandler statusHandler = spy(new EventStatusHandler(eventStatusUrl));

        List<EventStatus> statusDocuments = new ArrayList<>();
        doReturn(statusDocuments).when(statusHandler).findEventStatus(filterTemplate);
        statusHandler.getEventStatusByFilter(filterTemplate);
    }

    @Test(expected = GravitonCommunicationException.class)
    public void testGetEventStatusByFilterWithMultipleMatchingResponse() throws GravitonCommunicationException {
        EventStatusHandler statusHandler = spy(new EventStatusHandler(eventStatusUrl));

        List<EventStatus> statusDocuments = new ArrayList<>();
        statusDocuments.add(new EventStatus());
        statusDocuments.add(new EventStatus());

        doReturn(statusDocuments).when(statusHandler).findEventStatus(filterTemplate);
        statusHandler.getEventStatusByFilter(filterTemplate);
    }

    @Test
    public void testGetEventStatusByFilterWithOneMatchingResponse() throws GravitonCommunicationException {
        EventStatusHandler statusHandler = spy(new EventStatusHandler(eventStatusUrl));

        List<EventStatus> statusDocuments = new ArrayList<>();
        statusDocuments.add(new EventStatus());

        doReturn(statusDocuments).when(statusHandler).findEventStatus(filterTemplate);
        statusHandler.getEventStatusByFilter(filterTemplate);
    }

    @Test
    public void testGetRqlFilter()  {
        EventStatusHandler statusHandler = spy(new EventStatusHandler(eventStatusUrl));

        String expectedFilter = "?elemMatch(information,and(eq(content,myInput),eq(workerId,anotherInput)))&elemMatch(status,and(ne(status,done),eq(workerId,last%2Dinput)))";
        String firstParam = "myInput";
        String secondParam = "anotherInput";
        String thirdParam = "last-input";

        JSONArray statusDocuments = new JSONArray();
        statusDocuments.put(new JSONObject());
        String rqlFilter = statusHandler.getRqlFilter(filterTemplate, firstParam, secondParam, thirdParam);
        assertEquals(expectedFilter, rqlFilter);
    }

    @Test
    public void testGetRqlFilterWithTooManyParams()  {
        EventStatusHandler statusHandler = spy(new EventStatusHandler(eventStatusUrl));

        String expectedFilter = "?elemMatch(information,and(eq(content,myInput),eq(workerId,anotherInput)))&elemMatch(status,and(ne(status,done),eq(workerId,last%2Dinput)))";
        String firstParam = "myInput";
        String secondParam = "anotherInput";
        String thirdParam = "last-input";
        String forthParam = "this-will-never-be-used";

        JSONArray statusDocuments = new JSONArray();
        statusDocuments.put(new JSONObject());
        String rqlFilter = statusHandler.getRqlFilter(filterTemplate, firstParam, secondParam, thirdParam, forthParam);
        assertEquals(expectedFilter, rqlFilter);
    }

    @Test
    public void testGetRqlFilterWithInsufficientParams()  {
        EventStatusHandler statusHandler = spy(new EventStatusHandler(eventStatusUrl));

        String expectedFilter = "?elemMatch(information,and(eq(content,myInput),eq(workerId,{workerId})))&elemMatch(status,and(ne(status,done),eq(workerId,{workerId})))";
        String firstParam = "myInput";

        JSONArray statusDocuments = new JSONArray();
        statusDocuments.put(new JSONObject());
        String rqlFilter = statusHandler.getRqlFilter(filterTemplate, firstParam);
        assertEquals(expectedFilter, rqlFilter);
    }
}
