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

import javax.xml.bind.annotation.XmlRootElement;


/**
 * Represents Single Event Simulation Configuration class
 */
@XmlRootElement(name = "SingleEventSimulationDto")
public class SingleEventSimulationDto extends StreamConfigurationDto {

    private static final Logger log = LoggerFactory.getLogger(SingleEventSimulationDto.class);

    /**
     * List of values of attributes
     */
    private String[] attributeValues;
    private Long timestamp = -1L;

    /**
     * Initialize the SingleEventSimulationDto
     */
    public SingleEventSimulationDto() {
        super();
    }

    public String[] getAttributeValues() {
        return attributeValues;
    }

    public void setAttributeValues(String[] attributes) {

        this.attributeValues = attributes;

    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
