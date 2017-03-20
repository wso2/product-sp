package org.wso2.eventsimulator.core.eventGenerator.util.exceptions;


/**
 * customized exceptions class for parsing simulation and stream configurations
 */
public class InvalidConfigException extends Exception {

    /**
     * Throws customizes exceptions when parsing simulation and stream configurations
     *
     * @param message Error Message
     */
    public InvalidConfigException(String message) {
        super(message);
    }

    /**
     * Throws customizes exceptions when parsing simulation and stream configurations
     *
     * @param message Error Message
     * @param e       exception that caused the InvalidConfigException
     */
    public InvalidConfigException(String message, Exception e) {
        super(message, e);
    }

}
