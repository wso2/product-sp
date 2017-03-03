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

import org.wso2.eventsimulator.core.simulator.bean.FeedSimulationStreamConfiguration;

import java.util.ArrayList;
import java.util.List;


/**
 * RandomDataSimulationDto represents configuration details for simulate using
 * Random data
 * <p>
 * This file extends FeedSimulationStreamConfiguration abstract class
 * Constant value for this simulation type is RandomDataSimulation
 * <p>
 * Sample configuration for RandomDataSimulationDto :
 * {
 * "simulationType" : "RandomDataSimulation",
 * "streamName": "streamName",
 * "executionPlanName" : "planName",
 * "events": "5",
 * "delay": "200",
 * "attributeConfiguration": [
 * {
 * <p>
 * "type": "PROPERTYBASED",
 * "category": "Contact",
 * "property": "Full Name",
 * },
 * {
 * <p>
 * "type": "REGEXBASED",
 * "pattern": "[+]?[0-9]*\\.?[0-9]+"
 * },
 * {
 * <p>
 * "type": "PRIMITIVEBASED",
 * "min": "2",
 * "max": "200",
 * "length": "2",
 * },
 * {
 * <p>
 * "type": "custom",
 * "list": "2,3,4"
 * },
 * ]
 * }
 */
public class RandomDataSimulationDto extends FeedSimulationStreamConfiguration {
    /**
     * Time between two events
     */
    private int delay;

    /**
     * List of attribute configuration details of attributes of an input stream
     */
    private List<FeedSimulationStreamAttributeDto> FeedSimulationStreamAttributeDto = new ArrayList<>();

    /**
     * No of Events to be generated as random for simulation
     */

    private double events;

    public RandomDataSimulationDto() {
        super();
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public List<FeedSimulationStreamAttributeDto> getFeedSimulationStreamAttributeDto() {
        return FeedSimulationStreamAttributeDto;
    }

    public void setFeedSimulationStreamAttributeDto(List<FeedSimulationStreamAttributeDto> feedSimulationStreamAttributeDto) {
        this.FeedSimulationStreamAttributeDto = feedSimulationStreamAttributeDto;
    }

    public double getEvents() {
        return events;
    }

    public void setEvents(double events) {
        this.events = events;
    }


}
