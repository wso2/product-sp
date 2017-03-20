package org.wso2.eventsimulator.core.eventGenerator.util.exceptions;

/**
 * customized exceptions class for validation whethere the number of attributes generated is equal to the number of
 * attributes in stream
 */
public class InsufficientAttributesException extends Exception {


    /**
     * Throws customizes validating the number of attributes generated
     *
     * @param message Error Message
     */
    public InsufficientAttributesException(String message) {
        super(message);
    }
}
