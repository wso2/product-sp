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
import org.wso2.eventsimulator.core.simulator.exception.EventSimulationException;
import org.wso2.eventsimulator.core.simulator.randomdatafeedsimulation.bean.FeedSimulationStreamAttributeDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * CustomBasedAttributeDto represents the Random data generator based on custom data list
 * Constant value to represent to this type is ""CUSTOMDATA"
 * It has data list which is given by user
 * <p>
 * <p>
 * Eg: If user want to generate value for an attribute WSO2 products
 * then user cangive databaseFeedSimulation list of products as
 * "list": "CEP,ESB,DAS" with in json body
 * </p>
 * <p>
 * <p>
 * Eg for json string for configuration
 * {
 * "type": "CUSTOMDATA",
 * "list": "CEP,ESB,DAS"
 * }
 * </p>
 */
public class CustomBasedAttributeDto extends RandomAttributeDto{
    private static final Logger log = Logger.getLogger(FeedSimulationStreamAttributeDto.class);

    /**
     * List of custom data value given by user
     */
    private String[] customDataList;

    public CustomBasedAttributeDto() {
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

        List<String> dataList = new ArrayList<String>(
                Arrays.asList(customDataList.split("\\s*,\\s*")));
        for (int i = 0; i < dataList.size(); i++) {
            if (!dataList.get(i).isEmpty() && dataList.get(i) != null) {
                continue;
            } else {
                throw new EventSimulationException("Data list items cannot contain null or empty values");
            }
        }
        this.customDataList = dataList.toArray(new String[dataList.size()]);
    }
}
