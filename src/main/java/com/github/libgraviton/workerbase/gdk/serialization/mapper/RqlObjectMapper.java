package com.github.libgraviton.workerbase.gdk.serialization.mapper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.TimeZone;

/**
 * Created by taawemi6 on 14.03.17.
 */
public class RqlObjectMapper extends ObjectMapper {

    public RqlObjectMapper(Properties properties) {
        super();

        SimpleDateFormat dateFormat = new SimpleDateFormat(properties.getProperty("graviton.rql.date.format"));
        dateFormat.setTimeZone(TimeZone.getTimeZone(properties.getProperty("graviton.timezone")));
        setDateFormat(dateFormat);
        setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }
}
