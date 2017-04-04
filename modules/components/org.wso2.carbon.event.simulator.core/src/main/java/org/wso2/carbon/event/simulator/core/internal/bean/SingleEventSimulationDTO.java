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

import java.util.Arrays;

/**
 * Represents Single Event Simulation Configuration class
 */
public class SingleEventSimulationDTO extends StreamConfigurationDTO {
    /**
     * List of values of attributes
     */
    private Object[] data;
    private long timestamp = -1;

    /**
     * Initialize the SingleEventSimulationDTO
     */
    public SingleEventSimulationDTO() {
        super();
    }

    public Object[] getAttributeValues() {
        return (data != null) ? Arrays.copyOf(data, data.length) : new Object[0];
    }

    public void setAttributeValues(Object[] attributes) {
        this.data = Arrays.copyOf(attributes, attributes.length);

    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "\n Stream name : " + getStreamName() +
                "\n Execution plan name : " + getExecutionPlanName() +
                "\n Event timestamp : " + getTimestamp() +
                "\n Event data : " + Arrays.toString(getAttributeValues());
    }
}
