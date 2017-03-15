package org.wso2.eventsimulator.core.eventGenerator.util.exceptions;

/**
 * customized exceptions class for event generation
 */
public class EventGenerationException extends RuntimeException {

    /**
     * Throws customizes event generation exceptions
     *
     * @param message Error Message
     */
    public EventGenerationException(String message) {
        super(message);
    }
}
