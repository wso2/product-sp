package org.wso2.carbon.event.simulator.core.internal.util;

import org.json.JSONObject;

/**
 * CommonOperations class is used to perform functions common to all generators such as
 * 1. cast the streamConfigurationDto object to the respective simulation type object
 * 2. check whether the number of attributes generated is equal to the number of stream attributes.
 */
public class CommonOperations {


    /**
     * checkAvailability() performs the following checks on the the json object and key provided
     * 1. has
     * 2. isNull
     * 3. isEmpty
     *
     * @param configuration JSON object containing configuration
     * @param key           name of key
     * @return true if checks are successful, else false
     */
    public static Boolean checkAvailability(JSONObject configuration, String key) {

        return configuration.has(key)
                && !configuration.isNull(key)
                && !configuration.getString(key).isEmpty();
    }

    /**
     * checkAvailabilityOfArray() performs the following checks on the the json object and key provided.
     * This method is used for key's that contains json array values.
     * 1. has
     * 2. isNull
     * 3. isEmpty
     *
     * @param configuration JSON object containing configuration
     * @param key           name of key
     * @return true if checks are successful, else false
     */
    public static Boolean checkAvailabilityOfArray(JSONObject configuration, String key) {

        return configuration.has(key)
                && !configuration.isNull(key)
                && configuration.getJSONArray(key).length() > 0;
    }

}
