package org.wso2.eventsimulator.core.eventGenerator.util.exceptions;


/**
 * customized exceptions class for parsing simulation and stream configurations
 */
public class ConfigurationParserException extends RuntimeException {

    /**
     * Throws customizes exceptions when parsing simulation and stream configurations
     *
     * @param message Error Message
     */
    public ConfigurationParserException(String message) {
        super(message);
    }
}
