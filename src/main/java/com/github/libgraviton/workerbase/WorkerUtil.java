/**
 * 
 */
package com.github.libgraviton.workerbase;

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
     *
     * @param expr a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     * @throws java.io.UnsupportedEncodingException if any.
     */
    public static String encodeRql(String expr) throws UnsupportedEncodingException {
        String encoded = URLEncoder.encode(expr, "UTF-8");
        encoded = encoded.replace(",", "%2C");
        return encoded;
    }
    
}
