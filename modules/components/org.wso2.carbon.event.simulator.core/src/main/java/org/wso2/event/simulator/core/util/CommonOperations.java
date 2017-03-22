package org.wso2.event.simulator.core.util;

/**
 * CommonOperations class is used to perform functions common to all generators such as
 * 1. cast the streamConfigurationDto object to the respective simulation type object
 * 2. check whether the number of attributes generated is equal to the number of stream attributes.
 */
public class CommonOperations {

    /**
     * cast() method is used to case streamConfigurationDto object to an object of the respective simulation config
     * type
     *
     * @param simulationType the configuration class for simulation type
     * @param streamConfig   the stream configuration object
     */
    public static Object cast(Class simulationType, Object streamConfig) {
        if (simulationType.isInstance(streamConfig)) {
            return simulationType.cast(streamConfig);
        } else {
            return null;
        }
    }

    /**
     * checkAttributes() checks whether the number of attributes generated is equal to the number of stream attributes.
     *
     * @param genAttrCount    the number of attributes generated
     * @param streamAttrCount number of stream attributes
     */
    public static Boolean checkAttributes(int genAttrCount, int streamAttrCount) {
        return genAttrCount == streamAttrCount;
    }
}
