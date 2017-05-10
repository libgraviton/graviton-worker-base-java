package com.github.libgraviton.workerbase.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class TruncatedMessageConverterTest {

    private TruncatedMessageConverter converter = spy(new TruncatedMessageConverter());

    @Test
    public void testSmallEnoughMessage() {
        String message = "asdf";
        String option1 = "10";
        String option2 = "[... truncated]";

        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn(message);
        doReturn(Arrays.asList(option1, option2)).when(converter).getOptions();

        String convertedMessege = converter.convert(event);
        assertEquals(message, convertedMessege);
    }

    @Test
    public void testTooBigMessage() {
        String message = "asdf";
        String option1 = "2";
        String option2 = "[... truncated]";

        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn(message);
        doReturn(Arrays.asList(option1, option2)).when(converter).getOptions();

        String convertedMessege = converter.convert(event);
        assertEquals("as " + option2, convertedMessege);
    }
}
