package org.wso2.sp.open.tracer.client;

/**
 * This is the exception class which is thrown for any unexpected/wrong
 * configuration in the open tracer configs.
 */
public class InvalidTracerConfigurationException extends Exception {
    public InvalidTracerConfigurationException(String msg) {
        super(msg);
    }

    public InvalidTracerConfigurationException(String msg, Exception ex) {
        super(msg, ex);
    }
}
