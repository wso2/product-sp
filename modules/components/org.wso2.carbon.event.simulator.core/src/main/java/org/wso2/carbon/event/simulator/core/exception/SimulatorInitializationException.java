package org.wso2.carbon.event.simulator.core.exception;

/**
 * customized exception class for Simulator Initialization
 */
public class SimulatorInitializationException extends RuntimeException {
    /**
     * Throws customizes Simulator Initialization exception
     *
     * @param message Error Message
     */
    public SimulatorInitializationException(String message) {
        super(message);
    }

    /**
     * Throws customizes Simulator Initialization exception
     *
     * @param message Error Message
     * @param e       exception which caused the Simulator Initialization exception
     */
    public SimulatorInitializationException(String message, Exception e) {
        super(message, e);
    }
}
