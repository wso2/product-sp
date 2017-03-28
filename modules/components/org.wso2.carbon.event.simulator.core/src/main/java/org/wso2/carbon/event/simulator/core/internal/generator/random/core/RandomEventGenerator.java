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

package org.wso2.carbon.event.simulator.core.internal.generator.random.core;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.event.simulator.core.exception.EventGenerationException;
import org.wso2.carbon.event.simulator.core.exception.InsufficientAttributesException;
import org.wso2.carbon.event.simulator.core.exception.InvalidConfigException;
import org.wso2.carbon.event.simulator.core.exception.SimulatorInitializationException;
import org.wso2.carbon.event.simulator.core.internal.bean.RandomAttributeDTO;
import org.wso2.carbon.event.simulator.core.internal.bean.RandomSimulationDTO;
import org.wso2.carbon.event.simulator.core.internal.generator.EventGenerator;
import org.wso2.carbon.event.simulator.core.internal.generator.random.RandomAttributeGenerator;
import org.wso2.carbon.event.simulator.core.internal.generator.random.util.CustomBasedGenerator;
import org.wso2.carbon.event.simulator.core.internal.generator.random.util.PrimitiveBasedGenerator;
import org.wso2.carbon.event.simulator.core.internal.generator.random.util.PropertyBasedGenerator;
import org.wso2.carbon.event.simulator.core.internal.generator.random.util.RegexBasedGenerator;
import org.wso2.carbon.event.simulator.core.internal.util.CommonOperations;
import org.wso2.carbon.event.simulator.core.internal.util.EventConverter;
import org.wso2.carbon.event.simulator.core.internal.util.EventSimulatorConstants;
import org.wso2.carbon.event.simulator.core.service.EventSimulatorDataHolder;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.query.api.definition.Attribute;

import static org.wso2.carbon.event.simulator.core.internal.util.CommonOperations.checkAvailability;
import static org.wso2.carbon.event.simulator.core.internal.util.CommonOperations.checkAvailabilityOfArray;

import java.util.ArrayList;
import java.util.List;

/**
 * RandomEventGenerator class implements interface EventGenerator.
 * Thi class is responsible for producing events using random data generation.
 */
public class RandomEventGenerator implements EventGenerator {
    private static final Logger log = LoggerFactory.getLogger(RandomEventGenerator.class);
    private RandomSimulationDTO randomSimulationConfig;
    private List<RandomAttributeGenerator> randomAttrGenerators;
    private List<Attribute> streamAttributes;
    private Long currentTimestamp;
    private Long timestampEndTime;
    private Event nextEvent = null;


    /**
     * init() methods initializes random event generator
     *
     * @param streamConfiguration JSON object containing configuration for random event generation
     * @throws InvalidConfigException          if random stream simulation configuration is invalid
     * @throws InsufficientAttributesException if the number of random attribute configurations provided is not equal
     *                                         to the number of stream attributes
     */
    @Override
    public void init(JSONObject streamConfiguration) throws InvalidConfigException,
            InsufficientAttributesException {
        randomAttrGenerators = new ArrayList<>();
        randomSimulationConfig = validateRandomConfiguration(streamConfiguration);
        streamAttributes = EventSimulatorDataHolder.getInstance().getEventStreamService()
                .getStreamAttributes(randomSimulationConfig.getExecutionPlanName(),
                        randomSimulationConfig.getStreamName());

        if (streamAttributes == null) {
            throw new SimulatorInitializationException("Error occurred when initializing random event "
                    + "generator to simulate stream '" + randomSimulationConfig.getStreamName()
                    + "'. Execution plan '" + randomSimulationConfig.getExecutionPlanName() +
                    "' has not been deployed.");
        }

            /*
            * check whether the number of columns specified is the number of stream attributes
            * if yes, proceed with initialization of generator
            * else, throw an exception
            * */
        if (CommonOperations.checkAttributes(randomAttrGenerators.size(), streamAttributes.size())) {

            if (log.isDebugEnabled()) {
                log.debug("Initialize random generator for stream '" + randomSimulationConfig.getStreamName() +
                        "'");
            }
        } else {
            throw new InsufficientAttributesException("Stream '" + randomSimulationConfig.getStreamName() + "' has "
                    + streamAttributes.size() + " attribute(s) but random simulation configuration contains " +
                    "attribute configuration for only " + randomAttrGenerators.size() + " attribute(s).");
        }

    }


    /**
     * start() method is used to retrieve the first event
     */
    @Override
    public void start() {
        getNextEvent();

        if (log.isDebugEnabled()) {
            log.debug("Stop random generator for stream '" + randomSimulationConfig.getStreamName() + "'");
        }
    }


