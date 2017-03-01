/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.eventsimulator.core.simulator.bean;

import org.wso2.eventsimulator.core.simulator.csvFeedSimulation.CSVFileSimulationDto;
import org.wso2.eventsimulator.core.simulator.databaseFeedSimulation.DatabaseFeedSimulationDto;
import org.wso2.eventsimulator.core.simulator.randomdatafeedsimulation.bean.RandomDataSimulationDto;

/**
 * FeedSimulationStreamConfiguration class represents the feed simulation configuration for an input stream.
 * This is an abstract class, CSVFileSimulationDto and RandomDataSimulationDto extends this parent class
 *
 * @see CSVFileSimulationDto
 * @see RandomDataSimulationDto
 * @see DatabaseFeedSimulationDto
 */
public abstract class FeedSimulationStreamConfiguration {
    /**
     * Simulation type for an input stream
     * It can be
     * 1. RandomDataSimulation
     * 2. FileFeedSimulation
     * 3. DatabaseSimulation
     * 4. SingleEventSimulation
     * These values are constants and choose by user
     */

//    todo R 28/02/2017 set execution plan name here and make simulationType enum
//    private String simulationType;
    public enum SimulationType {FILE_SIMULATION,RANDOM_DATA_SIMULATION,DATABASE_SIMULATION,SINGLE_EVENT}

    private SimulationType simulationType;

    private String timestampAttribute = "";

    private String streamName;

    private String executionPlanName;

    public final SimulationType getSimulationType() {
        return simulationType;
    }

    public final void setSimulationType(SimulationType simulationType) {
        this.simulationType = simulationType;
    }

    public final String getTimestampAttribute() {
        return timestampAttribute;
    }

    public final void setTimestampAttribute(String timestampAttribute) {
        if (timestampAttribute != null && !timestampAttribute.isEmpty()) {
            this.timestampAttribute = timestampAttribute;
        }
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
}

