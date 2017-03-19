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

package org.wso2.eventsimulator.core.eventGenerator;

import org.wso2.eventsimulator.core.eventGenerator.bean.StreamConfigurationDto;
import org.wso2.siddhi.core.event.Event;

/**
 * Interface which defines the common methods used by all event generators
 */
public interface EventGenerator {

    void init(StreamConfigurationDto streamConfiguration);

    void start();

    void stop();

    Event poll();

    Event peek();

    void getNextEvent();

    void initTimestamp(Long timestampStartTime, Long timestampEndTime);

    String getStreamName();

    String getExecutionPlanName();

    /**
     * Generator type for an input stream can be
     * 1. RandomDataSimulation
     * 2. FileFeedSimulation
     * 3. DatabaseSimulation
     * 4. SingleEventSimulation
     */
    enum GeneratorType {
        FILE_SIMULATION, RANDOM_DATA_SIMULATION, DATABASE_SIMULATION
    }

}
