package com.github.libgraviton.workerbase.helper;

import com.github.libgraviton.workerbase.gdk.GravitonApi;
import com.github.libgraviton.workerbase.gdk.exception.CommunicationException;
import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatus;
import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatusStatus;
import com.github.libgraviton.workerbase.exception.GravitonCommunicationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * @author List of contributors
 *         <https://github.com/libgraviton/graviton/graphs/contributors>
 * @link http://swisscom.ch
 */
public class EventStatusHandlerTest {

  private final String filterTemplate = "?elemMatch(information,and(eq(content,{requestId}),eq(workerId,{workerId})))&elemMatch(status,and(ne(status,done),eq(workerId,{workerId})))";

  private GravitonApi gravitonApi;

  @BeforeEach
  public void setup() throws Exception {
    gravitonApi = mock(GravitonApi.class, RETURNS_DEEP_STUBS);
  }

  @Test
  public void testGetEventStatusFromUrl() {
    Assertions.assertThrows(GravitonCommunicationException.class, () -> {
      EventStatusHandler statusHandler = spy(new EventStatusHandler(gravitonApi, "workerId",20));
      String url = "testUrl";
      when(gravitonApi.get(url).execute()).thenThrow(new CommunicationException("Ooops!"));
      statusHandler.getEventStatusFromUrl(url);
    });
  }

  @Test
  public void testGetEventStatusByFilterWithNoMatchingResponse() {
    Assertions.assertThrows(GravitonCommunicationException.class, () -> {
      EventStatusHandler statusHandler = spy(new EventStatusHandler(gravitonApi, "workerId", 20));

      List<EventStatus> statusDocuments = new ArrayList<>();
      doReturn(statusDocuments).when(statusHandler).findEventStatus(filterTemplate);
      statusHandler.getEventStatusByFilter(filterTemplate);
    });
  }

  @Test
  public void testGetEventStatusByFilterWithMultipleMatchingResponse() {
    Assertions.assertThrows(GravitonCommunicationException.class, () -> {
      EventStatusHandler statusHandler = spy(new EventStatusHandler(gravitonApi, "workerId",20));

      List<EventStatus> statusDocuments = new ArrayList<>();
      statusDocuments.add(new EventStatus());
      statusDocuments.add(new EventStatus());

      doReturn(statusDocuments).when(statusHandler).findEventStatus(filterTemplate);
      statusHandler.getEventStatusByFilter(filterTemplate);
    });
  }

  @Test
  public void testGetEventStatusByFilterWithOneMatchingResponse() throws GravitonCommunicationException {
    EventStatusHandler statusHandler = spy(new EventStatusHandler(gravitonApi, "workerId", 20));

    List<EventStatus> statusDocuments = new ArrayList<>();
    statusDocuments.add(new EventStatus());

    doReturn(statusDocuments).when(statusHandler).findEventStatus(filterTemplate);
    statusHandler.getEventStatusByFilter(filterTemplate);
  }

  @Test
  public void testGetRqlFilter()  {
    EventStatusHandler statusHandler = spy(new EventStatusHandler(gravitonApi, "workerId", 20));

    String expectedFilter = "?elemMatch(information,and(eq(content,myInput),eq(workerId,anotherInput)))&elemMatch(status,and(ne(status,done),eq(workerId,last%2Dinput)))";
    String firstParam = "myInput";
    String secondParam = "anotherInput";
    String thirdParam = "last-input";

    String rqlFilter = statusHandler.getRqlFilter(filterTemplate, firstParam, secondParam, thirdParam);
    Assertions.assertEquals(expectedFilter, rqlFilter);
  }

  @Test
  public void testGetRqlFilterWithTooManyParams()  {
    EventStatusHandler statusHandler = spy(new EventStatusHandler(gravitonApi, "workerId", 20));

    String expectedFilter = "?elemMatch(information,and(eq(content,myInput),eq(workerId,anotherInput)))&elemMatch(status,and(ne(status,done),eq(workerId,last%2Dinput)))";
    String firstParam = "myInput";
    String secondParam = "anotherInput";
    String thirdParam = "last-input";
    String forthParam = "this-will-never-be-used";

    String rqlFilter = statusHandler.getRqlFilter(filterTemplate, firstParam, secondParam, thirdParam, forthParam);
    Assertions.assertEquals(expectedFilter, rqlFilter);
  }

  @Test
  public void testGetRqlFilterWithInsufficientParams()  {
    EventStatusHandler statusHandler = spy(new EventStatusHandler(gravitonApi, "workerId", 20));

    String expectedFilter = "?elemMatch(information,and(eq(content,myInput),eq(workerId,{workerId})))&elemMatch(status,and(ne(status,done),eq(workerId,{workerId})))";
    String firstParam = "myInput";

    String rqlFilter = statusHandler.getRqlFilter(filterTemplate, firstParam);
    Assertions.assertEquals(expectedFilter, rqlFilter);
  }
}
