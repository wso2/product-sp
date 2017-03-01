package org.wso2.eventsimulator.core.simulator.exception;

/**
 * Created by ruwini on 1/29/17.
 */
public class DatabaseConnectionException extends RuntimeException {
    /**
     * Throws customizes database connection exceptions
     *
     * @param message Error Message
     */
    public DatabaseConnectionException(String message) {
        super(message);
    }
}