    /**
     * stop() method
     */
    @Override
    public void stop() {
        if (log.isDebugEnabled()) {
            log.debug("Stop random generator for stream '" + randomSimulationConfig.getStreamName() + "'");
        }
//        do nothing
    }


    /**
     * poll() method is used to retrieve the nextEvent of generator and assign the next event of with least timestamp
     * as nextEvent
     *
     * @return nextEvent
     */
    @Override
    public Event poll() {
        Event tempEvent = null;
        /*
        * if nextEvent is not null, it implies that more events may be generated by the generator. Hence call
        * getNExtEvent(0 method to assign the next event with least timestamp as nextEvent.
        * else if nextEvent == null, it implies that generator will not generate any more events. Hence return null.
        * */
        if (nextEvent != null) {
            tempEvent = nextEvent;
            getNextEvent();
        }
        return tempEvent;
    }


    /**
     * peek() method is used to access the nextEvent of generator
     *
     * @return nextEvent
     */
    @Override
    public Event peek() {
        return nextEvent;
    }


    /**
     * getNextEvent() method is used to get the next event with least timestamp
     */
    @Override
    public void getNextEvent() {

        try {
               /*
             * if timestampEndTime != null and is greater than the currentTimestamp, more events can be generated.
             * else, nextEvent is set to null to indicate that the generator will not produce any more events
             * */
            if (timestampEndTime == null || currentTimestamp <= timestampEndTime) {
                Object[] attributeValues = new Object[streamAttributes.size()];

                int i = 0;
                for (RandomAttributeGenerator randomAttributeGenerator : randomAttrGenerators) {
                    attributeValues[i++] = randomAttributeGenerator.generateAttribute();
                }
                nextEvent = EventConverter.eventConverter(streamAttributes, attributeValues, currentTimestamp);
                currentTimestamp += randomSimulationConfig.getTimeInterval();
            } else {
                nextEvent = null;
            }
        } catch (EventGenerationException e) {
            log.error("Drop event and create a new event. Error occurred when generating an even using random event " +
                    "generator to simulate stream '" + randomSimulationConfig.getStreamName() + "' : ", e);
            getNextEvent();
        }
    }


    /**
     * initTimestamp() method is used to set the timestamp start and end time.
     *
     * @param timestampStartTime least possible value for timestamp
     * @param timestampEndTime   maximum possible value for timestamp
     */
    @Override
    public void initTimestamp(Long timestampStartTime, Long timestampEndTime) {
        this.currentTimestamp = timestampStartTime;
        this.timestampEndTime = timestampEndTime;
        if (log.isDebugEnabled()) {
            log.debug("Timestamp range initiated for random event generator for stream '" +
                    randomSimulationConfig.getStreamName() + "'. Timestamp start time : " + timestampStartTime + " and"
                    + " timestamp end time : " + timestampEndTime);
        }
    }


    /**
     * getStreamName() method returns the name of the stream to which events are generated
     *
     * @return stream name
     */
    @Override
    public String getStreamName() {
        return randomSimulationConfig.getStreamName();
    }


    /**
     * getExecutionPlanName() method returns the name of the execution plan to which events are generated
     *
     * @return execution plan name
     */
    @Override
    public String getExecutionPlanName() {
        return randomSimulationConfig.getExecutionPlanName();
    }

    /**
     * validateRandomConfiguration() method parses the database simulation configuration into a DBSimulationDTO object
     *
     * @param streamConfig JSON object containing configuration required to simulate stream
     * @throws InvalidConfigException if the stream configuration is invalid
     */
    private RandomSimulationDTO validateRandomConfiguration(JSONObject streamConfig) throws InvalidConfigException {

            /*
            * set properties to RandomSimulationDTO.
            *
            * Perform the following checks prior to setting the properties.
            * 1. has
            * 2. isNull
            * 3. isEmpty
            *
            * if any of the above checks fail, throw an exception indicating which property is missing.
            * */

        if (!checkAvailability(streamConfig, EventSimulatorConstants.STREAM_NAME)) {
            throw new InvalidConfigException("Stream name is required for random data simulation");
        }

        if (!checkAvailability(streamConfig, EventSimulatorConstants.EXECUTION_PLAN_NAME)) {
            throw new InvalidConfigException("Execution plan name is required for random simulation of stream '" +
                    streamConfig.getString(EventSimulatorConstants.STREAM_NAME) + "'. Invalid stream configuration " +
                    "provided : " + streamConfig.toString());
        }

        /*
        * validate attribute configuration of the random stream configuration
        * if all random attributes can be initialized without an error, create the random simulation configuration
        * object
        * */
        if (checkAvailabilityOfArray(streamConfig, EventSimulatorConstants.ATTRIBUTE_CONFIGURATION)) {
            validateAttributeConfig(streamConfig.getJSONArray(EventSimulatorConstants.ATTRIBUTE_CONFIGURATION));
        } else {
            throw new InvalidConfigException("Attribute configuration is required for random simulation of stream '" +
                    streamConfig.getString(EventSimulatorConstants.STREAM_NAME) + "'. Invalid stream configuration : " +
                    streamConfig.toString());
        }


        RandomSimulationDTO randomSimulationDTO = new RandomSimulationDTO();
        randomSimulationDTO.setStreamName(streamConfig.getString(EventSimulatorConstants.STREAM_NAME));
        randomSimulationDTO.setExecutionPlanName(streamConfig
                .getString(EventSimulatorConstants.EXECUTION_PLAN_NAME));

        if (checkAvailability(streamConfig, EventSimulatorConstants.TIME_INTERVAL)) {
            randomSimulationDTO.setTimeInterval(streamConfig.getLong(EventSimulatorConstants.TIME_INTERVAL));
        } else {
            log.warn("Time interval is required for random data simulation of stream '" +
                    streamConfig.getString(EventSimulatorConstants.STREAM_NAME) + "'. Time interval will " +
                    "be set to 1 second");
            randomSimulationDTO.setTimeInterval(1);
        }

        return randomSimulationDTO;
    }


