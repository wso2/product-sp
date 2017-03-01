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

package org.wso2.eventsimulator.core.simulator;


import org.wso2.eventsimulator.core.simulator.bean.FeedSimulationStreamConfiguration;
import org.wso2.eventsimulator.core.simulator.csvFeedSimulation.core.CSVFeedEventSimulator;
import org.wso2.eventsimulator.core.simulator.databaseFeedSimulation.core.DatabaseFeedSimulator;
import org.wso2.eventsimulator.core.simulator.randomdatafeedsimulation.core.RandomDataEventSimulator;
import org.wso2.eventsimulator.core.simulator.singleventsimulator.SingleEventSimulator;

/**
 * EventSimulator is an Interface class which provides core functions of simulation process such as
 * send, Pause, stop, resume
 *
 * @see SingleEventSimulator
 * @see RandomDataEventSimulator
 * @see CSVFeedEventSimulator
 * @see DatabaseFeedSimulator
 */
public interface EventSimulator extends Runnable {

    public void stop();

    public void pause();

    public void resume();

    public FeedSimulationStreamConfiguration getStreamConfiguration();

}
