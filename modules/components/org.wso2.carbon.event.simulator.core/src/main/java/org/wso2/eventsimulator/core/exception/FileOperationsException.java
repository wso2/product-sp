package org.wso2.eventsimulator.core.exception;

/**
 * customized exception class for copying and deleting files
 */
public class FileDeploymentException extends Exception {
    /**
     * Throws customizes exception copying and deleting files
     *
     * @param message Error Message
     */
    public FileDeploymentException(String message) {
        super(message);
    }

    /**
     * Throws customizes exception copying and deleting files
     *
     * @param message Error Message
     * @param e       exception which caused the file deployment exception
     */
    public FileDeploymentException(String message, Exception e) {
        super(message, e);
    }
}
