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

package org.wso2.eventsimulator.core.eventGenerator.randomEventGeneration.bean;

import org.apache.log4j.Logger;
import org.wso2.eventsimulator.core.eventGenerator.util.exceptions.ConfigurationParserException;

/**
 * CustomBasedAttributeDto represents the Random data generator based on custom data list
 * It has a data list which is given by user
 * <p>
 * <p>
 * Eg: If user want to generate value for an attribute WSO2 products
 * then user can give list of products as
 * "list": "CEP,ESB,DAS" with in json body
 * </p>
 * <p>
 * <p>
 * Eg for json string for configuration
 * {
 * "type": "CUSTOM_DATA",
 * "list": "CEP,ESB,DAS"
 * }
 * </p>
 */
public class CustomBasedAttributeDto extends RandomAttributeDto {
    private static final Logger log = Logger.getLogger(CustomBasedAttributeDto.class);

    /**
     * List of custom data value given by user
     */
    private String[] customDataList;

    public CustomBasedAttributeDto() {
        super();
    }

    public String[] getCustomDataList() {
        return customDataList;
    }

    /**
     * Method to split the data list into seperated values and assign it to customDataList
     *
     * @param customDataList String that has data list values
     *                       Initial string format is "CEP,ESB,DAS"
     */
    public void setCustomData(String customDataList) {


        String[] dataList = customDataList.split("\\s*,\\s*");

        /*
        * dataList can not contain empty strings. if it does, throw an exception
        * */
        for (String data : dataList) {
            if (data.isEmpty()) {
                throw new ConfigurationParserException("Data list items cannot contain null or empty values");
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Set data list for custom based random simulation.");
        }
        this.customDataList = dataList;
    }
}
