package com.github.libgraviton.workerbase.util;

import com.github.libgraviton.gdk.api.gateway.OkHttpGateway;
import io.opentracing.contrib.okhttp3.TracingInterceptor;
import io.opentracing.util.GlobalTracer;

public class TracingGateway extends OkHttpGateway {

  public TracingGateway() {
    super();
    okHttp = TracingInterceptor.addTracing(okHttp.newBuilder(), GlobalTracer.get());
  }

}
