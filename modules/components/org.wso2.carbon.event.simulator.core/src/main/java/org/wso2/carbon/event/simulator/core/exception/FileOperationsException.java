package org.wso2.carbon.event.simulator.core.exception;

/**
 * customized exception class for copying and deleting files
 */
public class FileOperationsException extends Exception {
    /**
     * Throws customizes exception copying and deleting files
     *
     * @param message Error Message
     */
    public FileOperationsException(String message) {
        super(message);
    }

    /**
     * Throws customizes exception copying and deleting files
     *
     * @param message Error Message
     * @param e       exception which caused the file deployment exception
     */
    public FileOperationsException(String message, Exception e) {
        super(message, e);
    }
}
