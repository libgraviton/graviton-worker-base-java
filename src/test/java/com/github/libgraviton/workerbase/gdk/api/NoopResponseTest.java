package com.github.libgraviton.workerbase.gdk.api;

import com.github.libgraviton.gdk.exception.CommunicationException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class NoopResponseTest {

    @Test
    public void testResponse() throws CommunicationException {
        NoopRequest request = mock(NoopRequest.class);
        NoopResponse response = new NoopResponse(request);
        assertEquals(request, response.getRequest());
        assertEquals(-1, response.getCode());
        assertEquals(true, response.isSuccessful());
    }
}
