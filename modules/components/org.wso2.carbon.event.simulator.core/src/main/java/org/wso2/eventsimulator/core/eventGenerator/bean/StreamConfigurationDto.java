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

import org.wso2.eventsimulator.core.eventGenerator.EventGenerator;

/**
 * StreamConfigurationDto class represents the feed simulation configuration for an input stream.
 * This is an abstract class, CSVSimulationDto and RandomDataSimulationDto extends this parent class
 *
 * @see CSVSimulationDto
 * @see RandomSimulationDto
 * @see DBSimulationDto
 */
public abstract class StreamConfigurationDto {

    private String timestampAttribute = "";
    private String streamName;
    private String executionPlanName;
    private EventGenerator.GeneratorType generatorType;

    public StreamConfigurationDto() {
    }

    public final String getTimestampAttribute() {
        return timestampAttribute;
    }

    public final void setTimestampAttribute(String timestampAttribute) {
        this.timestampAttribute = timestampAttribute;
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

    public EventGenerator.GeneratorType getGeneratorType() {
        return generatorType;
    }

    public void setGeneratorType(EventGenerator.GeneratorType generatorType) {
        this.generatorType = generatorType;
    }
}

