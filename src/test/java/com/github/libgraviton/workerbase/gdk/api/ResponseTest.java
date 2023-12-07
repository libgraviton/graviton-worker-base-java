package com.github.libgraviton.workerbase.gdk.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.libgraviton.workerbase.gdk.data.NoopClass;
import com.github.libgraviton.workerbase.gdk.data.SerializationTestClass;
import com.github.libgraviton.workerbase.gdk.exception.DeserializationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;

public class ResponseTest {

    private Response response;

    private Request request;

    @BeforeEach
    public void setup() throws Exception {
        request = mock(Request.class);
        response = new Response.Builder(request)
                .body("{\"code\":0}".getBytes())
                .successful(true)
                .code(200)
                .build();
        response.setObjectMapper(new ObjectMapper());
    }

    @Test
    public void testDeserializeBodyItemWithDeserializationException() {
        Assertions.assertThrows(DeserializationException.class, () -> {
            response.getBodyItem(NoopClass.class);
        });
    }

    @Test
    public void testDeserializeBodyItemWithMissingObjectMapper() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            response.setObjectMapper(null);
            response.getBodyItem(NoopClass.class);
        });
    }

    @Test
    public void testDeserializeBodyItemsWithMissingObjectMapper() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            response.setObjectMapper(null);
            response.getBodyItems(NoopClass.class);
        });
    }

    @Test
    public void testSuccessfulDeserializeBody() throws DeserializationException {
        response.getBodyItem(SerializationTestClass.class);
        Assertions.assertTrue(response.isSuccessful());
        Assertions.assertEquals(200, response.getCode());
        Assertions.assertEquals("{\"code\":0}", response.getBody());
        Assertions.assertEquals(request, response.getRequest());
        Assertions.assertEquals(0, response.getHeaders().all().size());
    }

    @Test
    public void testDeserializeBodyAsList() throws Exception {
        SerializationTestClass testClass1 = new SerializationTestClass();
        testClass1.setCode(1);
        SerializationTestClass testClass2 = new SerializationTestClass();
        testClass2.setCode(2);
        List<SerializationTestClass> testClasses = Arrays.asList(testClass1, testClass2);

        response = new Response.Builder(request)
                .body(new ObjectMapper().writeValueAsString(testClasses).getBytes())
                .successful(true)
                .code(200)
                .build();
        response.setObjectMapper(new ObjectMapper());

        response.getBodyItems(SerializationTestClass.class);
        Assertions.assertTrue(response.isSuccessful());
        List<SerializationTestClass> items = response.getBodyItems(SerializationTestClass.class);
        Assertions.assertEquals(2, items.size());
        Assertions.assertEquals(testClass1.getCode(), items.get(0).getCode());
        Assertions.assertEquals(testClass2.getCode(), items.get(1).getCode());
    }
}
