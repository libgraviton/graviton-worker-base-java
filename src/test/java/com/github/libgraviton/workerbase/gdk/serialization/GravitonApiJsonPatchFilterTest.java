package com.github.libgraviton.workerbase.gdk.serialization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.Charset;

public class GravitonApiJsonPatchFilterTest {

    private GravitonJsonPatchFilter filter;

    private ObjectMapper mapper;

    @BeforeEach
    public void setup() {
        filter = new GravitonJsonPatchFilter();
        mapper = new ObjectMapper();
    }

    @Test
    public void testFilter() throws Exception {
        String patchJson = FileUtils.readFileToString(new File("src/test/resources/serialization/patch1.json"), Charset.defaultCharset());
        String expectedPatchJson = FileUtils.readFileToString(new File("src/test/resources/serialization//filteredPatch1.json"), Charset.defaultCharset());

        JsonNode patchNode = mapper.readTree(patchJson);
        JsonNode filteredPatchNode = this.filter.filter(patchNode);
        Assertions.assertEquals(expectedPatchJson, mapper.writeValueAsString(filteredPatchNode));
    }
}
