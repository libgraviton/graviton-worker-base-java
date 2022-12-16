/**
 * 
 */
package com.github.libgraviton.workerbase.helper;

import com.github.libgraviton.workerbase.annotation.GravitonWorkerDiScan;
import com.github.libgraviton.workerbase.gdk.GravitonApi;
import com.github.libgraviton.workerbase.gdk.GravitonFileEndpoint;
import io.activej.inject.annotation.Inject;
import okhttp3.HttpUrl;
import org.apache.commons.collections4.map.HashedMap;

import java.util.Map;
import java.util.Properties;

/**
 * this is the "main" scope that holds all needed utilities..
 */
@GravitonWorkerDiScan
public class WorkerScope {

    private final GravitonApi gravitonApi;
    private final Properties properties;
    private final EventStatusHandler statusHandler;
    private final GravitonFileEndpoint fileEndpoint;
    private final Map<String, Object> scopeCacheMap;

    @Inject
    public WorkerScope(Properties properties, GravitonApi gravitonApi, EventStatusHandler statusHandler, GravitonFileEndpoint fileEndpoint) {
        this.properties = properties;
        this.gravitonApi = gravitonApi;
        this.statusHandler = statusHandler;
        this.fileEndpoint = fileEndpoint;
        scopeCacheMap = new HashedMap<>();
    }

    public GravitonApi getGravitonApi() {
        return gravitonApi;
    }

    public Properties getProperties() {
        return properties;
    }

    public EventStatusHandler getStatusHandler() {
        return statusHandler;
    }

    public GravitonFileEndpoint getFileEndpoint() {
        return fileEndpoint;
    }

    public String getWorkerId() {
        return properties.getProperty(WorkerProperties.WORKER_ID);
    }

    public Map<String, Object> getScopeCacheMap() {
        return scopeCacheMap;
    }

    /**
     * Rewrite the status URL in relation to the configured GravitonApi Base url.
     *
     * @param url url
     * @return corrected url
     */
    public String convertToGravitonUrl(String url) {
        HttpUrl baseUrl = HttpUrl.parse(properties.getProperty(WorkerProperties.GRAVITON_BASE_URL));

        // convert
        HttpUrl targetUrl = HttpUrl
                .parse(url)
                .newBuilder()
                .host(baseUrl.host())
                .port(baseUrl.port())
                .scheme(baseUrl.scheme())
                .build();

        return targetUrl.toString();
    }
}
