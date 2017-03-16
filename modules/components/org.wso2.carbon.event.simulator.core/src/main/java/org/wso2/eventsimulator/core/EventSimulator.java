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

package org.wso2.eventsimulator.core;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.eventsimulator.core.eventGenerator.EventGenerator;
import org.wso2.eventsimulator.core.eventGenerator.csvEventGeneration.core.CSVEventGenerator;
import org.wso2.eventsimulator.core.eventGenerator.databaseEventGeneration.core.DatabaseEventGenerator;
import org.wso2.eventsimulator.core.eventGenerator.randomEventGeneration.core.RandomEventGenerator;
import org.wso2.eventsimulator.core.eventGenerator.util.constants.EventSimulatorConstants;
import org.wso2.eventsimulator.core.eventGenerator.util.exceptions.ConfigurationParserException;
import org.wso2.eventsimulator.core.eventGenerator.util.exceptions.EventGenerationException;
import org.wso2.eventsimulator.core.internal.EventSimulatorDataHolder;
import org.wso2.eventsimulator.core.internal.ServiceComponent;

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
    private List<EventGenerator> generators = new ArrayList<>();
    private Long delay;
    private Long timestampStartTime;
    private Long timeStampEndTime;
    private String uuid;
    private Boolean containsRandom = false;


    /**
     * EventSimulator() constructor initializes an EventSimulator object and assigns it an UUID
     *
     * @param simulationConfiguration a JSONObject containing the simulation configuration
     */
    public EventSimulator(JSONObject simulationConfiguration) {
        uuid = UUID.randomUUID().toString();
        init(simulationConfiguration);
    }


    /**
     * init()  method is used to initialize the event simulator and the generators needed for simulation
     *
     * @param simulationConfiguration JSON object containing simulation configuration
     */
    private void init(JSONObject simulationConfiguration) {

        try {
            /*
            * assign values to properties of event simulator.
            *
            * prior to assigning perform following checks
            *
            * 1. has
            * 2. isNull
            * 3. isEmpty
            *
            * if checks are successful, assign value to property.
            * else, throw an exception specifying which property is missing*/

            if (simulationConfiguration.has(EventSimulatorConstants.DELAY)
                    && !simulationConfiguration.isNull(EventSimulatorConstants.DELAY)
                    && !simulationConfiguration.getString(EventSimulatorConstants.DELAY).isEmpty()) {

                delay = simulationConfiguration.getLong(EventSimulatorConstants.DELAY);
            } else {
                throw new ConfigurationParserException("Delay is not specified.");
            }

            if (simulationConfiguration.has(EventSimulatorConstants.TIMESTAMP_START_TIME)
                    && !simulationConfiguration.isNull(EventSimulatorConstants.TIMESTAMP_START_TIME)
                    && !simulationConfiguration.getString(EventSimulatorConstants.TIMESTAMP_START_TIME).isEmpty()) {

                timestampStartTime = simulationConfiguration.getLong(EventSimulatorConstants.TIMESTAMP_START_TIME);
            } else {
                throw new ConfigurationParserException("TimestampStartTime is required");
            }

//            if timestampEndTime is not specified assign null to it
            if (simulationConfiguration.has(EventSimulatorConstants.TIMESTAMP_END_TIME)) {
                if (simulationConfiguration.isNull(EventSimulatorConstants.TIMESTAMP_END_TIME)) {
                    timeStampEndTime = null;
                } else if (!simulationConfiguration.getString(EventSimulatorConstants.TIMESTAMP_END_TIME).isEmpty()) {
                    timeStampEndTime = simulationConfiguration.getLong(EventSimulatorConstants.TIMESTAMP_END_TIME);
                } else {
                    throw new ConfigurationParserException("TimestampEndTime is not specified.");
                }
            } else {
                throw new ConfigurationParserException("TimestampEndTime is not specified.");
            }

            JSONArray streamConfigurations;
            if (simulationConfiguration.has(EventSimulatorConstants.FEED_SIMULATION_STREAM_CONFIGURATION)
                    && !simulationConfiguration.isNull(EventSimulatorConstants.FEED_SIMULATION_STREAM_CONFIGURATION)
                    && simulationConfiguration
                    .getJSONArray(EventSimulatorConstants.FEED_SIMULATION_STREAM_CONFIGURATION).length() > 0) {

                streamConfigurations = simulationConfiguration
                        .getJSONArray(EventSimulatorConstants.FEED_SIMULATION_STREAM_CONFIGURATION);
            } else {
                throw new ConfigurationParserException("Stream configuration is required");
            }

            EventGenerator.GeneratorType simulationType;

            for (int i = 0; i < streamConfigurations.length(); i++) {
                if (streamConfigurations.getJSONObject(i).has(EventSimulatorConstants.FEED_SIMULATION_TYPE)
                        && !streamConfigurations.getJSONObject(i).isNull(EventSimulatorConstants.FEED_SIMULATION_TYPE)
                        && !streamConfigurations.getJSONObject(i)
                        .getString(EventSimulatorConstants.FEED_SIMULATION_TYPE).isEmpty()) {

                    /*
                    * for each stream configuration retrieve the simulation type.
                    * Switch by the simulation type to determine which type of generator needs to be initiated
                    * */
                    try {
                        simulationType = EventGenerator.GeneratorType.valueOf(streamConfigurations.getJSONObject(i)
                                .getString(EventSimulatorConstants.FEED_SIMULATION_TYPE));
                    } catch (IllegalArgumentException e) {
                        throw new ConfigurationParserException("Invalid simulation type. Simulation type must be " +
                                "either '" + EventGenerator.GeneratorType.FILE_SIMULATION + "' or '" +
                                EventGenerator.GeneratorType.DATABASE_SIMULATION + "' or '" +
                                EventGenerator.GeneratorType.RANDOM_DATA_SIMULATION + "'.");
                    }

                    /*
                    * Actions performed when initializing generators
                    * 1. create a generator object
                    * 2. initialize the generators
                    * 2. initialize the timestamp range
                    * */
                    switch (simulationType) {
                        case FILE_SIMULATION:
                            CSVEventGenerator csvEventGenerator = new CSVEventGenerator();
                            csvEventGenerator.initTimestamp(timestampStartTime, timeStampEndTime);
                            csvEventGenerator.init(streamConfigurations.getJSONObject(i));
                            generators.add(csvEventGenerator);
                            break;
                        case DATABASE_SIMULATION:
                            DatabaseEventGenerator databaseEventGenerator = new DatabaseEventGenerator();
                            databaseEventGenerator.initTimestamp(timestampStartTime, timeStampEndTime);
                            databaseEventGenerator.init(streamConfigurations.getJSONObject(i));
                            generators.add(databaseEventGenerator);
                            break;
                        case RANDOM_DATA_SIMULATION:
                            containsRandom = true;
                            RandomEventGenerator randomEventGenerator = new RandomEventGenerator();
                            randomEventGenerator.initTimestamp(timestampStartTime, timeStampEndTime);
                            randomEventGenerator.init(streamConfigurations.getJSONObject(i));
                            generators.add(randomEventGenerator);
                            break;
                    }
                } else {
                    throw new ConfigurationParserException("Simulation type is not specified. Simulation type must" +
                            " be either '" + EventGenerator.GeneratorType.FILE_SIMULATION + "' or '" +
                            EventGenerator.GeneratorType.DATABASE_SIMULATION + "' or '" +
                            EventGenerator.GeneratorType.RANDOM_DATA_SIMULATION + "'.");
                }
            }

        } catch (JSONException e) {
            log.error("Error occurred when accessing stream configuration : " , e);
        } catch (ConfigurationParserException e) {
            log.error("Error occurred when parsing simulation configuration : " , e);
        } catch (EventGenerationException e) {
            log.error("Error occurred when generating an event : " , e);
        }
    }


    /**
     * eventSimulation() method is responsible for sending events belonging to one simulation configuration in the
     * order of their timestamps
     * Events will be sent at time intervals equal to the delay
     */
    private void eventSimulation() {
        Long minTimestamp;
        EventGenerator generator;
        try {
            while (true) {
//                if the simulator is paused, wait till it is resumed
                synchronized (this) {
                    while (isPaused) {
                        wait();
                    }
                }
                minTimestamp = -1L;
                generator = null;

                /*
                * 1. for each event generator peek the next event (i.e. the next event with least timestamp)
                * 2. take the first event generator will a not null nextEvent as the first refferal value for generator
                * with minimum timestamp event, and take the events timestamp as the minimum timestamp refferal value
                * 3. then compare the timestamp of the remaining not null nextEvents with the minimum timestamp and
                * update the minimum timestamp refferal value accordingly.
                * 4. once all generators are iterated and the event with minimum timestamp if obtained, send event.
                * 5. if all generators has nextEvent == null, then stop event simulation
                * */
                for (EventGenerator eventGenerator : generators) {
                    if (eventGenerator.peek() != null) {
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
                    System.out.println("Input Event (" + uuid + ") : "
                            + Arrays.deepToString(generator.peek().getData()));
                    EventSimulatorDataHolder.getInstance().getEventStreamService()
                            .pushEvent(generator.getExecutionPlanName(), generator.getStreamName(), generator.poll());
                } else {
                    break;
                }
                Thread.sleep(delay);
            }
            stop();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (EventGenerationException e) {
            log.error("Error occurred when generating an event : " , e);
        }
    }

    /**
     * run() method of runnable associated with the event simulator
     * This method starts all the event generators belonging to the simulations and begins the event simulation
     */
    @Override
    public void run() {
        try {
            generators.forEach(EventGenerator::start);

            if (log.isDebugEnabled()) {
                log.debug("Event generators started. Begin event simulation for uuid : " + uuid);
            }

            eventSimulation();
        } catch (EventGenerationException e) {
            log.error("Error occurred when generating an event : " , e);
        }
    }


    /**
     * stop() is used to stop event simulation
     *
     * @see ServiceComponent#stop(String)
     * @see EventGenerator#stop()
     */
    public synchronized void stop() {
        generators.forEach(EventGenerator::stop);
        ServiceComponent.simulatorMap.remove(uuid);

        if (log.isDebugEnabled()) {
            log.debug("Stop event simulation for uuid : " + uuid);
        }
    }


    /**
     * pause() is used to pause event simulation
     *
     * @see ServiceComponent#pause(String)
     */
    public synchronized void pause() {
        isPaused = true;

        if (log.isDebugEnabled()) {
            log.debug("Pause event simulation for uuid : " + uuid);
        }
    }


    /**
     * resume() is used to resume event simulation
     *
     * @see ServiceComponent#resume(String)
     */
    public synchronized void resume() {
        isPaused = false;
        notify();
        if (log.isDebugEnabled()) {
            log.debug("Resume event simulation for uuid : " + uuid);
        }
    }


    /**
     * getUuid() is used to retrieve the UUID of the event simulator when be adding the simulator to the
     * EventSimulatorMap in ServiceComponent
     *
     * @return uuid of event simulator
     * @see ServiceComponent#stop()
     */
    public String getUuid() {
        return uuid;
    }
}
