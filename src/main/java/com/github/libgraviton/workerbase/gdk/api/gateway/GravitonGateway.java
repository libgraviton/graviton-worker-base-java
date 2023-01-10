package com.github.libgraviton.workerbase.gdk.api.gateway;

import com.github.libgraviton.workerbase.gdk.api.Request;
import com.github.libgraviton.workerbase.gdk.api.Response;
import com.github.libgraviton.workerbase.gdk.exception.CommunicationException;

public interface GravitonGateway {
    Response execute(Request request) throws CommunicationException;
}
