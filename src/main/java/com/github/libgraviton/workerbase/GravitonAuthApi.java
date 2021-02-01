package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.gdk.GravitonApi;
import com.github.libgraviton.workerbase.gdk.api.header.HeaderBag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Public base class for workers api calls with auth headers
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/graviton-worker-base-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
public class GravitonAuthApi extends GravitonApi {

    private static final Logger LOG = LoggerFactory.getLogger(GravitonAuthApi.class);

    protected String authHeaderName;
    protected String authHeaderValue;
    private Map<String, String> transientHeaders = new HashMap<>();

    public GravitonAuthApi(Properties properties) {
        super();
        this.authHeaderValue = properties.getProperty("graviton.authentication.prefix.username")
                .concat(properties.getProperty("graviton.workerId"));
        this.authHeaderName = properties.getProperty("graviton.authentication.header.name");
    }

    @Override
    protected HeaderBag.Builder getDefaultHeaders() {
        HeaderBag.Builder builder = new HeaderBag.Builder()
                .set("Content-Type", "application/json")
                .set("Accept", "application/json")
                .set(authHeaderName, authHeaderValue);

        // transient headers?
        if (!transientHeaders.isEmpty()) {
            for (Map.Entry<String, String> entry : transientHeaders.entrySet()) {
                builder.set(entry.getKey(), entry.getValue());
            }

            LOG.info("Including transient headers from QueueEvent in request: {}", transientHeaders);
        }

        return builder;
    }

    /**
     * transient headers as those that we receive in {@link com.github.libgraviton.workerbase.model.QueueEvent}
     * and forward subsequently again on following requests during the request handling in the worker..
     *
     * @param transientHeaders
     */
    public void setTransientHeaders(Map<String, String> transientHeaders) {
        this.transientHeaders = transientHeaders;
    }

    public void clearTransientHeaders() {
        transientHeaders.clear();
    }
}
