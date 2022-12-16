package com.github.libgraviton.workerbase.gdk.api;

import com.github.libgraviton.workerbase.gdk.exception.CommunicationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

public class NoopResponseTest {

    @Test
    public void testResponse() throws CommunicationException {
        NoopRequest request = mock(NoopRequest.class);
        NoopResponse response = new NoopResponse(request);
        Assertions.assertEquals(request, response.getRequest());
        Assertions.assertEquals(-1, response.getCode());
        Assertions.assertTrue(response.isSuccessful());
    }
}
