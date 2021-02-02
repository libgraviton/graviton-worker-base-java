package com.github.libgraviton.workerbase.gdk.serialization.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.TimeZone;

public class GravitonObjectMapper {
    public static ObjectMapper getInstance(Properties properties) {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleDateFormat dateFormat = new SimpleDateFormat(properties.getProperty("graviton.date.format"));
        dateFormat.setTimeZone(TimeZone.getTimeZone(properties.getProperty("graviton.timezone")));
        objectMapper.setDateFormat(dateFormat);
        return objectMapper;
    }
}
