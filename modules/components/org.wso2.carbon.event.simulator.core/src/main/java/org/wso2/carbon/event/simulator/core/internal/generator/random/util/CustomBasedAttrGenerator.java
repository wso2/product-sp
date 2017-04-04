/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.event.simulator.core.internal.generator.random.util;

import com.google.gson.Gson;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.event.simulator.core.exception.InvalidConfigException;
import org.wso2.carbon.event.simulator.core.internal.bean.CustomBasedAttributeDTO;
import org.wso2.carbon.event.simulator.core.internal.generator.random.RandomAttributeGenerator;
import org.wso2.carbon.event.simulator.core.internal.util.EventSimulatorConstants;

import static org.wso2.carbon.event.simulator.core.internal.util.CommonOperations.checkAvailabilityOfArray;

import java.util.ArrayList;
import java.util.Random;

/**
 * CustomBasedAttrGenerator class is responsible for generating attribute values from the data list provided by user
 * This class implements interface RandomAttributeGenerator
 */
public class CustomBasedAttrGenerator implements RandomAttributeGenerator {
    private static final Logger log = LoggerFactory.getLogger(CustomBasedAttrGenerator.class);
    private CustomBasedAttributeDTO customBasedAttrConfig = new CustomBasedAttributeDTO();

    /**
     * CustomBasedAttrGenerator() constructor validates the custom data attribute configuration provided and creates a
     * CustomBasedAttributeDTO object containing custom based attribute generation configuration
     *
     * @param attributeConfig JSON object of the custom data attribute configuration
     * @throws InvalidConfigException if attribute configuration is invalid
     */
    public CustomBasedAttrGenerator(JSONObject attributeConfig) throws InvalidConfigException {
        if (checkAvailabilityOfArray(attributeConfig, EventSimulatorConstants.CUSTOM_DATA_BASED_ATTRIBUTE_LIST)) {
            Gson gson = new Gson();
            ArrayList dataValues = gson.fromJson(attributeConfig.
                    getJSONArray(EventSimulatorConstants.CUSTOM_DATA_BASED_ATTRIBUTE_LIST).toString(), ArrayList.class);
            customBasedAttrConfig.setCustomData(dataValues.toArray(new String[dataValues.size()]));
            if (log.isDebugEnabled()) {
                log.debug("Set data list for custom based random simulation.");
            }
        } else {
            throw new InvalidConfigException("Data list is not given for " +
                    RandomAttributeGenerator.RandomDataGeneratorType.CUSTOM_DATA_BASED + " simulation. Invalid " +
                    "attribute configuration provided : " + attributeConfig.toString());
        }
    }

    /**
     * Generate data with in given data list
     * <p>
     * Initialize Random to select random element from array
     *
     * @return generated data from custom data list
     */
    @Override
    public Object generateAttribute() {
        /*
         * randomElementSelector will be assigned a pseudoRandom integer value from 0 to (datalist.length - 1)
         * the data element in the randomElementSelector's position will be assigned to result and returned
         * */
        Random random = new Random();
        int randomElementSelector = random.nextInt(customBasedAttrConfig.getCustomDataList().length);
        return customBasedAttrConfig.getCustomDataList()[randomElementSelector];
    }
}
