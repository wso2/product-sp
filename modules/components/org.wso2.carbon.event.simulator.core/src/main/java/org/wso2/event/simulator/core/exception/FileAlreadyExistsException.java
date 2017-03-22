package org.wso2.event.simulator.core.exception;

/**
 * customized exception class for duplicate files uploaded
 */
public class FileAlreadyExistsException extends Exception {

    /**
     * Throws customizes extensions for duplicate files uploaded
     *
     * @param message Error Message
     */
    public FileAlreadyExistsException(String message) {
        super(message);
    }
}
