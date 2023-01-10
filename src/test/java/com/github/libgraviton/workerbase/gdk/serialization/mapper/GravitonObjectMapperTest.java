package com.github.libgraviton.workerbase.gdk.serialization.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.libgraviton.workerbase.helper.DependencyInjection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class GravitonObjectMapperTest {

    @Test
    public void testDateFormat() {
        ObjectMapper mapper = DependencyInjection.getInstance(ObjectMapper.class);

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(2001, 10, 20, 9, 8, 7);
        Date date = calendar.getTime();
        JsonNode jsonNode = mapper.valueToTree(date);
        Assertions.assertEquals("2001-11-20T09:08:07+0000", jsonNode.textValue());
    }
}
