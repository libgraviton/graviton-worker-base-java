/**
 * worker exception
 */

package com.github.libgraviton.workerbase.exception;

import java.io.Serial;

/**
 * <p>WorkerExceptionRetriable class.</p>
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/graviton-worker-base-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
public class WorkerExceptionRetriable extends WorkerException {

    @Serial
    private static final long serialVersionUID = 1L;

    public WorkerExceptionRetriable() {
    }

    public WorkerExceptionRetriable(String message) {
        super(message);
    }

    public WorkerExceptionRetriable(String message, Throwable cause) {
        super(message, cause);
    }

    public WorkerExceptionRetriable(Throwable cause) {
        super(cause);
    }

    public WorkerExceptionRetriable(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
