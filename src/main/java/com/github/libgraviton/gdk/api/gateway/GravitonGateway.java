package com.github.libgraviton.gdk.api.gateway;

import com.github.libgraviton.gdk.api.Request;
import com.github.libgraviton.gdk.api.Response;
import com.github.libgraviton.gdk.exception.CommunicationException;

public interface GravitonGateway {

    Response execute(Request request) throws CommunicationException;

}
