/**
 * 
 */
package com.github.libgraviton.workerbase.helper;

import com.github.libgraviton.gdk.gravitondyn.file.document.File;
import com.github.libgraviton.workerbase.gdk.GravitonFileEndpoint;
import com.github.libgraviton.workerbase.gdk.api.Response;
import com.github.libgraviton.workerbase.gdk.api.gateway.OkHttpGateway;
import com.github.libgraviton.workerbase.gdk.api.gateway.okhttp.OkHttpGatewayFactory;
import com.github.libgraviton.workerbase.exception.GravitonCommunicationException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.OkHttpClient;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * <p>WorkerUtil class.</p>
 *
 * @author dn
 * @version $Id: $Id
 * @since 0.7.0
 */
public class WorkerUtil {

    // create a trust manager that does not validate certificate chains
    final private static TrustManager[] trustAllCerts = new TrustManager[]{
        new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                String i;
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                String i;
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }
    };

    /**
     * <p>encodeRql.</p>
     * Encode as described in https://github.com/xiag-ag/rql-parser
     *
     * @param expr a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     * @throws java.io.UnsupportedEncodingException if any.
     */
    public static String encodeRql(String expr) {
        String encoded = URLEncoder.encode(expr, StandardCharsets.UTF_8);
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

        try {
            Response response = fileEndpoint.getMetadata(fileUrl).execute();
            return response.getBodyItem(File.class);
        } catch (Throwable t) {
            throw new GravitonCommunicationException("Error fetching file " + fileUrl, t);
        }
    }

    @Deprecated
    public static boolean isJarContext() {
        return isJarContext(WorkerUtil.class);
    }

    @Deprecated
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

    public static SSLSocketFactory getAllTrustingSocketFactory(String algo)
        throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance(algo);
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

        return sslContext.getSocketFactory();
    }

    public static SSLSocketFactory getAllTrustingSocketFactory()
        throws NoSuchAlgorithmException, KeyManagementException {
        return getAllTrustingSocketFactory("SSL");
    }

    public static TrustManager[] getAllTrustingTrustManagers() {
        return trustAllCerts;
    }
}
