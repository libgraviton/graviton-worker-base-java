package com.github.libgraviton.workerbase.gdk.api;

import com.github.libgraviton.workerbase.gdk.exception.CommunicationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NoopRequestTest {

    @Test
    public void testExecute() throws CommunicationException {
        Response response = new NoopRequest.Builder("no reason")
                .setUrl("http://url-to-a-beautiful-place")
                .execute();
        Assertions.assertNotNull(response);
        Assertions.assertTrue(response instanceof NoopResponse);
    }
}
