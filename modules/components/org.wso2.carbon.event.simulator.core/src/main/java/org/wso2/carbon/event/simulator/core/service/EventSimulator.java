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

package org.wso2.carbon.event.simulator.core.service;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.event.simulator.core.exception.InsufficientAttributesException;
import org.wso2.carbon.event.simulator.core.exception.InvalidConfigException;
import org.wso2.carbon.event.simulator.core.internal.bean.SimulationPropertiesDTO;
import org.wso2.carbon.event.simulator.core.internal.generator.EventGenerator;
import org.wso2.carbon.event.simulator.core.internal.util.EventGeneratorFactoryImpl;
import org.wso2.carbon.event.simulator.core.internal.util.EventSimulatorConstants;

import static org.wso2.carbon.event.simulator.core.internal.util.CommonOperations.checkAvailability;
import static org.wso2.carbon.event.simulator.core.internal.util.CommonOperations.checkAvailabilityOfArray;

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
    private SimulationPropertiesDTO simulationProperties;
    private String uuid;


    /**
     * EventSimulator() constructor initializes an EventSimulator object and assigns it an UUID
     *
     * @param simulationConfiguration a string containing the simulation configuration
     * @throws InsufficientAttributesException is a configuration does not produce data for all stream attributes
     * @throws InvalidConfigException          if the simulation configuration is invalid
     */
    public EventSimulator(String simulationConfiguration)
            throws InsufficientAttributesException, InvalidConfigException {
        uuid = UUID.randomUUID().toString();
        JSONObject simulationConfig = new JSONObject(simulationConfiguration);
//        first create a simulation properties object
        if (simulationConfig.has(EventSimulatorConstants.EVENT_SIMULATION_PROPERTIES)
                && !simulationConfig.isNull(EventSimulatorConstants.EVENT_SIMULATION_PROPERTIES)) {
            simulationProperties = validateSimulationProperties(simulationConfig
                    .getJSONObject(EventSimulatorConstants.EVENT_SIMULATION_PROPERTIES));
//            check whether the simulation has source configurations and create event generators for each source config
            if (checkAvailabilityOfArray(simulationConfig,
                    EventSimulatorConstants.EVENT_SIMULATION_SOURCES)) {
                JSONArray streamConfig = simulationConfig.getJSONArray(EventSimulatorConstants
                        .EVENT_SIMULATION_SOURCES);
                EventGeneratorFactoryImpl generatorFactory = new EventGeneratorFactoryImpl();
                for (int i = 0; i < streamConfig.length(); i++) {
                    generators.add(generatorFactory.getEventGenerator(streamConfig.getJSONObject(i),
                            simulationProperties.getTimestampStartTime(), simulationProperties.getTimestampEndTime()));
                }
            } else {
                throw new InvalidConfigException("Source configuration is required for event simulation '" +
                        simulationProperties.getSimulationName() + "'. Invalid simulation configuration provided : " +
                        simulationConfig.toString());
            }
        } else {
            throw new InvalidConfigException("Simulation properties are required for event simulation. Invalid " +
                    "simulation configuration provided : " + simulationConfig.toString());
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
        int eventsRemaining = simulationProperties.getNoOfEventsRequired();
        try {
            while (true) {
//                if the simulator is paused, wait till it is resumed
                synchronized (this) {
                    while (isPaused) {
                        wait();
                    }
                }
                /*
                 * if there is no limit to the number of events to be sent or is the number of event remaining to be
                 * sent is > 0, send an event, else stop event simulation
                 * */
                if (eventsRemaining == -1 ||  eventsRemaining > 0) {
                    minTimestamp = -1L;
                    generator = null;
                    /*
                     * 1. for each event generator peek the next event (i.e. the next event with least timestamp)
                     * 2. take the first event generator will a not null nextEvent as the first refferal value for
                     * generator with minimum timestamp event, and take the events timestamp as the minimum timestamp
                      * refferal value
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
                        log.info("Input Event (" + uuid + ") : "
                                + Arrays.deepToString(generator.peek().getData()));
                        EventSimulatorDataHolder.getInstance().getEventStreamService()
                                .pushEvent(generator.getExecutionPlanName(), generator.getStreamName(),
                                        generator.poll());
                    } else {
                        break;
                    }
                    if (eventsRemaining > 0) {
                        eventsRemaining--;
                    }
                    Thread.sleep(simulationProperties.getTimeInterval());
                } else {
                    break;
                }
            }
            stop();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * validateSimulationConfiguration() is used to parse the simulation configuration
     *
     * @param simulationPropertiesConfig a JSON object containing simulation properties
     * @return SimulationPropertiesDTO object containing simulation properties
     * @throws InvalidConfigException if the simulation configuration contains invalid data
     */
    private SimulationPropertiesDTO validateSimulationProperties(JSONObject simulationPropertiesConfig)
            throws InvalidConfigException {
        /*
         * checkAvailability() method performs the following checks
         * 1. has
         * 2. isNull
         * 3. isEmpty
         * */
        try {
            if (!checkAvailability(simulationPropertiesConfig, EventSimulatorConstants.EVENT_SIMULATION_NAME)) {
                throw new InvalidConfigException("Simulation name is required for event simulation. Invalid " +
                        "simulation properties configuration provided : " + simulationPropertiesConfig.toString());
            }
            if (!checkAvailability(simulationPropertiesConfig, EventSimulatorConstants.SIMULATION_TIME_INTERVAL)) {
                throw new InvalidConfigException("Time interval is required for simulation '" +
                        simulationPropertiesConfig.getString(EventSimulatorConstants.EVENT_SIMULATION_NAME) +
                        "'. Invalid simulation properties configuration provided : "
                        + simulationPropertiesConfig.toString());
            }
            /*
             * a simulation must have the timestampStartTime specified or set to null
             * if timestampStartTime is set to null it implies that there is no restriction on the least possible
             * timestamp value that an event can have.
             * if null, set timestampStartTime to -1
             * else if timestampStartTime is specified, use that value as least possible timestamp value
             * else throw an exception
             * */
            long timestampStartTime;
            if (simulationPropertiesConfig.has(EventSimulatorConstants.TIMESTAMP_START_TIME)) {
                if (simulationPropertiesConfig.isNull(EventSimulatorConstants.TIMESTAMP_START_TIME)) {
                    timestampStartTime = -1;
                } else if (!simulationPropertiesConfig.getString(EventSimulatorConstants.TIMESTAMP_START_TIME).isEmpty
                        ()) {
                    timestampStartTime = simulationPropertiesConfig
                            .getLong(EventSimulatorConstants.TIMESTAMP_START_TIME);
                } else {
                    throw new InvalidConfigException("TimestampStartTime is required for simulation '" +
                            simulationPropertiesConfig.getString(EventSimulatorConstants.EVENT_SIMULATION_NAME)
                            + "'. Invalid simulation properties configuration provided : "
                            + simulationPropertiesConfig.toString());
                }
            } else {
                throw new InvalidConfigException("TimestampStartTime is required for simulation '" +
                        simulationPropertiesConfig.getString(EventSimulatorConstants.EVENT_SIMULATION_NAME)
                        + "'. Invalid simulation properties configuration provided : " + simulationPropertiesConfig
                        .toString());
            }
            /*
             * a simulation must either contain a timestampEndTime(maximum possible timestamp) or the number of events
             * that needed to be produced.
             * check whether the simulation has timestampEndTime, if so it must either be null or a non-empty value.
             * if the simulation properties configuration had timestampEndtime but it is not specified or set to null
             * throw an exception
             * else if timestampEndTime is null set timestampEndTime property as -1. it implies that there is no bound
             * for maximum timestamp possible for an event.
             * else if timestampEndTime is specified, use that as the maximum possible timestamp value
             * next check whether the number of events to be produced is specified
             * simulation properties configuration must have property numberOfEventRequired specified
             * or set to null
             * if not throw an exception
             * if number of events required == null, there is no limit to the number of events to be produced. set
             * property numberOfEventsRequired to -1
             * else, set the number of events required property to that specified in simulation properties config
             * */
            long timestampEndTime;
            int noOfEventsRequired;
            if (simulationPropertiesConfig.has(EventSimulatorConstants.TIMESTAMP_END_TIME)) {
                if (simulationPropertiesConfig.isNull(EventSimulatorConstants.TIMESTAMP_END_TIME)) {
                    timestampEndTime = -1;
                } else if (!simulationPropertiesConfig.getString(EventSimulatorConstants.TIMESTAMP_END_TIME)
                        .isEmpty()) {
                    timestampEndTime = simulationPropertiesConfig.getLong(EventSimulatorConstants.TIMESTAMP_END_TIME);
                } else {
                    throw new InvalidConfigException("TimestampEndTime is required for simulation '" +
                            simulationPropertiesConfig.getString(EventSimulatorConstants.EVENT_SIMULATION_NAME) +
                            "'. TimestampEndTime must be either specified or set to null. Invalid simulation " +
                            "properties configuration provided : " + simulationPropertiesConfig.toString());
                }
            } else {
                throw new InvalidConfigException("TimestampEndTime is required for simulation '" +
                        simulationPropertiesConfig.getString(EventSimulatorConstants.EVENT_SIMULATION_NAME) +
                        "'. TimestampEndTime must be either specified or set to null. Invalid simulation " +
                        "properties configuration provided : " + simulationPropertiesConfig.toString());
            }
            if (simulationPropertiesConfig.has(EventSimulatorConstants.NUMBER_OF_EVENTS_REQUIRED)) {
                if (simulationPropertiesConfig.isNull(EventSimulatorConstants.NUMBER_OF_EVENTS_REQUIRED)) {
                    noOfEventsRequired = -1;
                } else if (!simulationPropertiesConfig.getString(EventSimulatorConstants.NUMBER_OF_EVENTS_REQUIRED)
                        .isEmpty()) {
                    noOfEventsRequired = simulationPropertiesConfig
                            .getInt(EventSimulatorConstants.NUMBER_OF_EVENTS_REQUIRED);
                } else {
                    throw new InvalidConfigException("Number of event to be generated for simulation '" +
                            simulationPropertiesConfig.getString(EventSimulatorConstants.EVENT_SIMULATION_NAME) + "' " +
                            "must be either specified or set to null. Invalid simulation  configuration provided : " +
                            simulationPropertiesConfig.toString());
                }
            } else {
                throw new InvalidConfigException("Number of event to be generated for simulation '" +
                        simulationPropertiesConfig.getString(EventSimulatorConstants.EVENT_SIMULATION_NAME) +
                        "' must be either specified or set to null. Invalid simulation properties configuration " +
                        "provided : " + simulationPropertiesConfig.toString());
            }
//            create simulationPropertiesDTO object
            SimulationPropertiesDTO simulationPropertiesDTO = new SimulationPropertiesDTO();
            simulationPropertiesDTO.setSimulationName(simulationPropertiesConfig
                    .getString(EventSimulatorConstants.EVENT_SIMULATION_NAME));
            simulationPropertiesDTO.setTimeInterval(simulationPropertiesConfig
                    .getLong(EventSimulatorConstants.SIMULATION_TIME_INTERVAL));
            simulationPropertiesDTO.setTimestampStartTime(timestampStartTime);
            simulationPropertiesDTO.setTimestampEndTime(timestampEndTime);
            simulationPropertiesDTO.setNoOfEventsRequired(noOfEventsRequired);
            return simulationPropertiesDTO;
        } catch (JSONException e) {
            log.error("Error occurred when accessing simulation configuration for simulation '" +
                    simulationPropertiesConfig.getString(EventSimulatorConstants.EVENT_SIMULATION_NAME) + "'. Invalid" +
                    " simulation properties configuration provided : " + simulationPropertiesConfig.toString() + ". " +
                    "Error : " + e.getMessage());
            throw new InvalidConfigException("Error occurred when accessing simulation configuration for simulation '" +
                    simulationPropertiesConfig.getString(EventSimulatorConstants.EVENT_SIMULATION_NAME) + "'. Invalid" +
                    " simulation properties configuration provided : " + simulationPropertiesConfig.toString() + ". " +
                    "Error  : " + e.getMessage(), e);
        }
    }


    /**
     * run() method of runnable associated with the event simulator
     * This method starts all the event generators belonging to the simulations and begins the event simulation
     */
    @Override
    public void run() {
        generators.forEach(EventGenerator::start);

        if (log.isDebugEnabled()) {
            log.debug("Event generators started. Begin event simulation for uuid : " + uuid);
        }
        eventSimulation();
    }
// todo catch exception and stop everything

    /**
     * stop() is used to stop event simulation
     *
     * @see ServiceComponent#stop(String)
     * @see EventGenerator#stop()
     */
    public synchronized void stop() {
        generators.forEach(EventGenerator::stop);
        ServiceComponent.SIMULATOR_MAP.remove(uuid);

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
        notifyAll();
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
