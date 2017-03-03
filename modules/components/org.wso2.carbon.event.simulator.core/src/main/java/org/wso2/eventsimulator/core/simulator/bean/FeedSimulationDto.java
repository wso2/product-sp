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

package org.wso2.eventsimulator.core.simulator.bean;


import java.util.List;

/**
 * FeedSimulationDto represents the configuration details of Feed simulation
 * <p>
 * For an execution plan It may have one or more input streams.
 * this class has configuration details to simulate each input streams in different cases.
 * such as simulate using CSV File, simulate using Random Data and simulate using
 * database resource.
 * </p>
 */
public class FeedSimulationDto {
    /**
     * A flag used to start the orderByTimeStamp process
     * <p>
     * If orderByTimeStampProcess is enabled then
     * events are send to input handler in an order according to given timestamp of all input streams
     */
    private boolean orderByTimeStamp = false;

    /**
     * List of stream simulation configuration for feed simulation
     * Stream Configuration simulation can be any on of bellow mentioned cases
     * 1. Configuration for CSV file
     * 2. Configuration for Random Data
     * 3. Configuration for Database resource
     */
    private List<FeedSimulationStreamConfiguration> streamConfigurationList;

    private int noOfParallelSimulationSources = 1;

    /**
     * Initialize FeedSimulationDto
     */
    public FeedSimulationDto() {}

    public boolean getOrderByTimeStamp() { return orderByTimeStamp;}

    public void setOrderByTimeStamp(boolean orderByTimeStamp) {
        this.orderByTimeStamp = orderByTimeStamp;
    }

    public List<FeedSimulationStreamConfiguration> getStreamConfigurationList() {
        return streamConfigurationList;
    }

    public void setStreamConfigurationList(List<FeedSimulationStreamConfiguration> streamConfigurationList) {
        this.streamConfigurationList = streamConfigurationList;
    }

    public int getNoOfParallelSimulationSources() { return noOfParallelSimulationSources;}

    public void setNoOfParallelSimulationSources(int noOfParallelSimulationSources) { this.noOfParallelSimulationSources = noOfParallelSimulationSources;}
}
