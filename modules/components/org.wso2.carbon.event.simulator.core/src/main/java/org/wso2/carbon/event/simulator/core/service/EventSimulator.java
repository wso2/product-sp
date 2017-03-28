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
import org.wso2.carbon.event.simulator.core.internal.generator.csv.core.CSVEventGenerator;
import org.wso2.carbon.event.simulator.core.internal.generator.database.core.DatabaseEventGenerator;
import org.wso2.carbon.event.simulator.core.internal.generator.random.core.RandomEventGenerator;
import org.wso2.carbon.event.simulator.core.internal.util.EventSimulatorConstants;

import static org.wso2.carbon.event.simulator.core.internal.util.CommonOperations.checkAvailability;
import static org.wso2.carbon.event.simulator.core.internal.util.CommonOperations.checkAvailabilityOfArray;
import static org.wso2.carbon.event.simulator.core.internal.util.CommonOperations.checkAvailabilityOfObject;

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
     */
    public EventSimulator() {
        uuid = UUID.randomUUID().toString();
    }


    /**
     * init()  method is used to initialize the event simulator and the generators needed for simulation
     *
     * @param simulationConfiguration a string containing the simulation configuration
     * @throws InsufficientAttributesException is a configuration does not produce data for all stream attributes
     */
//    todo do in constructor
    public void init(String simulationConfiguration) throws InsufficientAttributesException, InvalidConfigException {

        JSONObject simulationConfig = new JSONObject(simulationConfiguration);

//        first create a simulation properties object
        if (checkAvailabilityOfObject(simulationConfig, EventSimulatorConstants.EVENT_SIMULATION_PROPERTIES)) {
            simulationProperties = validateSimulationProperties(simulationConfig
                    .getJSONObject(EventSimulatorConstants.EVENT_SIMULATION_PROPERTIES));
//            create generators for each stream configuration
            if (checkAvailabilityOfArray(simulationConfig,
                    EventSimulatorConstants.EVENT_SIMULATION_STREAM_CONFIGURATION)) {

                JSONArray streamConfig = simulationConfig.getJSONArray(EventSimulatorConstants
                        .EVENT_SIMULATION_STREAM_CONFIGURATION);

                for (int i = 0; i < streamConfig.length(); i++) {
//                    retrieve generator type to be used for stream configuration
                    EventGenerator.GeneratorType generatorType = getGeneratorType(streamConfig.getJSONObject(i));
                /*
                    * Actions performed when initializing generators
                    * 1. create a generator object
                    * 2. initialize the generators
                    * 3. initialize the timestamp range
                    * */
//                todo abstract factory
//                    todo pass to constructor
                    switch (generatorType) {
                        case FILE_SIMULATION:
                            CSVEventGenerator csvEventGenerator = new CSVEventGenerator();
                            csvEventGenerator.initTimestamp(simulationProperties.getTimestampStartTime(),
                                    simulationProperties.getTimestampEndTime());
                            csvEventGenerator.init(streamConfig.getJSONObject(i));
                            generators.add(csvEventGenerator);
                            break;
                        case DATABASE_SIMULATION:
                            DatabaseEventGenerator databaseEventGenerator = new DatabaseEventGenerator();
                            databaseEventGenerator.initTimestamp(simulationProperties.getTimestampStartTime(),
                                    simulationProperties.getTimestampEndTime());
                            databaseEventGenerator.init(streamConfig.getJSONObject(i));
                            generators.add(databaseEventGenerator);
                            break;
                        case RANDOM_DATA_SIMULATION:
                            RandomEventGenerator randomEventGenerator = new RandomEventGenerator();
                            randomEventGenerator.initTimestamp(simulationProperties.getTimestampStartTime(),
                                    simulationProperties.getTimestampEndTime());
                            randomEventGenerator.init(streamConfig.getJSONObject(i));
                            generators.add(randomEventGenerator);
                            break;
                    }
                }
            } else {
                throw new InvalidConfigException("Stream configuration is required for event simulation '" +
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
                    log.info("Input Event (" + uuid + ") : "
                            + Arrays.deepToString(generator.peek().getData()));
                    EventSimulatorDataHolder.getInstance().getEventStreamService()
                            .pushEvent(generator.getExecutionPlanName(), generator.getStreamName(),
                                    generator.poll());
                } else {
                    break;
                }
                Thread.sleep(simulationProperties.getTimeInterval());
            }
            stop();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * validateSimulationConfiguration() is used to parse the simulation configuration
     *
     * @param simulationPropertiesString a JSON object containing simulation properties
     * @return SimulationPropertiesDTO object containing simulation properties
     * @throws InvalidConfigException if the simulation configuration contains invalid data
     */
    private SimulationPropertiesDTO validateSimulationProperties(JSONObject simulationPropertiesString)
            throws InvalidConfigException {

        /*
        * checkAvailability() method performs the following checks
        * 1. has
        * 2. isNull
        * 3. isEmpty
        * */
        try {
            if (!checkAvailability(simulationPropertiesString, EventSimulatorConstants.EVENT_SIMULATION_NAME)) {
                throw new InvalidConfigException("Simulation name is required for event simulation. Invalid " +
                        "simulation configuration provided : " + simulationPropertiesString.toString());
            }

            if (!checkAvailability(simulationPropertiesString, EventSimulatorConstants.SIMULATION_TIME_INTERVAL)) {
                throw new InvalidConfigException("Time interval is required for simulation '" +
                        simulationPropertiesString.getString(EventSimulatorConstants.EVENT_SIMULATION_NAME) +
                        "'. Invalid simulation configuration provided : " + simulationPropertiesString.toString());
            }

            if (!checkAvailability(simulationPropertiesString, EventSimulatorConstants.TIMESTAMP_START_TIME)) {
                throw new InvalidConfigException("TimestampStartTime is required for simulation '" +
                        simulationPropertiesString.getString(EventSimulatorConstants.EVENT_SIMULATION_NAME)
                        + "'. Invalid simulation configuration provided : " + simulationPropertiesString.toString());
            }

            /*
            * a simulation must either contain a timestampEndTime(maximum possible timestamp) or the number of events
            * that needed to be produced.
            * check whether the simulation has timestampEndTime, if so it must either be null or a non-empty value.
            * if yes, set property 'timestampEndTime'
            * if not, throw an exception
            * if timestampEndTime is not specified check whether the number of events to be produced is specified
            * if yes, set property 'noOfEventRequired'
            * else throw an exception
            * */
            long timestampEndTime = -1;
            int noOfEventsRequired = -1;

            if (simulationPropertiesString.has(EventSimulatorConstants.TIMESTAMP_END_TIME)) {
                if (simulationPropertiesString.isNull(EventSimulatorConstants.TIMESTAMP_END_TIME)) {
                    timestampEndTime = -1;
                } else if (!simulationPropertiesString
                        .getString(EventSimulatorConstants.TIMESTAMP_END_TIME).isEmpty()) {
                    timestampEndTime = simulationPropertiesString.getLong(EventSimulatorConstants.TIMESTAMP_END_TIME);
                } else {
                    throw new InvalidConfigException("TimestampEndTime is required for simulation '" +
                            simulationPropertiesString.getString(EventSimulatorConstants.EVENT_SIMULATION_NAME) +
                            "'. TimestampEndTime must be either specified or set to null. Invalid simulation " +
                            "configuration provided : " + simulationPropertiesString.toString());
                }
            } else if (checkAvailability(simulationPropertiesString,
                    EventSimulatorConstants.NUMBER_OF_EVENTS_REQUIRED)) {
                noOfEventsRequired = simulationPropertiesString
                        .getInt(EventSimulatorConstants.NUMBER_OF_EVENTS_REQUIRED);
            } else {
                throw new InvalidConfigException("Either TimestampEndTime or the number of event required to be " +
                        "produced must be specified for simulation '" +
                        simulationPropertiesString.getString(EventSimulatorConstants.EVENT_SIMULATION_NAME)
                        + "'. Invalid simulation configuration provided : " + simulationPropertiesString.toString());
            }


            SimulationPropertiesDTO simulationPropertiesDTO = new SimulationPropertiesDTO();
            simulationPropertiesDTO.setSimulationName(simulationPropertiesString
                    .getString(EventSimulatorConstants.EVENT_SIMULATION_NAME));
            simulationPropertiesDTO.setTimeInterval(simulationPropertiesString
                    .getLong(EventSimulatorConstants.SIMULATION_TIME_INTERVAL));
            simulationPropertiesDTO.setTimestampStartTime(
                    simulationPropertiesString.getLong(EventSimulatorConstants.TIMESTAMP_START_TIME));
            simulationPropertiesDTO.setTimestampEndTime(timestampEndTime);
            simulationPropertiesDTO.setNoOfEventsRequired(noOfEventsRequired);

            return simulationPropertiesDTO;

        } catch (JSONException e) {
            log.error("Error occurred when accessing simulation configuration for simulation '" +
                    simulationPropertiesString.getString(EventSimulatorConstants.EVENT_SIMULATION_NAME) + "' : "
                    + e.getMessage());
            throw new InvalidConfigException("Error occurred when accessing simulation configuration for simulation '" +
                    simulationPropertiesString.getString(EventSimulatorConstants.EVENT_SIMULATION_NAME) + "' : "
                    + e.getMessage(), e);
        }
    }


    /**
     * getGeneratorType() is used to retrieve the generator type of a stream configuration
     *
     * @param streamConfiguration JSON object containing stream configuration
     * @return generator type to be used for stream configuration
     * @throws InvalidConfigException if the simulation type is not specified or if an invalid generator type is
     *                                specified
     */
    private EventGenerator.GeneratorType getGeneratorType(JSONObject streamConfiguration)
            throws InvalidConfigException {
        /*
        * check whether the stream configuration has a simulation type specified
        * if the generator type is either DB, CSV, or Random return value
        * else throw an exception
        * */
        if (checkAvailability(streamConfiguration, EventSimulatorConstants.EVENT_SIMULATION_TYPE)) {

            try {
                return EventGenerator.GeneratorType.valueOf(streamConfiguration.
                        getString(EventSimulatorConstants.EVENT_SIMULATION_TYPE));
            } catch (IllegalArgumentException e) {
                throw new InvalidConfigException("Invalid simulation type provided for a stream configuration in " +
                        "simulation '" + simulationProperties.getSimulationName() + "'. Simulation type must be " +
                        "either '" + EventGenerator.GeneratorType.FILE_SIMULATION + "' or '" +
                        EventGenerator.GeneratorType.DATABASE_SIMULATION + "' or '" +
                        EventGenerator.GeneratorType.RANDOM_DATA_SIMULATION + "'. Invalid stream configuration " +
                        "provided : " + streamConfiguration.toString());
            }
        } else {
            throw new InvalidConfigException("Simulation type is not specified for a stream configuration in " +
                    "simulation '" + simulationProperties.getSimulationName() + "'. Simulation type must" +
                    " be either '" + EventGenerator.GeneratorType.FILE_SIMULATION + "' or '" +
                    EventGenerator.GeneratorType.DATABASE_SIMULATION + "' or '" +
                    EventGenerator.GeneratorType.RANDOM_DATA_SIMULATION + "'. Invalid stream configuration " +
                    "provided : " + streamConfiguration.toString());
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
//catch exception and stop everything

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
