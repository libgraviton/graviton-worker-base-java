/**
 * worker exception
 */

package com.github.libgraviton.workerbase;

/**
 * @author List of contributors
 *         <https://github.com/libgraviton/graviton/graphs/contributors>
 * @link http://swisscom.ch
 */
public class WorkerException extends Exception {

    /**
     * serial
     */
    private static final long serialVersionUID = 1L;
    
    public WorkerException(String message) {
        super(message);
    }

}
