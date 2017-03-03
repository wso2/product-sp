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
package org.wso2.eventsimulator.core.simulator.singleventsimulator;

import org.wso2.eventsimulator.core.simulator.bean.FeedSimulationStreamConfiguration;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;


/**
 * Represents Single Event Simulation Configuration class
 */
@XmlRootElement(name = "SingleEventDto")
public class SingleEventDto extends FeedSimulationStreamConfiguration {

    /**
     * List of values of attributes
     */
    private List<String> attributeValues = new ArrayList<>();

    /**
     * Initialize the SingleEventDto
     */
    public SingleEventDto() {
    }

    public List<String> getAttributeValues() {
        return attributeValues;
    }

    public void setAttributeValues(List<String> attributeValues) {
        this.attributeValues = attributeValues;
    }
}
