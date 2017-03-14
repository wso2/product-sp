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

package org.wso2.eventsimulator.core.eventGenerator.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.eventsimulator.core.eventGenerator.EventGenerator;
import org.wso2.eventsimulator.core.eventGenerator.csvEventGeneration.CSVEventGenerator;
import org.wso2.eventsimulator.core.eventGenerator.databaseEventGeneration.DatabaseEventGenerator;
import org.wso2.eventsimulator.core.eventGenerator.randomEventGeneration.RandomEventGenerator;
import org.wso2.eventsimulator.core.internal.EventSimulatorDataHolder;
import org.wso2.eventsimulator.core.internal.ServiceComponent;
import org.wso2.eventsimulator.core.util.EventSimulatorConstants;
import scala.util.parsing.combinator.testing.Str;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * EventSimulator starts the simulation execution for single Event and
 * Feed Simulation
 */
public class EventSimulator implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(EventSimulator.class);
    private volatile boolean isPaused = false;
    private List<EventGenerator> simulators = new ArrayList<>();
    private Long delay;
    private Long timestampStartTime;
    private Long timeStampEndTime;
    private String uuid;
    private Boolean containsRandom = false;


    public EventSimulator(JSONObject simulationConfiguration) {
        uuid = UUID.randomUUID().toString();
        init(simulationConfiguration);
    }

    private void init(JSONObject simulationConfiguration) {

        JSONArray streamConfigurations = null;

        try {
            if (simulationConfiguration.has(EventSimulatorConstants.DELAY) && !simulationConfiguration.getString(EventSimulatorConstants.DELAY).isEmpty()) {
                delay = simulationConfiguration.getLong(EventSimulatorConstants.DELAY);
            } else {
                log.error("Delay is not specified. Delay is set to 0 milliseconds.");
                delay = 0L;
            }

            if (simulationConfiguration.has(EventSimulatorConstants.TIMESTAMP_START_TIME)
                    && !simulationConfiguration.getString(EventSimulatorConstants.TIMESTAMP_START_TIME).isEmpty()) {
                timestampStartTime = simulationConfiguration.getLong(EventSimulatorConstants.TIMESTAMP_START_TIME);
            } else {
                log.error("TimestampStartTime is required");
                throw new RuntimeException("TimestampStartTime is required");
            }

            if (simulationConfiguration.has(EventSimulatorConstants.TIMESTAMP_END_TIME)) {
                if (simulationConfiguration.isNull(EventSimulatorConstants.TIMESTAMP_END_TIME)) {
                    timeStampEndTime = null;
                } else if (!simulationConfiguration.getString(EventSimulatorConstants.TIMESTAMP_END_TIME).isEmpty()) {
                    timeStampEndTime = simulationConfiguration.getLong(EventSimulatorConstants.TIMESTAMP_END_TIME);
                } else {
                    log.error("TimestampEndTime is not specified. TimestampEndTime is set to null.");
                    timeStampEndTime = null;
                }
            } else {
                log.error("TimestampEndTime is not specified. TimestampEndTime is set to null.");
                timeStampEndTime = null;
            }

            if (simulationConfiguration.has(EventSimulatorConstants.FEED_SIMULATION_STREAM_CONFIGURATION)
                    && simulationConfiguration.getJSONArray(EventSimulatorConstants.FEED_SIMULATION_STREAM_CONFIGURATION).length() > 0) {
                streamConfigurations = simulationConfiguration.getJSONArray(EventSimulatorConstants.FEED_SIMULATION_STREAM_CONFIGURATION);
            } else {
                log.error("Stream configuration is required.");
                throw new RuntimeException("Stream configuration is required");
            }

            EventGenerator.SimulationType simulationType;

            for (int i = 0; i < streamConfigurations.length(); i++) {
                if (streamConfigurations.getJSONObject(i).has(EventSimulatorConstants.FEED_SIMULATION_TYPE)
                        && !streamConfigurations.getJSONObject(i).getString(EventSimulatorConstants.FEED_SIMULATION_TYPE).isEmpty()) {
                    simulationType = EventGenerator.SimulationType.valueOf(streamConfigurations.getJSONObject(i)
                            .getString(EventSimulatorConstants.FEED_SIMULATION_TYPE));

                    switch (simulationType) {
                        case FILE_SIMULATION:
                            CSVEventGenerator csvEventGenerator = new CSVEventGenerator();
                            csvEventGenerator.init(streamConfigurations.getJSONObject(i));
                            csvEventGenerator.initTimestamp(timestampStartTime, timeStampEndTime);
                            simulators.add(csvEventGenerator);
                            break;
                        case DATABASE_SIMULATION:
                            DatabaseEventGenerator databaseEventGenerator = new DatabaseEventGenerator();
                            databaseEventGenerator.init(streamConfigurations.getJSONObject(i));
                            databaseEventGenerator.initTimestamp(timestampStartTime, timeStampEndTime);
                            simulators.add(databaseEventGenerator);
                            break;
                        case RANDOM_DATA_SIMULATION:
                            containsRandom = true;
                            RandomEventGenerator randomEventGenerator = new RandomEventGenerator();
                            randomEventGenerator.init(streamConfigurations.getJSONObject(i));
                            randomEventGenerator.initTimestamp(timestampStartTime, timeStampEndTime);
                            simulators.add(randomEventGenerator);
                            break;
                    }
                } else {
                    log.error("Simulation type is not specified. Simulation type must be either '" + EventGenerator.SimulationType.FILE_SIMULATION +
                            "' or '" + EventGenerator.SimulationType.DATABASE_SIMULATION + "' or '" + EventGenerator.SimulationType.RANDOM_DATA_SIMULATION + "'.");
                    throw new RuntimeException("Simulation type is not specified. Simulation type must be either '"
                            + EventGenerator.SimulationType.FILE_SIMULATION + "' or '"
                            + EventGenerator.SimulationType.DATABASE_SIMULATION + "' or '" + EventGenerator.SimulationType.RANDOM_DATA_SIMULATION + "'.");
                }
            }

        } catch (IllegalArgumentException e) {
            log.error("Invalid simulation type. Simulation type must be either '" + EventGenerator.SimulationType.FILE_SIMULATION +
                    "' or '" + EventGenerator.SimulationType.DATABASE_SIMULATION + "' or '" + EventGenerator.SimulationType.RANDOM_DATA_SIMULATION + "'.");
            throw new RuntimeException("Invalid simulation type. Simulation type must be either '" + EventGenerator.SimulationType.FILE_SIMULATION + "' or '"
                    + EventGenerator.SimulationType.DATABASE_SIMULATION + "' or '" + EventGenerator.SimulationType.RANDOM_DATA_SIMULATION + "'.");
        } catch (JSONException e) {
            log.error("Error occurred when accessing stream configuration : " + e.getMessage());
            throw new RuntimeException("Error occurred when accessing stream configuration : " + e.getMessage());
        }
    }

    private  void eventSimulation() {
        Long minTimestamp;
        EventGenerator generator;
        try {
            while (true) {
                synchronized (this) {
                    while (isPaused) {
                        wait();
                    }
                }
                minTimestamp = -1L;
                generator = null;
                for (EventGenerator eventGenerator : simulators) {
                    if (eventGenerator.peek() != null){
                        if (minTimestamp == -1L) {
                            minTimestamp = eventGenerator.peek().getTimestamp();
                            generator = eventGenerator;
                        } else if (eventGenerator.peek().getTimestamp() < minTimestamp) {
                            minTimestamp = eventGenerator.peek().getTimestamp();
                            generator = eventGenerator;
                        }
                    }
                }
                if (minTimestamp >= 0L && generator != null) {
                    System.out.println("Input Event (" + uuid + ") : " + Arrays.deepToString(generator.peek().getData()));
                    EventSimulatorDataHolder.getInstance().getEventStreamService().pushEvent(generator.getExecutionPlanName(), generator.getStreamName(), generator.poll());
                } else {
                    break;
                }
                Thread.sleep(delay);
            }
            stop();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void run() {
        simulators.forEach(EventGenerator::start);
        log.info("Event generators started. Begin event simulation for uuid : " + uuid);
        eventSimulation();
    }

    public synchronized void stop() {
        try {
            simulators.forEach(EventGenerator::stop);
            ServiceComponent.simulatorMap.remove(uuid);
            log.info("Event simulation stopped for uuid : " + uuid);
        } catch (Exception e) {
            log.error("Error occurred when stopping simulation : " + e.getMessage());
        }
    }

    public synchronized void pause(){
        try {
            isPaused = true;
            log.info("Pause event simulation for uuid : " + uuid);
        } catch (Exception e) {
            log.error("Error occurred when pausing simulation : " + e.getMessage());
        }
    }

    public synchronized void resume(){
        try {
            isPaused = false;
            notify();
            log.info("Resume event simulation for uuid : " + uuid);
        } catch (Exception e) {
            log.error("Error occurred when resuming simulation : " + e.getMessage());
        }
    }

    public String getUuid() {
        return uuid;
    }
}
