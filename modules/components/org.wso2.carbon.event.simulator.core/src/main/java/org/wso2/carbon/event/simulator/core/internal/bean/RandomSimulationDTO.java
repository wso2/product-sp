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

package org.wso2.carbon.event.simulator.core.internal.bean;

import java.util.HashMap;
import java.util.List;

/**
 * RandomSimulationDTO represents configuration details for simulate using random data
 * <p>
 * This file extends StreamConfigurationDTO abstract class
 * <p>
 * Sample configuration for RandomSimulationDTO :
 * {
 * <p>
 * "timestampStartTime" : "1488615136958",
 * "timestampEndTime" : "1488615136998",
 * "delay": "1000",
 * "streamConfiguration" :[
 * {
 * <p>
 * "simulationType" : "RANDOM_DATA_SIMULATION",
 * "streamName" : "FooStream","executionPlanName" :
 * "TestExecutionPlan",
 * "timeInterval": "5",
 * "attributeConfiguration": [
 * {
 * <p>
 * "type": "PROPERT_YBASED",
 * "category": "Contact",
 * "property": "Full Name",
 * },
 * {
 * <p>
 * "type": "REGEX_BASED",
 * "pattern": "[+]?[0-9]*\\.?[0-9]+"
 * },
 * {
 * <p>
 * "type": "PRIMITIVE_BASED",
 * "primitiveType" : "LONG",
 * "min": "2",
 * "max": "200"
 * }
 * ]
 * }
 */
public class RandomSimulationDTO extends StreamConfigurationDTO {

    private long timeInterval;
    private List<HashMap<String, Object>> attributeConfiguration;

    public RandomSimulationDTO() {
    }

    public long getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(long timeInterval) {
        this.timeInterval = timeInterval;
    }

    public List<HashMap<String, Object>> getAttributeConfiguration() {
        return attributeConfiguration;
    }

    public void setAttributeConfiguration(List<HashMap<String, Object>> attributeConfiguration) {
        this.attributeConfiguration = attributeConfiguration;
    }
}
