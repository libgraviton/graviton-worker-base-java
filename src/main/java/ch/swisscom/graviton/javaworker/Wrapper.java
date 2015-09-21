/**
 * simple wrapper starting the worker. this class is the jar main class
 */

package ch.swisscom.graviton.javaworker;

import ch.swisscom.graviton.javaworker.lib.Worker;
import ch.swisscom.graviton.javaworker.lib.WorkerAbstract;

/**
 * @author List of contributors
 *         <https://github.com/libgraviton/graviton/graphs/contributors>
 * @license http://opensource.org/licenses/gpl-license.php GNU Public License
 * @link http://swisscom.ch
 */
public class Wrapper {

    /**
     * Main
     * 
     * @param args
     */
    public static void main(String[] args) {
        
        WorkerAbstract exampleWorker = new ExampleWorker();
        
        Worker worker = new Worker(exampleWorker);
        worker.run();
    }
}