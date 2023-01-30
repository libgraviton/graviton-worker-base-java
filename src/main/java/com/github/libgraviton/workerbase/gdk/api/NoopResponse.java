package com.github.libgraviton.workerbase.gdk.api;

import com.github.libgraviton.workerbase.gdk.api.header.HeaderBag;

/**
 * Graviton response wrapper with additional functionality and simplified interface.
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/gdk-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
public class NoopResponse extends Response {

    public NoopResponse(NoopRequest request) {
        this.request = request;
        this.code = -1;
        this.isSuccessful = true;
        this.message = "This is not the response you are looking for";
        this.body = null;
        this.headers = new HeaderBag.Builder().build();
    }
}
