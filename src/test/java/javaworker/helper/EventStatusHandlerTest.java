package javaworker.helper;

import com.github.libgraviton.workerbase.exception.GravitonCommunicationException;
import com.github.libgraviton.workerbase.helper.EventStatusHandler;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

/**
 * @author List of contributors
 *         <https://github.com/libgraviton/graviton/graphs/contributors>
 * @link http://swisscom.ch
 */
public class EventStatusHandlerTest {

    private String eventStatusUrl = "http://localhost/some/url/";
    private String filterTemplate = "?elemMatch(information,and(eq(content,{requestId}),eq(workerId,{workerId})))&elemMatch(status,and(ne(status,done),eq(workerId,{workerId})))";

    @Test(expected = GravitonCommunicationException.class)
    public void testGetEventStatusByFilterWithNoMatchingResponse() throws GravitonCommunicationException {
        EventStatusHandler statusHandler = spy(new EventStatusHandler(eventStatusUrl));

        JSONArray statusDocuments = new JSONArray();
        doReturn(statusDocuments).when(statusHandler).findEventStatus(filterTemplate);
        statusHandler.getEventStatusByFilter(filterTemplate);
    }

    @Test(expected = GravitonCommunicationException.class)
    public void testGetEventStatusByFilterWithMultipleMatchingResponse() throws GravitonCommunicationException {
        EventStatusHandler statusHandler = spy(new EventStatusHandler(eventStatusUrl));

        JSONArray statusDocuments = new JSONArray();
        statusDocuments.put(new JSONObject());
        statusDocuments.put(new JSONObject());

        doReturn(statusDocuments).when(statusHandler).findEventStatus(filterTemplate);
        statusHandler.getEventStatusByFilter(filterTemplate);
    }

    @Test
    public void testGetEventStatusByFilterWithOneMatchingResponse() throws GravitonCommunicationException {
        EventStatusHandler statusHandler = spy(new EventStatusHandler(eventStatusUrl));

        JSONArray statusDocuments = new JSONArray();
        statusDocuments.put(new JSONObject());
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
