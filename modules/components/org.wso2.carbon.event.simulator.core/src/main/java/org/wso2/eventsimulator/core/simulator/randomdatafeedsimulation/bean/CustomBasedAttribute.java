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
package org.wso2.eventsimulator.core.simulator.randomdatafeedsimulation.bean;


import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import org.wso2.eventsimulator.core.simulator.exception.EventSimulationException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * CustomBasedAttribute represents the Random data generator based on custom data list
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
public class CustomBasedAttribute extends FeedSimulationStreamAttributeDto {

    private static final Logger log = Logger.getLogger(FeedSimulationStreamAttributeDto.class);


    /**
     * List of custom data value given by user
     */
    private String[] customDataList;

    public CustomBasedAttribute() { }

    public String[] getCustomDataList() {
        return customDataList;
    }

    private void setCustomDataList(String[] customDataList) {
        this.customDataList = customDataList;
    }

    /**
     * Method to split the data list into seperated values and assign it to customDataList
     *
     * @param customData String that has data list values
     *                   Initial string format is "CEP,ESB,DAS"
     */
    public void setCustomData(String customData) {
        CSVParser csvParser = null;
        List<String> dataList = null;
        try {
            if (!customData.isEmpty()) {
                csvParser = CSVParser.parse(customData, CSVFormat.newFormat(',').withQuote('/'));
                dataList = new ArrayList<>();
                for (CSVRecord record : csvParser) {
                    for (int i = 0; i < record.size(); i++) {
                        dataList.add(record.get(i));
                    }
                }
            } else {
                log.error("Data list cannot be empty");
                throw new RuntimeException("Data list cannot be empty");

            }
        } catch (IOException e) {
            throw new EventSimulationException("I/O error occurs :" + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new EventSimulationException("Data set is null :" + e.getMessage());
        }
        customDataList = dataList.toArray(new String[dataList.size()]);
    }

}

