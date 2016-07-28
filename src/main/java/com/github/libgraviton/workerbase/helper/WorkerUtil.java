/**
 * 
 */
package com.github.libgraviton.workerbase.helper;

import com.fasterxml.jackson.jr.ob.JSON;
import com.github.libgraviton.workerbase.exception.GravitonCommunicationException;
import com.github.libgraviton.workerbase.model.file.GravitonFile;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * <p>WorkerUtil class.</p>
 *
 * @author dn
 * @version $Id: $Id
 * @since 0.7.0
 */
public class WorkerUtil {
   
    /**
     * <p>encodeRql.</p>
     * Encode as described in https://github.com/xiag-ag/rql-parser
     *
     * @param expr a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     * @throws java.io.UnsupportedEncodingException if any.
     */
    public static String encodeRql(String expr) throws UnsupportedEncodingException {
        String encoded = URLEncoder.encode(expr, "UTF-8");
        encoded = encoded
                .replace("-", "%2D")
                .replace("_", "%5F")
                .replace(".", "%2E")
                .replace("~", "%7E")
                .replace(",", "%2C");
        return encoded;
    }

    /**
     * gets file metadata from backend as a GravitonFile object
     *
     * @param fileUrl the url of the object
     * @throws GravitonCommunicationException if file could not be fetched.
     * @return file instance
     */
    public static GravitonFile getGravitonFile(String fileUrl) throws GravitonCommunicationException {
        try {
            HttpResponse<String> response = Unirest.get(fileUrl).header("Accept", "application/json").asString();
            GravitonFile file =  JSON.std.beanFrom(GravitonFile.class, response.getBody());
            if (file == null || file.getId() == null) {
                throw new GravitonCommunicationException("Unable to GET graviton file from '" + fileUrl + "'.");
            }

            return file;
        } catch (UnirestException | IOException e) {
            throw new GravitonCommunicationException("Unable to GET graviton file from '" + fileUrl + "'.", e);
        }
    }
}
