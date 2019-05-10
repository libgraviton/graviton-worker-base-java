package com.github.libgraviton.workerbase.util.tracing;

import com.rabbitmq.client.AMQP.BasicProperties;
import io.opentracing.propagation.TextMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class TextMapExtractor implements TextMap {

  private Map<String, String> map;

  public TextMapExtractor(
    BasicProperties basicProperties
  ) {
    map = new HashMap<>();
    if (basicProperties.getCorrelationId() != null) {
      map.put("uber-trace-id", basicProperties.getCorrelationId());
    }
  }

  @Override
  public Iterator<Entry<String, String>> iterator() {
    return map.entrySet().iterator();
  }

  @Override
  public void put(String key, String value) {

  }
}
