package com.github.libgraviton.workerbase.gdk.serialization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.libgraviton.workerbase.gdk.data.NoopClass;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.Charset;

public class JsonPatcherTest {

    private ObjectMapper mapper;

    @BeforeEach
    public void setup() {
        mapper = new ObjectMapper();
        JsonPatcher.clearMemory();
    }

    @Test
    public void testAdd() throws Exception {
        NoopClass object = new NoopClass();
        Assertions.assertEquals(0,JsonPatcher.getMemory().size());
        JsonPatcher.add(object, mapper.readTree("{\"a\":\"b\"}"));
        Assertions.assertEquals(1,JsonPatcher.getMemory().size());
    }

    @Test
    public void testGetPatch() throws Exception {
        NoopClass object = new NoopClass();
        String jsonBefore = FileUtils.readFileToString(new File("src/test/resources/serialization/patch2.json"), Charset.defaultCharset());
        JsonPatcher.add(object, mapper.readTree(jsonBefore));
        Assertions.assertEquals(1,JsonPatcher.getMemory().size());
        String jsonAfter = FileUtils.readFileToString(new File("src/test/resources/serialization/patch3.json"), Charset.defaultCharset());
        JsonNode patch = JsonPatcher.getPatch(object, mapper.readTree(jsonAfter));
        String jsonPatch = FileUtils.readFileToString(new File("src/test/resources/serialization/patch4.json"), Charset.defaultCharset());
        Assertions.assertEquals(jsonPatch, mapper.writeValueAsString(patch));
    }

    @Test
    public void testGetPatchNullValue() throws Exception {
        NoopClass object = new NoopClass();
        Assertions.assertEquals(0,JsonPatcher.getMemory().size());
        String json = FileUtils.readFileToString(new File("src/test/resources/serialization/patch3.json"), Charset.defaultCharset());
        JsonNode patch = JsonPatcher.getPatch(object, mapper.readTree(json));
        Assertions.assertNull(patch);
    }
}
