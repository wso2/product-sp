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

/**
 * StreamConfigurationDTO class represents the event simulation configuration for an input stream.
 * This is an abstract class, CSVSimulationDTO and RandomDataSimulationDto extends this parent class
 *
 * @see CSVSimulationDTO
 * @see RandomSimulationDTO
 * @see DBSimulationDTO
 */
public abstract class StreamConfigurationDTO {

    private String streamName;
    private String executionPlanName;
    private String timestampAttribute;
    private long timeInterval;

    public StreamConfigurationDTO() {
    }

    public final String getStreamName() {
        return streamName;
    }

    public final void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public final String getExecutionPlanName() {
        return executionPlanName;
    }

    public final void setExecutionPlanName(String executionPlanName) {
        this.executionPlanName = executionPlanName;
    }

    public final String getTimestampAttribute() {
        return timestampAttribute;
    }

    public final void setTimestampAttribute(String timestampAttribute) {
        this.timestampAttribute = timestampAttribute;
    }

    public long getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(long timeInterval) {
        this.timeInterval = timeInterval;
    }
}

