package com.github.libgraviton.workerbase.gdk.serialization.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.libgraviton.gdk.util.PropertiesLoader;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

public class GravitonObjectMapperTest {

    @Test
    public void testDateFormat() throws Exception {
        Properties properties = PropertiesLoader.load();
        GravitonObjectMapper mapper = new GravitonObjectMapper(properties);

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(2001, 10, 20, 9, 8, 7);
        Date date = calendar.getTime();
        JsonNode jsonNode = mapper.valueToTree(date);
        assertEquals("2001-11-20T09:08:07+0000", jsonNode.textValue());
    }
}
