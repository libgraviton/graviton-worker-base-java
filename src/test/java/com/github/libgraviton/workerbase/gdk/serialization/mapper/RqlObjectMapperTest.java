package com.github.libgraviton.workerbase.gdk.serialization.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.libgraviton.workerbase.gdk.data.SimpleClass;
import com.github.libgraviton.workerbase.helper.DependencyInjection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class RqlObjectMapperTest {

    @Test
    public void testDateFormat() {
        RqlObjectMapper mapper = DependencyInjection.getInstance(RqlObjectMapper.class);

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(2001, 10, 20, 9, 8, 7);
        Date date = calendar.getTime();
        JsonNode jsonNode = mapper.valueToTree(date);
        Assertions.assertEquals("2001-11-20T09:08:07Z", jsonNode.textValue());
    }

    @Test
    public void testIgnoreNullValues() {
        RqlObjectMapper mapper = DependencyInjection.getInstance(RqlObjectMapper.class);

        SimpleClass simpleClass = new SimpleClass();
        simpleClass.setId("123");

        JsonNode node = mapper.valueToTree(simpleClass);
        Assertions.assertEquals("123", node.get("id").textValue());
        Assertions.assertFalse(node.has("name"));

        simpleClass.setName("aName");

        node = mapper.valueToTree(simpleClass);
        Assertions.assertEquals("123", node.get("id").textValue());
        Assertions.assertEquals("aName", node.get("name").textValue());
    }
}
