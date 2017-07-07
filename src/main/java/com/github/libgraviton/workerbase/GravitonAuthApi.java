package com.github.libgraviton.workerbase;

import com.github.libgraviton.gdk.GravitonApi;
import com.github.libgraviton.gdk.api.header.HeaderBag;

import java.util.Properties;

/**
 * Public base class for workers api calls with auth headers
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/graviton-worker-base-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
public class GravitonAuthApi extends GravitonApi {

    protected String authHeaderName;
    protected String authHeaderValue;


    public GravitonAuthApi(Properties properties) {
        super();
        this.authHeaderValue = properties.getProperty("graviton.authentication.prefix.username")
                .concat(properties.getProperty("graviton.workerId"));
        this.authHeaderName = properties.getProperty("graviton.authentication.header.name");
    }

    @Override
    protected HeaderBag.Builder getDefaultHeaders() {
        return new HeaderBag.Builder()
                .set("Content-Type", "application/json")
                .set("Accept", "application/json")
                .set(authHeaderName, authHeaderValue);
    }
}
