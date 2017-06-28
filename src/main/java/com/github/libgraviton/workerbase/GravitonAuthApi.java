/**
 * Public base class for workers api calls with auth headers
 */

package com.github.libgraviton.workerbase;

import com.github.libgraviton.gdk.GravitonApi;
import com.github.libgraviton.gdk.api.header.HeaderBag;
import com.github.libgraviton.gdk.util.PropertiesLoader;

import java.io.IOException;
import java.util.Properties;

/**
 * <p>Public GravitonAuthApi class.</p>
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/graviton-worker-base-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
public class GravitonAuthApi extends GravitonApi {

    private Properties properties;

    protected HeaderBag getDefaultHeaders() {
        String subnetName = "subnet-" + properties.getProperty("graviton.workerId");
        String authHeaderName = properties.getProperty("graviton.authentication.header.name");

        return new HeaderBag.Builder()
                .set("Content-Type", "application/json")
                .set("Accept", "application/json")
                .set(authHeaderName, subnetName)
                .build();
    }

    protected void setup() {
        try {
            this.properties = PropertiesLoader.load();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load properties files.", e);
        }
        super.setup();
    }
}
