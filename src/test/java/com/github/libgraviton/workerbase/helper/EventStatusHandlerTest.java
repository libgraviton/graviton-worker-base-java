package com.github.libgraviton.workerbase.helper;

import com.github.libgraviton.workerbase.gdk.GravitonApi;
import com.github.libgraviton.workerbase.gdk.exception.CommunicationException;
import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatus;
import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatusStatus;
import com.github.libgraviton.workerbase.exception.GravitonCommunicationException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author List of contributors
 *         <https://github.com/libgraviton/graviton/graphs/contributors>
 * @link http://swisscom.ch
 */
public class EventStatusHandlerTest {

    private String filterTemplate = "?elemMatch(information,and(eq(content,{requestId}),eq(workerId,{workerId})))&elemMatch(status,and(ne(status,done),eq(workerId,{workerId})))";

    private GravitonApi gravitonApi;

    @Before
    public void setup() throws Exception {
        gravitonApi = mock(GravitonApi.class, RETURNS_DEEP_STUBS);
    }

    @Test(expected = IllegalStateException.class)
    public void testStatusHandlerUpdateWithoutValidStatus() throws GravitonCommunicationException {
        EventStatusHandler statusHandler = spy(new EventStatusHandler(gravitonApi));
        EventStatus eventStatus = new EventStatus();
        EventStatusStatus workerStatus = new EventStatusStatus();
        workerStatus.setWorkerId("workerId");
        workerStatus.setStatus(EventStatusStatus.Status.WORKING);

        statusHandler.update(eventStatus, workerStatus);
    }

    @Test(expected = GravitonCommunicationException.class)
    public void testStatusHandlerUpdateWithFailingBackend() throws Exception {
        EventStatusHandler statusHandler = spy(new EventStatusHandler(gravitonApi));
        EventStatus eventStatus = new EventStatus();
        EventStatusStatus workerStatus = new EventStatusStatus();
        workerStatus.setWorkerId("workerId");
        workerStatus.setStatus(EventStatusStatus.Status.WORKING);
        eventStatus.setStatus(Arrays.asList(workerStatus));

        when(gravitonApi.patch(eventStatus).execute())
                .thenThrow(new CommunicationException("Something strange but beautiful happened"));

        statusHandler.update(eventStatus, workerStatus);
    }

    @Test(expected = GravitonCommunicationException.class)
    public void testGetEventStatusFromUrl() throws Exception {
        EventStatusHandler statusHandler = spy(new EventStatusHandler(gravitonApi));
        String url = "testUrl";
        when(gravitonApi.get(url).execute()).thenThrow(new CommunicationException("Ooops!"));
        statusHandler.getEventStatusFromUrl(url);
    }

    @Test(expected = GravitonCommunicationException.class)
    public void testGetEventStatusByFilterWithNoMatchingResponse() throws GravitonCommunicationException {
        EventStatusHandler statusHandler = spy(new EventStatusHandler(gravitonApi));

        List<EventStatus> statusDocuments = new ArrayList<>();
        doReturn(statusDocuments).when(statusHandler).findEventStatus(filterTemplate);
        statusHandler.getEventStatusByFilter(filterTemplate);
    }

    @Test(expected = GravitonCommunicationException.class)
    public void testGetEventStatusByFilterWithMultipleMatchingResponse() throws GravitonCommunicationException {
        EventStatusHandler statusHandler = spy(new EventStatusHandler(gravitonApi));

        List<EventStatus> statusDocuments = new ArrayList<>();
        statusDocuments.add(new EventStatus());
        statusDocuments.add(new EventStatus());

        doReturn(statusDocuments).when(statusHandler).findEventStatus(filterTemplate);
        statusHandler.getEventStatusByFilter(filterTemplate);
    }

    @Test
    public void testGetEventStatusByFilterWithOneMatchingResponse() throws GravitonCommunicationException {
        EventStatusHandler statusHandler = spy(new EventStatusHandler(gravitonApi));

        List<EventStatus> statusDocuments = new ArrayList<>();
        statusDocuments.add(new EventStatus());

        doReturn(statusDocuments).when(statusHandler).findEventStatus(filterTemplate);
        statusHandler.getEventStatusByFilter(filterTemplate);
    }

    @Test
    public void testGetRqlFilter()  {
        EventStatusHandler statusHandler = spy(new EventStatusHandler(gravitonApi));

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
        EventStatusHandler statusHandler = spy(new EventStatusHandler(gravitonApi));

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
        EventStatusHandler statusHandler = spy(new EventStatusHandler(gravitonApi));

        String expectedFilter = "?elemMatch(information,and(eq(content,myInput),eq(workerId,{workerId})))&elemMatch(status,and(ne(status,done),eq(workerId,{workerId})))";
        String firstParam = "myInput";

        JSONArray statusDocuments = new JSONArray();
        statusDocuments.put(new JSONObject());
        String rqlFilter = statusHandler.getRqlFilter(filterTemplate, firstParam);
        assertEquals(expectedFilter, rqlFilter);
    }
}
