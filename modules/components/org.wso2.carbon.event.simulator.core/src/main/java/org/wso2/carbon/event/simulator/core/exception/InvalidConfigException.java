package org.wso2.carbon.event.simulator.core.exception;


/**
 * customized exception class for parsing simulation and stream configurations
 */
public class InvalidConfigException extends Exception {

    /**
     * Throws customizes exception when parsing simulation and stream configurations
     *
     * @param message Error Message
     */
    public InvalidConfigException(String message) {
        super(message);
    }

    /**
     * Throws customizes exception when parsing simulation and stream configurations
     *
     * @param message Error Message
     * @param e       exception that caused the InvalidConfigException
     */
    public InvalidConfigException(String message, Exception e) {
        super(message, e);
    }

}
