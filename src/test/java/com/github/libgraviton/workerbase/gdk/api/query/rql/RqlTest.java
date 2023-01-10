package com.github.libgraviton.workerbase.gdk.api.query.rql;

import com.github.libgraviton.workerbase.gdk.data.ComplexClass;
import com.github.libgraviton.workerbase.gdk.serialization.mapper.RqlObjectMapper;
import com.github.libgraviton.workerbase.helper.DependencyInjection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

public class RqlTest {

    @Test
    public void testGenerateWithAllStatements() {
        ComplexClass aClass1 = new ComplexClass();
        aClass1.setName("name1");
        ComplexClass aClass2 = new ComplexClass();
        aClass2.setName("name2");
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(2001, 10, 20, 9, 8, 7);
        aClass2.setDate(calendar.getTime());

        ComplexClass complexClass = new ComplexClass();
        complexClass.setName("aName");
        complexClass.setaClass(aClass1);
        complexClass.setClasses(Arrays.asList(aClass1, aClass2));

        RqlObjectMapper rqlObjectMapper = DependencyInjection.getInstance(RqlObjectMapper.class);

        Rql rql = new Rql.Builder()
                .setLimit(1)
                .addSelect("zip")
                .addSelect("city")
                .setResource(complexClass, rqlObjectMapper)
                .build();

        String expectedRql = "?and(eq(name,string:aName),eq(aClass.name,string:name1),eq(classes..name,string:name1),eq(classes..name,string:name2),eq(classes..date,2001-11-20T09:08:07Z))&limit(1)&select(zip,city)";

        Assertions.assertEquals(expectedRql, rql.generate());
    }

    @Test
    public void testGenerateWithoutStatements() {
        Rql rql = new Rql.Builder().build();
        String expectedRql = "";
        Assertions.assertEquals(expectedRql, rql.generate());
    }

    @Test
    public void testAddRqlStatements() {
        Rql rql1 = new Rql.Builder()
                .setLimit(2,3)
                .build();

        Rql rql2 = new Rql.Builder()
                .addSelect("attribute1")
                .build();

        rql1.addStatements(rql2.getStatements());

        String expectedRql = "?limit(2,3)&select(attribute1)";
        Assertions.assertEquals(expectedRql, rql1.generate());
    }

    @Test
    public void testEncodeSuccessfully() throws UnsupportedEncodingException {
        String unencoded = "http://a-host/endpoint?query=123_5.6~7";
        String encoded = "http%3A%2F%2Fa%2Dhost%2Fendpoint%3Fquery%3D123%5F5%2E6%7E7";
        Assertions.assertEquals(encoded, Rql.encode(unencoded, Rql.DEFAULT_ENCODING));
    }

    @Test
    public void testEncodeUnchanged() throws UnsupportedEncodingException {
        String unencoded = "asdf123";
        Assertions.assertEquals(unencoded, Rql.encode(unencoded, Rql.DEFAULT_ENCODING));
    }

    @Test
    public void testEncodeUnsupportedEncoding() throws UnsupportedEncodingException {
        Assertions.assertThrows(UnsupportedEncodingException.class, () -> {
            Rql.encode("asdf123", "non-existing-encoding");
        });
    }
}
