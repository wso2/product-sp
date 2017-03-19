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

package org.wso2.eventsimulator.core.eventGenerator.randomEventGeneration.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.eventsimulator.core.eventGenerator.EventGenerator;
import org.wso2.eventsimulator.core.eventGenerator.bean.RandomSimulationDto;
import org.wso2.eventsimulator.core.eventGenerator.bean.StreamConfigurationDto;
import org.wso2.eventsimulator.core.eventGenerator.randomEventGeneration.bean.CustomBasedAttributeDto;
import org.wso2.eventsimulator.core.eventGenerator.randomEventGeneration.bean.PrimitiveBasedAttributeDto;
import org.wso2.eventsimulator.core.eventGenerator.randomEventGeneration.bean.PropertyBasedAttributeDto;
import org.wso2.eventsimulator.core.eventGenerator.randomEventGeneration.bean.RandomAttributeDto;
import org.wso2.eventsimulator.core.eventGenerator.randomEventGeneration.bean.RegexBasedAttributeDto;
import org.wso2.eventsimulator.core.eventGenerator.randomEventGeneration.util.CustomBasedGenerator;
import org.wso2.eventsimulator.core.eventGenerator.randomEventGeneration.util.PrimitiveBasedGenerator;
import org.wso2.eventsimulator.core.eventGenerator.randomEventGeneration.util.PropertyBasedGenerator;
import org.wso2.eventsimulator.core.eventGenerator.randomEventGeneration.util.RegexBasedGenerator;
import org.wso2.eventsimulator.core.eventGenerator.util.EventConverter;
import org.wso2.eventsimulator.core.eventGenerator.util.exceptions.EventGenerationException;
import org.wso2.eventsimulator.core.internal.EventSimulatorDataHolder;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.List;

/**
 * RandomEventGenerator class implements interface EventGenerator.
 * Thi class is responsible for producing events using random data generation.
 */
public class RandomEventGenerator implements EventGenerator {
    private static final Logger log = LoggerFactory.getLogger(RandomEventGenerator.class);
    private RandomSimulationDto randomGenerationConfig;
    private List<RandomAttributeDto> randomAttributeList;
    private List<Attribute> streamAttributes;
    private Long currentTimestamp;
    private Long timestampEndTime;
    private Long timeInterval;
    private Event nextEvent = null;


    /**
     * init() methods initializes random event generator
     *
     * @param streamConfiguration JSON object containing configuration for random event generation
     */
    @Override
    public void init(StreamConfigurationDto streamConfiguration) {

        randomGenerationConfig = (RandomSimulationDto) streamConfiguration;
        streamAttributes = EventSimulatorDataHolder.getInstance().getEventStreamService()
                .getStreamAttributes(randomGenerationConfig.getExecutionPlanName(),
                        randomGenerationConfig.getStreamName());
        randomAttributeList = randomGenerationConfig.getAttributeConfigurations();

        if (streamAttributes == null) {
            throw new EventGenerationException("Error occurred when generating events from database event " +
                    "generator to simulate stream '" + randomGenerationConfig.getStreamName()
                    + "'. Execution plan '" + randomGenerationConfig.getExecutionPlanName() +
                    "' has not been deployed.");
        }

        if (randomAttributeList.size() != streamAttributes.size()) {
            throw new EventGenerationException("Stream '" + randomGenerationConfig.getStreamName() + "' has " +
                    streamAttributes.size() + " attribute(s) but random simulation configuration contains attribute" +
                    " configuration for only " + randomAttributeList.size() + " attribute(s).");
        }
        timeInterval = randomGenerationConfig.getTimeInterval();

        if (log.isDebugEnabled()) {
            log.debug("Initialize random generator for stream '" + randomGenerationConfig.getStreamName() + "'");
        }


    }


    /**
     * start() method is used to retrieve the first event
     */
    @Override
    public void start() {
        getNextEvent();

        if (log.isDebugEnabled()) {
            log.debug("Stop random generator for stream '" + randomGenerationConfig.getStreamName() + "'");
        }
    }


    /**
     * stop() method
     */
    @Override
    public void stop() {
        if (log.isDebugEnabled()) {
            log.debug("Stop random generator for stream '" + randomGenerationConfig.getStreamName() + "'");
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

                for (int i = 0; i < streamAttributes.size(); i++) {
                    RandomAttributeDto.RandomDataGeneratorType dataGeneratorType = randomAttributeList.get(i).getType();

                    switch (dataGeneratorType) {

                        case CUSTOM_DATA_BASED:
                            attributeValues[i] = CustomBasedGenerator
                                    .generateCustomBasedData((CustomBasedAttributeDto) randomAttributeList.get(i));
                            break;

                        case PRIMITIVE_BASED:
                            attributeValues[i] = PrimitiveBasedGenerator
                                    .generatePrimitiveBasedData((PrimitiveBasedAttributeDto) randomAttributeList.get(i));
                            break;

                        case PROPERTY_BASED:
                            attributeValues[i] = PropertyBasedGenerator
                                    .generatePropertyBasedData((PropertyBasedAttributeDto) randomAttributeList.get(i));
                            break;

                        case REGEX_BASED:
                            attributeValues[i] = RegexBasedGenerator
                                    .generateRegexBasedData((RegexBasedAttributeDto) randomAttributeList.get(i));
                            break;
                    }
                }
                nextEvent = EventConverter.eventConverter(streamAttributes, attributeValues, currentTimestamp);
                currentTimestamp += timeInterval;
            }
        } catch (EventGenerationException e) {
            log.error("Error occurred when generating random data event for stream '" +
                    randomGenerationConfig.getStreamName() + "' : ", e);
        }

        if (log.isDebugEnabled()) {
            log.debug("get next event of random generator to simulate stream '" + randomGenerationConfig.getStreamName()
                    + "'");
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
                    randomGenerationConfig.getStreamName() + "'. Timestamp start time : " + timestampStartTime + " and" +
                    " timestamp end time : " + timestampEndTime);
        }
    }


    /**
     * getStreamName() method returns the name of the stream to which events are generated
     *
     * @return stream name
     */
    @Override
    public String getStreamName() {
        return randomGenerationConfig.getStreamName();
    }


    /**
     * getExecutionPlanName() method returns the name of the execution plan to which events are generated
     *
     * @return execution plan name
     */
    @Override
    public String getExecutionPlanName() {
        return randomGenerationConfig.getExecutionPlanName();
    }
}
