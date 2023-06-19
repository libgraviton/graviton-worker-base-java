/**
 * worker exception
 */

package com.github.libgraviton.workerbase.exception;

import java.io.Serial;

/**
 * if a worker throws this, it will never be redelivered/retried!
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/graviton-worker-base-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
public class WorkerExceptionFatal extends WorkerException {

    @Serial
    private static final long serialVersionUID = 1L;

    public WorkerExceptionFatal() {
    }

    public WorkerExceptionFatal(String message) {
        super(message);
    }

    public WorkerExceptionFatal(String message, Throwable cause) {
        super(message, cause);
    }

    public WorkerExceptionFatal(Throwable cause) {
        super(cause);
    }

    public WorkerExceptionFatal(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
