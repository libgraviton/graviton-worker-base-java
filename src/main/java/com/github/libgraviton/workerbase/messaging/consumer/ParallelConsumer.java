package com.github.libgraviton.workerbase.messaging.consumer;

import com.github.libgraviton.workerbase.messaging.consumer.Consumer;
import com.github.libgraviton.workerbase.messaging.exception.CannotConsumeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParallelConsumer implements Consumer {

    private static final Logger LOG = LoggerFactory.getLogger(com.github.libgraviton.workerbase.messaging.consumer.ParallelConsumer.class);

    private Consumer consumer;

    public ParallelConsumer(Consumer consumer) {
        this.consumer = consumer;
    }

    @Override
    public void consume(String messageId, String message) {
        new Thread(new ConsumerRunner(consumer, messageId, message)).start();
    }

    private class ConsumerRunner implements Runnable {

        private Consumer consumer;

        private String messageId;

        private String message;

        ConsumerRunner(Consumer consumer, String messageId, String message) {
            this.consumer = consumer;
            this.messageId = messageId;
            this.message = message;
        }

        @Override
        public void run() {
            try {
                consumer.consume(messageId, message);
            } catch (CannotConsumeMessage e) {
                LOG.warn(String.format("Consumer '%s' failed: '%s'", consumer, e.getMessage()));
            }
        }
    }

}
