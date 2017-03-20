package org.wso2.eventsimulator.core.eventGenerator.util.exceptions;

/**
 * customized exceptions class for copying and deleting files
 */
public class FileDeploymentException extends Exception {
    /**
     * Throws customizes exceptions copying and deleting files
     *
     * @param message Error Message
     */
    public FileDeploymentException(String message) {
        super(message);
    }

    /**
     * Throws customizes exceptions copying and deleting files
     *
     * @param message Error Message
     * @param e       exception which caused the file deployment exception
     */
    public FileDeploymentException(String message, Exception e) {
        super(message, e);
    }
}
