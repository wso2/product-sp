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

package org.wso2.eventsimulator.core.generator.random.util;

import org.wso2.eventsimulator.core.generator.random.bean.CustomBasedAttributeDto;

import java.util.Random;

/**
 * CustomBasedGenerator class is responsible for generating attribute values from the data list provided by user
 */
public class CustomBasedGenerator {

    private CustomBasedGenerator() {
    }

    /**
     * Generate data with in given data list
     * <p>
     * Initialize Random to select random element from array
     *
     * @param customBasedAttributeDto a customBasedAttributeDto object
     * @return generated data from custom data list
     */
    public static Object generateCustomBasedData(CustomBasedAttributeDto customBasedAttributeDto) {
        Random random = new Random();
        /*
        * randomElementSelector will be assigned a pseudoRandom integer value from 0 to (datalist.length - 1)
        * the data element in the randomElementSelector's position will be assigned to result and returned
         * */
        int randomElementSelector = random.nextInt(customBasedAttributeDto.getCustomDataList().length);
        Object result;
        result = customBasedAttributeDto.getCustomDataList()[randomElementSelector];
        return result;
    }

}
