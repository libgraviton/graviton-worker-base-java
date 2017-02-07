package com.github.libgraviton.workerbase.mq;

public class ParallelConsumer implements Consumer {

    private Consumer consumer;

    public ParallelConsumer(Consumer consumer) {
        this.consumer = consumer;
    }

    @Override
    public void consume(long messageId, String message) {
        new Thread(new ConsumerRunner(consumer, messageId, message)).start();
    }

    private class ConsumerRunner implements Runnable {

        private Consumer consumer;

        private long messageId;

        private String message;

        ConsumerRunner(Consumer consumer, long messageId, String message) {
            this.consumer = consumer;
            this.messageId = messageId;
            this.message = message;
        }

        @Override
        public void run() {
            consumer.consume(messageId, message);
        }
    }

}
