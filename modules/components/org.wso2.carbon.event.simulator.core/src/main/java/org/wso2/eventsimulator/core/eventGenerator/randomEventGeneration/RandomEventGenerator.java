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

package org.wso2.eventsimulator.core.eventGenerator.randomEventGeneration;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.eventsimulator.core.eventGenerator.EventGenerator;
import org.wso2.eventsimulator.core.eventGenerator.randomEventGeneration.bean.*;
import org.wso2.eventsimulator.core.eventGenerator.randomEventGeneration.util.CustomBasedGenerator;
import org.wso2.eventsimulator.core.eventGenerator.randomEventGeneration.util.PrimitiveBasedGenerator;
import org.wso2.eventsimulator.core.eventGenerator.randomEventGeneration.util.PropertyBasedGenerator;
import org.wso2.eventsimulator.core.eventGenerator.randomEventGeneration.util.RegexBasedGenerator;
import org.wso2.eventsimulator.core.internal.EventSimulatorDataHolder;
import org.wso2.eventsimulator.core.util.EventConverter;
import org.wso2.eventsimulator.core.util.EventSimulatorParser;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.List;

public class RandomEventGenerator implements EventGenerator{
    private static final Logger log = LoggerFactory.getLogger(RandomEventGenerator.class);
    private RandomSimulationDto randomGenerationConfig;
    private List<RandomAttributeDto> randomAttributeList;
    private List<Attribute> streamAttributes;
    private Long currentTimestamp;
    private Long timestampEndTime;
    private Long timeInterval;
    private Event nextEvent = null;


    @Override
    public void init(JSONObject streamConfiguration) {

        randomGenerationConfig = EventSimulatorParser.randomDataSimulatorParser(streamConfiguration);
        streamAttributes = EventSimulatorDataHolder.getInstance().getEventStreamService()
                .getStreamAttributes(randomGenerationConfig.getExecutionPlanName(),randomGenerationConfig.getStreamName());
        randomAttributeList = randomGenerationConfig.getAttributeConfigurations();
        timeInterval = randomGenerationConfig.getTimeInterval();

    }

    @Override
    public void start() {
        getNextEvent();
    }

    @Override
    public void stop() {

    }

    @Override
    public Event poll() {
        Event tempEvent = null;
        if (nextEvent != null) {
            tempEvent = nextEvent;
            getNextEvent();
        }
        return tempEvent;
    }

    @Override
    public Event peek() {
        return nextEvent;
    }

    @Override
    public void getNextEvent() {

        if (currentTimestamp <= timestampEndTime || timestampEndTime == null) {
            Object[] attributeValues = new Object[streamAttributes.size()];

            for (int i = 0; i < streamAttributes.size(); i++) {
                RandomAttributeDto.RandomDataGeneratorType dataGeneratorType = randomAttributeList.get(i).getType();

                switch (dataGeneratorType) {

                    case CUSTOM_DATA_BASED:
                        attributeValues[i] = CustomBasedGenerator.generateCustomBasedData((CustomBasedAttributeDto) randomAttributeList.get(i));
                        break;

                    case PRIMITIVE_BASED:
                        attributeValues[i] = PrimitiveBasedGenerator
                                .generatePrimitiveBasedData((PrimitiveBasedAttributeDto) randomAttributeList.get(i));
                        break;

                    case PROPERTY_BASED:
                        attributeValues[i] = PropertyBasedGenerator.generatePropertyBasedData((PropertyBasedAttributeDto) randomAttributeList.get(i));
                        break;

                    case REGEX_BASED:
                        attributeValues[i] = RegexBasedGenerator.generateRegexBasedData((RegexBasedAttributeDto) randomAttributeList.get(i));
                        break;
                }
            }
            nextEvent = EventConverter.eventConverter(streamAttributes,attributeValues,currentTimestamp);
            currentTimestamp += timeInterval;
        } else {
            nextEvent = null;
        }

    }

    @Override
    public void initTimestamp(Long timestampStartTime, Long timestampEndTime) {
        this.currentTimestamp = timestampStartTime;
        this.timestampEndTime = timestampEndTime;
    }

    @Override
    public String getStreamName() {
        return randomGenerationConfig.getStreamName();
    }

    @Override
    public String getExecutionPlanName() {
        return randomGenerationConfig.getExecutionPlanName();
    }
}
