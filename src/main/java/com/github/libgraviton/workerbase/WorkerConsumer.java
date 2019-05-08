/**
 * queue consumer - here we have the main logic
 */

package com.github.libgraviton.workerbase;

import com.fasterxml.jackson.jr.ob.JSON;
import com.github.libgraviton.messaging.MessageAcknowledger;
import com.github.libgraviton.messaging.consumer.AcknowledgingConsumer;
import com.github.libgraviton.messaging.consumer.PropertyConsumer;
import com.github.libgraviton.messaging.exception.CannotConsumeMessage;
import com.github.libgraviton.workerbase.model.QueueEvent;

import com.rabbitmq.client.AMQP.BasicProperties;
import io.jaegertracing.internal.propagation.TextMapCodec;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.propagation.Format.Builtin;
import io.opentracing.propagation.TextMap;
import io.opentracing.propagation.TextMapExtractAdapter;
import io.opentracing.util.GlobalTracer;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * <p>WorkerConsumer class.</p>
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/graviton-worker-base-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
public class WorkerConsumer implements AcknowledgingConsumer, PropertyConsumer {

    private WorkerAbstract worker;

    private MessageAcknowledger acknowledger;

    protected BasicProperties basicProperties;

    /**
     * constructor
     *
     * @param worker worker
     */
    public WorkerConsumer(WorkerAbstract worker) {
        this.worker = worker;
    }

    @Override
    public void consume(String messageId, String message) throws CannotConsumeMessage {
        // is there a correlationId?
        Span workSpan;
        String spanName = "do-work";

        if (basicProperties.getCorrelationId() != null) {
            Map<String, String> textMap = new HashMap<>();
            textMap.put("uber-trace-id", basicProperties.getCorrelationId());

            SpanContext parentSpan = GlobalTracer.get().extract(Builtin.TEXT_MAP_EXTRACT, new TextMapExtractAdapter(textMap));

            workSpan = GlobalTracer.get().buildSpan(spanName).asChildOf(parentSpan).start();
        } else {
            workSpan = GlobalTracer.get().buildSpan(spanName).start();
        }

        QueueEvent queueEvent;
        try {
            queueEvent = JSON.std.beanFrom(QueueEvent.class, message);
        } catch (IOException e) {
            throw new CannotConsumeMessage(messageId, message, e);
        } finally {
            workSpan.finish();
        }
        worker.handleDelivery(queueEvent, messageId, acknowledger);
    }

    @Override
    public void setAcknowledger(MessageAcknowledger acknowledger) {
        this.acknowledger = acknowledger;
    }

    @Override
    public void setBasicProperties(BasicProperties basicProperties) {
        this.basicProperties = basicProperties;
    }
}
