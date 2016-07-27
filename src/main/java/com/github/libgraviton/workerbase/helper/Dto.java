package com.github.libgraviton.workerbase.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.github.libgraviton.workerbase.exception.UnsuccessfulHttpResponseException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;


/**
 * TODO replace all Unirest calls with Dto calls
 * Dto Communication to fetch and send Dto objects to their endpoints.
 *
 * @author List of contributors
 *         https://github.com/libgraviton/graviton/graphs/contributors
 * @see <a href="http://swisscom.chm">http://swisscom.ch</a>
 */
public class Dto {
    private static final Logger LOG = LoggerFactory.getLogger(Dto.class);

    public static <T> T fetchFrom(String url, Class<T> clazz, String... params) throws UnsuccessfulHttpResponseException {
        String responseBody = fetch(url, params);
        try {
            return getMapper().readValue(responseBody, clazz);
        } catch (IOException e) {
            throw new UnsuccessfulHttpResponseException("Unable to deserialize Response from '" + url + "'.");
        }
    }

    public static JsonNode fetchFrom(String url, String... params) throws UnsuccessfulHttpResponseException {
        String responseBody = fetch(url, params);
        try {
            return getMapper().readTree(responseBody);
        } catch (IOException e) {
            throw new UnsuccessfulHttpResponseException("Unable to deserialize Response from '" + url + "'.");
        }
    }

    public static <T> List<T> fetchListFrom(String url, Class<T> clazz, String... params) throws UnsuccessfulHttpResponseException {
        String responseBody = fetch(url, params);
        ObjectMapper mapper = getMapper();
        try {
            return mapper.readValue(responseBody, mapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (IOException e) {
            throw new UnsuccessfulHttpResponseException("Unable to deserialize Response from '" + url + "'.");
        }
    }

    protected static String fetch(String url, String... params) throws UnsuccessfulHttpResponseException {
        url = generateUrl(url, params);
        LOG.debug("Starting GET from '" + url + "'...");

        HttpResponse<String> response;
        try {
            response = Unirest.get(url).header("Accept", "application/json").asString();
            if (response.getStatus() >= 300) {
                throw new UnsuccessfulHttpResponseException("GET Response for '" + url + "' had response '" + response.getStatus() + " - " + response.getStatusText() + "' with body '" + response.getBody() + "'.");
            }
            String responseBody = response.getBody();
            LOG.info("Successful GET from '" + url + "'.");
            return responseBody;
        } catch (UnirestException e) {
            throw new UnsuccessfulHttpResponseException("Unable to GET '" + url + "'.", e);
        }
    }

    public static HttpResponse<String> sendTo(String url, Object payload, String... params) throws UnsuccessfulHttpResponseException {
        url = generateUrl(url, params);
        LOG.debug("Starting POST to '" + url + "'...");
        
        String serializedPayload;
        try {
            serializedPayload = getMapper().writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new UnsuccessfulHttpResponseException("Unable to serialize class '" + payload.getClass() + "'.", e);
        }

        HttpResponse<String> response;
        try {
            response = Unirest.post(url)
                    .header("Content-Type", "application/json")
                    .body(serializedPayload)
                    .asString();
        } catch (UnirestException e) {
            throw new UnsuccessfulHttpResponseException("Could not POST to '" + url +"'.");
        }
        if (response.getStatus() >= 300) {
            throw new UnsuccessfulHttpResponseException("POST to '" + url + "' failed with HTTP status " + response.getStatus() +
                    ", Message: " + response.getStatusText() );
        }
        LOG.info("Successful POST to '" + url + "'.");
        return response;
    }

    private static ObjectMapper getMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector());
        return objectMapper;
    }

    private static String generateUrl(String url, String... params) {
        // placeholder -> {somePlaceholder}
        String placeholderRegex = "\\{[^\\{]*\\}";

        for (String param : params) {
            url = url.replaceFirst(placeholderRegex, param);
        }
        return url;
    }

}
