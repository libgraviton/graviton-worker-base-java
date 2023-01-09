/**
 * 
 */
package com.github.libgraviton.workerbase.helper;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.io.IOUtils;

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

    public static String getWorkerBaseVersion() {
        try {
            return IOUtils.resourceToString("worker-base-version", StandardCharsets.UTF_8, WorkerUtil.class.getClassLoader());
        } catch (Throwable t) {
            return "(UNKNOWN)";
        }
    }

    // create a trust manager that does not validate certificate chains
    final private static TrustManager[] trustAllCerts = new TrustManager[]{
        new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
                String i;
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
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