    /**
     * validateAttributeConfig() validates the attribute configurations provided for random simulation
     *
     * @param attributeConfig JSONArray containing attribute configurations
     * @throws InvalidConfigException if attribute configurations are invalid
     */
    private void validateAttributeConfig(JSONArray attributeConfig) throws InvalidConfigException {

        RandomAttributeDTO.RandomDataGeneratorType type;
        for (int i = 0; i < attributeConfig.length(); i++) {
            if (checkAvailability(attributeConfig.getJSONObject(i),
                    EventSimulatorConstants.RANDOM_DATA_GENERATOR_TYPE)) {

                  /*
                  * for each attribute configuration, switch using the random generation type and create
                  * respective attribute configuration objects.
                  * */
                try {
                    type = RandomAttributeDTO.RandomDataGeneratorType.valueOf(attributeConfig.getJSONObject(i)
                            .getString(EventSimulatorConstants.RANDOM_DATA_GENERATOR_TYPE));
                } catch (IllegalArgumentException e) {
                    throw new InvalidConfigException("Invalid random attribute generation type. Generation type must " +
                            "be either '" + RandomAttributeDTO.RandomDataGeneratorType.CUSTOM_DATA_BASED + "' or '"
                            + RandomAttributeDTO.RandomDataGeneratorType.PRIMITIVE_BASED + "' or '" +
                            RandomAttributeDTO.RandomDataGeneratorType.PROPERTY_BASED + "' or '" +
                            RandomAttributeDTO.RandomDataGeneratorType.REGEX_BASED + "'. Invalid attribute " +
                            "configuration : " + attributeConfig.getJSONObject(i).toString());
                }

                switch (type) {
                    case CUSTOM_DATA_BASED:
                        CustomBasedGenerator customBasedGenerator = new CustomBasedGenerator();
                        customBasedGenerator.validateAttributeConfig(attributeConfig.getJSONObject(i));
                        randomAttrGenerators.add(customBasedGenerator);
                        break;

                    case PRIMITIVE_BASED:
                        PrimitiveBasedGenerator primitiveBasedGenerator = new PrimitiveBasedGenerator();
                        primitiveBasedGenerator.validateAttributeConfig(attributeConfig.getJSONObject(i));
                        randomAttrGenerators.add(primitiveBasedGenerator);
                        break;

                    case PROPERTY_BASED:
                        PropertyBasedGenerator propertyBasedGenerator = new PropertyBasedGenerator();
                        propertyBasedGenerator.validateAttributeConfig(attributeConfig.getJSONObject(i));
                        randomAttrGenerators.add(propertyBasedGenerator);
                        break;

                    case REGEX_BASED:
                        RegexBasedGenerator regexBasedGenerator = new RegexBasedGenerator();
                        regexBasedGenerator.validateAttributeConfig(attributeConfig.getJSONObject(i));
                        randomAttrGenerators.add(regexBasedGenerator);
                        break;
                }
            } else {
                throw new InvalidConfigException("Random attribute generator type is required for random " +
                        "simulation. Generation type must be either '" +
                        RandomAttributeDTO.RandomDataGeneratorType.CUSTOM_DATA_BASED + "' or '" +
                        RandomAttributeDTO.RandomDataGeneratorType.PRIMITIVE_BASED + "' or '" +
                        RandomAttributeDTO.RandomDataGeneratorType.PROPERTY_BASED + "' or '" +
                        RandomAttributeDTO.RandomDataGeneratorType.REGEX_BASED + "'. Invalid attribute configuration " +
                        ": " + attributeConfig.getJSONObject(i).toString());
            }
        }
    }

}
