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
package org.wso2.eventsimulator.core.simulator.singleventsimulator;

import org.apache.log4j.Logger;
import org.wso2.eventsimulator.core.internal.EventSimulatorDataHolder;
import org.wso2.siddhi.core.event.Event;
import org.wso2.eventsimulator.core.simulator.EventSimulator;
import org.wso2.eventsimulator.core.simulator.bean.FeedSimulationStreamConfiguration;
import org.wso2.eventsimulator.core.simulator.exception.EventSimulationException;
import org.wso2.eventsimulator.core.util.EventConverter;
import org.wso2.eventsimulator.core.util.EventSender;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.Arrays;
import java.util.List;

/**
 * SingleEventSimulator simulates the deployed execution plan using single event.
 * It implements EventSimulator parentclass
 */
public class SingleEventSimulator implements EventSimulator {
    private static final Logger log = Logger.getLogger(SingleEventSimulator.class);
    private SingleEventDto streamConfiguration;

    /**
     * Initialize single event simulator for single event simulation process
     *
     * @param streamConfiguration
     */
    public SingleEventSimulator(SingleEventDto streamConfiguration) {
        this.streamConfiguration = streamConfiguration;
    }

    @Override
    public void pause() {
        // no need to pause
    }

    @Override
    public void resume() {
        // no need to pause
    }

    @Override
    public void stop() {
        // no need to stop
    }

    @Override
    public FeedSimulationStreamConfiguration getStreamConfiguration() {
        return streamConfiguration;
    }

    @Override
    public void run() {
        //attributeValue used to store values of attributes of an input stream
        String[] attributeValue = new String[streamConfiguration.getAttributeValues().size()];
        attributeValue = streamConfiguration.getAttributeValues().toArray(attributeValue);
        List<Attribute> streamAttributes = EventSimulatorDataHolder.getInstance()
                .getEventStreamService().getStreamAttributes(streamConfiguration.getExecutionPlanName(),streamConfiguration.getStreamName());
        Event event;
        try {
            //Convert attribute value as an Event
            event = EventConverter.eventConverter(streamAttributes, attributeValue,0L);
            if (streamConfiguration.getExecutionPlanName() != null && streamConfiguration.getStreamName() != null && event.getData() != null) {
                EventSender.getInstance().sendEvent(streamConfiguration.getExecutionPlanName(), streamConfiguration.getStreamName(), event);
                System.out.println("Input Event (Single feed) " + Arrays.deepToString(event.getData()));
            } else {
                log.error("Simulation configuration must specify a stream name, execution plan name and event data. Current configuration is" +
                        "stream name : '" + streamConfiguration.getStreamName() +"', execution plan name : '" + streamConfiguration.getExecutionPlanName() +
                        "' and event data : '" + Arrays.deepToString(event.getData()) + "'");
            }
        } catch (EventSimulationException e) {
            log.error("Error occurred : Failed to send an event. " + e.getMessage());
        }
    }
}
