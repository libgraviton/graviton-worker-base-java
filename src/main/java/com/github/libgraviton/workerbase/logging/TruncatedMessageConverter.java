package com.github.libgraviton.workerbase.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.List;

/**
 * <p>Allows to truncate the message size and add a postfix to mark the message visibly as truncated.</p>
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/graviton-worker-base-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
public class TruncatedMessageConverter extends ClassicConverter {

    @Override
    public String convert(ILoggingEvent event) {
        if(getOptions().size() != 2) {
            throw new IllegalArgumentException("Expected 2 arguments but got " + getOptionList().size() + ".");
        }

        int maxMessageLength = Integer.parseInt(getOptions().get(0));
        String postfix = getOptions().get(1);
        String message = event.getFormattedMessage();

        if (message.length() >= maxMessageLength) {
            message = message.substring(0, maxMessageLength) + " " + postfix;
        }

        return message;
    }

    /**
     * For some reason getOptionList() is protected. With getOptions() it's exposed and testable.
     */
    public List<String> getOptions() {
        return getOptionList();
    }
}
