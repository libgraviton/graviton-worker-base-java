/**
 * worker exception
 */

package com.github.libgraviton.workerbase;

/**
 * <p>WorkerException class.</p>
 *
 * @author List of contributors
 *         <https://github.com/libgraviton/graviton/graphs/contributors>
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
public class WorkerException extends Exception {

    /**
     * serial
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * <p>Constructor for WorkerException.</p>
     *
     * @param message a {@link java.lang.String} object.
     */
    public WorkerException(String message) {
        super(message);
    }

}
