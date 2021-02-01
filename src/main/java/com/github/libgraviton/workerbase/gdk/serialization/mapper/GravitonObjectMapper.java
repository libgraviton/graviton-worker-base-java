package com.github.libgraviton.workerbase.gdk.serialization.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.TimeZone;

/**
 * Created by taawemi6 on 14.03.17.
 */
public class GravitonObjectMapper extends ObjectMapper {

    public GravitonObjectMapper(Properties properties) {
        super();

        SimpleDateFormat dateFormat = new SimpleDateFormat(properties.getProperty("graviton.date.format"));
        dateFormat.setTimeZone(TimeZone.getTimeZone(properties.getProperty("graviton.timezone")));
        setDateFormat(dateFormat);
    }
}
