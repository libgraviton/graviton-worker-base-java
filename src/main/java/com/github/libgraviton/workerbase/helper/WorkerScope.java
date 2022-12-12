/**
 * 
 */
package com.github.libgraviton.workerbase.helper;

import com.github.libgraviton.workerbase.annotation.GravitonWorkerDiScan;
import com.github.libgraviton.workerbase.gdk.GravitonApi;
import io.activej.inject.annotation.Inject;

import java.util.Properties;

/**
 * this is the "main" scope that holds all needed utilities..
 */
@GravitonWorkerDiScan
public class WorkerScope {

    private final GravitonApi gravitonApi;
    private final Properties properties;

    @Inject
    public WorkerScope(Properties properties, GravitonApi gravitonApi) {
        this.properties = properties;
        this.gravitonApi = gravitonApi;
    }

    public GravitonApi getGravitonApi() {
        return gravitonApi;
    }

    public Properties getProperties() {
        return properties;
    }
}
