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
package org.wso2.eventsimulator.core.eventGenerator.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.eventsimulator.core.eventGenerator.util.exceptions.ConfigurationParserException;

import javax.xml.bind.annotation.XmlRootElement;


/**
 * Represents Single Event Simulation Configuration class
 */
@XmlRootElement(name = "SingleEventDto")
public class SingleEventDto extends FeedSimulationStreamConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SingleEventDto.class);

    /**
     * List of values of attributes
     */
    private String[] attributeValues;
    private Long timestamp = -1L;

    /**
     * Initialize the SingleEventDto
     */
    public SingleEventDto() {
        super();
    }

    public String[] getAttributeValues() {
        return attributeValues;
    }

    public void setAttributeValues(String attributeValues) {

        /*
        * convert the string of attribute values to a string array by splitting at "."
        * check whether all attribute values specified are non empty and not null
        * if yes, assign the array to attributeValues
        * else, throw an exception
        * */
        String[] dataList = attributeValues.split("\\s*,\\s*");

        for (String data : dataList) {
            if (data.isEmpty()) {
                throw new ConfigurationParserException("Attribute values cannot contain empty values");
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Set attribute values for single event simulation");
        }

        this.attributeValues = dataList;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
