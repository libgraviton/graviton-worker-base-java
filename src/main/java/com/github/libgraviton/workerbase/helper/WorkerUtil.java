/**
 * 
 */
package com.github.libgraviton.workerbase.helper;

import com.github.libgraviton.gdk.gravitondyn.file.document.File;
import com.github.libgraviton.workerbase.gdk.GravitonFileEndpoint;
import com.github.libgraviton.workerbase.gdk.api.Response;
import com.github.libgraviton.workerbase.gdk.api.gateway.OkHttpGateway;
import com.github.libgraviton.workerbase.gdk.api.gateway.okhttp.OkHttpGatewayFactory;
import com.github.libgraviton.workerbase.gdk.exception.CommunicationException;
import com.github.libgraviton.workerbase.exception.GravitonCommunicationException;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOG = LoggerFactory.getLogger(WorkerUtil.class);

    public static final int RETRY_COUNT = 5;

    public static final int SEC_WAIT_BEFORE_RETRY = 3;

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
     * @param fileEndpoint GravitonFileEndpoint instance
     * @param fileUrl the url of the object
     * @throws GravitonCommunicationException if file could not be fetched.
     * @return file instance
     */
    public static File getGravitonFile(
            GravitonFileEndpoint fileEndpoint,
            String fileUrl
    ) throws GravitonCommunicationException {
        return getGravitonFile(fileEndpoint, fileUrl, RETRY_COUNT, SEC_WAIT_BEFORE_RETRY);
    }

    /**
     * gets file metadata from backend as a GravitonFile object
     *
     * @param fileEndpoint GravitonFileEndpoint instance
     * @param fileUrl the url of the object
     * @param retryCount number of retries if the file meta could not be fetched
     * @param secWaitBeforeRetry amount of seconds to wait before retrying
     * @throws GravitonCommunicationException GravitonCommunicationException if file could not be fetched.
     * @return file instance
     */
    public static File getGravitonFile(
            GravitonFileEndpoint fileEndpoint, String fileUrl, int retryCount, int secWaitBeforeRetry
    ) throws GravitonCommunicationException {
        int triesCount = 0;
        File file = null;
        Response response = null;
        do {
            if (triesCount > 0) {
                LOG.warn(
                        "Unable to fetch {}. Trying again in {}s ({}/{})",
                        fileUrl,
                        secWaitBeforeRetry,
                        triesCount,
                        retryCount
                );
                try {
                    Thread.sleep(secWaitBeforeRetry * 1000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
            try {
                response = fileEndpoint.getMetadata(fileUrl).execute();
                file = response.getBodyItem(File.class);
            } catch (CommunicationException e) {
                throw new GravitonCommunicationException("Unable to GET graviton file from '" + fileUrl + "'.", e);
            }

            triesCount++;

        } while (triesCount <= retryCount && (file == null || file.getId() == null));

        if (file == null || file.getId() == null) {
            throw new GravitonCommunicationException("Unable to GET graviton file from '" + fileUrl + "'.");
        }

        return file;
    }

    public static boolean isJarContext() {
        return isJarContext(WorkerUtil.class);
    }

    public static boolean isJarContext(Object obj) {
        return obj.getClass().getResource("").getProtocol().equals("jar");
    }

    public static OkHttpGateway getGatewayInstance() {
        return new OkHttpGateway();
    }

    public static OkHttpGateway getGatewayInstance(OkHttpClient okHttpClient) {
        return new OkHttpGateway(okHttpClient);
    }

    public static OkHttpGateway getAllTrustingGatewayInstance() throws Exception {
        return new OkHttpGateway(OkHttpGatewayFactory.getAllTrustingInstance(false, null));
    }

    public static OkHttpGateway getAllTrustingGatewayInstance(OkHttpClient okHttpClient) throws Exception {
        return new OkHttpGateway(OkHttpGatewayFactory.getAllTrustingInstance(false, okHttpClient));
    }
}
