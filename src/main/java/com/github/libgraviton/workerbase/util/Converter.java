package com.github.libgraviton.workerbase.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.libgraviton.workerbase.gdk.exception.SerializationException;
import org.jetbrains.annotations.NotNull;


public class Converter {
    private static final ObjectMapper mapper = new ObjectMapper();


    public static <T> T getInstance(@NotNull String json, @NotNull Class<T> type) throws Exception {
        return mapper.readValue(json, type);
    }

    public static <T> T getInstance(Object object, Class<T> type) {
        return mapper.convertValue(object, type);
    }

    /**
     * serializes an object.
     *
     * @param data object to serialize
     * @param objectMapper object mapper
     *
     * @return serialized object
     *
     * @throws SerializationException object could not be serialized
     */
    public static String objectToJson(@NotNull Object data, @NotNull ObjectMapper objectMapper) throws SerializationException {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new SerializationException(String.format("Cannot serialize '%s' to json.", data.getClass().getName()), e);
        }
    }

    /**
     * serializes an object.
     *
     * @param data object to serialize
     *
     * @return serialized object
     *
     * @throws SerializationException object could not be serialized
     */
    public static String objectToJson(@NotNull Object data) throws SerializationException {
        try {
            return new ObjectMapper().writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new SerializationException(String.format("Cannot serialize '%s' to json.", data.getClass().getName()), e);
        }
    }

    public static byte[] decodeBase64(byte[] data) {
        // is base64 encoded?
        if (org.apache.commons.codec.binary.Base64.isBase64(data)) {
            return java.util.Base64.getDecoder().decode(data);
        }

        return data;
    }

    public static String encodeBase64(byte[] data) {
        return java.util.Base64.getEncoder().encodeToString(data);
    }

}
