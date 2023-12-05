package com.github.libgraviton.workerbase.gdk.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.github.libgraviton.workerbase.gdk.api.header.HeaderBag;
import com.github.libgraviton.workerbase.gdk.exception.DeserializationException;
import com.github.libgraviton.workerbase.gdk.serialization.JsonPatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Graviton response wrapper with additional functionality and simplified interface.
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/gdk-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
public class Response {

    protected static final Logger LOG = LoggerFactory.getLogger(Response.class);

    protected Request request;

    protected int code;

    protected String message;

    protected HeaderBag headers;

    protected byte[] body;

    protected boolean isSuccessful;

    protected ObjectMapper objectMapper;

    protected Response() {
    }

    protected Response(com.github.libgraviton.workerbase.gdk.api.Response.Builder builder) {
        request = builder.request;
        code = builder.code;
        isSuccessful = builder.isSuccessful;
        body = builder.body;
        headers = builder.headerBuilder.build();
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    /**
     * Deserialize the response body into the requested POJO class.
     *
     * @param beanClass the requested POJO class
     * @param <BeanClass> requested POJO must extend from this class
     * @return serialized POJO
     * @throws DeserializationException will be thrown on a failed deserialization / POJO mapping
     */
    public <BeanClass> BeanClass getBodyItem(final Class<? extends BeanClass> beanClass) throws DeserializationException {
        if(getObjectMapper() == null) {
            throw new IllegalStateException("'objectMapper' is not allowed to be null.");
        }

        try {
            BeanClass pojoValue = getObjectMapper().readValue(getBody(), beanClass);
            JsonPatcher.add(pojoValue, getObjectMapper().valueToTree(pojoValue));
            return pojoValue;
        } catch (IOException e) {
            LOG.error(
                    "Unable to deserialize response body from '{}' to class '{}'; received body '{}'.",
                    getRequest() == null ? '?' : getRequest().getUrl(),
                    beanClass.getName(),
                    getBody(),
                    e
            );
            throw new DeserializationException("Error in getBodyItem()", e);
        }
    }

    /**
     * Deserialize the response body into a list with elements of the requested POJO class.
     *
     * @param beanClass the requested POJO class
     * @param <BeanClass> requested POJO must extend from this class
     * @return serialized list with POJO elements
     * @throws DeserializationException will be thrown on a failed deserialization / POJO mapping
     */
    public <BeanClass> List<BeanClass> getBodyItems(final Class<? extends BeanClass> beanClass) throws DeserializationException {
        if(getObjectMapper() == null) {
            throw new IllegalStateException("'objectMapper' is not allowed to be null.");
        }

        try {
            final CollectionType javaType =
                    getObjectMapper().getTypeFactory().constructCollectionType(List.class, beanClass);
            List<BeanClass> pojoValues = getObjectMapper().readValue(getBody(), javaType);
            for (BeanClass pojoValue : pojoValues) {
                JsonPatcher.add(pojoValue, getObjectMapper().valueToTree(pojoValue));
            }
            return pojoValues;
        } catch (IOException e) {
            LOG.error(
                    "Unable to deserialize response body from '{}' to class '{}'; received body '{}'.",
                    getRequest() == null ? '?' : getRequest().getUrl(),
                    beanClass.getName(),
                    getBody(),
                    e
            );
            throw new DeserializationException("Error in getBodyItems()", e);
        }
    }

    /**
     * Deserialize the response body. Should only be used if no binary file is expected as response.
     * @return response body as String
     */
    public String getBody() {
        return body != null ? new String(body) : null;
    }

    /**
     * Deserialize the response body. Can also be used if binary file is expected as response.
     *
     * @return response body
     */
    public byte[] getBodyBytes() {
        return body;
    }

    public HeaderBag getHeaders() {
        return headers;
    }

    public Request getRequest() {
        return request;
    }


    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static class Builder {

        protected int code;

        protected final Request request;

        protected byte[] body;

        protected boolean isSuccessful;

        protected HeaderBag.Builder headerBuilder;

        public Builder(Request request) {
            this.request = request;
            headerBuilder = new HeaderBag.Builder();
        }

        public Builder code(int code) {
            this.code = code;
            return this;
        }

        public Builder successful(boolean isSuccessful) {
            this.isSuccessful = isSuccessful;
            return this;
        }

        public Builder body(byte[] body) {
            this.body = body;
            return this;
        }

        public Builder headers(HeaderBag.Builder builder) {
            this.headerBuilder = builder;
            return this;
        }

        public com.github.libgraviton.workerbase.gdk.api.Response build() {
            return new com.github.libgraviton.workerbase.gdk.api.Response(this);
        }

    }

}
