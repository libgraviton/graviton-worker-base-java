/**
 * 
 */
package com.github.libgraviton.workerbase;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @author dn
 *
 */
public class WorkerUtil {

    public static String encodeRql(String expr) throws UnsupportedEncodingException {
        String encoded = URLEncoder.encode(expr, "UTF-8");
        encoded = encoded.replace(",", "%2C");
        return encoded;
    }
    
}
