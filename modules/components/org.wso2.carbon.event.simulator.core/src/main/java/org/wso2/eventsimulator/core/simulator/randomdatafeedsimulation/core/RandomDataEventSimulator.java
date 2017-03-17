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

package org.wso2.eventsimulator.core.simulator.randomdatafeedsimulation.core;


import org.apache.log4j.Logger;
import org.wso2.carbon.stream.processor.core.StreamDefinitionRetriever;
import org.wso2.eventsimulator.core.internal.EventSimulatorDataHolder;
import org.wso2.siddhi.core.event.Event;
import org.wso2.eventsimulator.core.simulator.EventSimulator;
import org.wso2.eventsimulator.core.simulator.bean.FeedSimulationStreamConfiguration;
import org.wso2.eventsimulator.core.simulator.exception.EventSimulationException;
import org.wso2.eventsimulator.core.simulator.randomdatafeedsimulation.bean.FeedSimulationStreamAttributeDto;
import org.wso2.eventsimulator.core.simulator.randomdatafeedsimulation.bean.RandomDataSimulationDto;
import org.wso2.eventsimulator.core.simulator.randomdatafeedsimulation.util.AttributeGenerator;
import org.wso2.eventsimulator.core.util.EventConverter;
import org.wso2.eventsimulator.core.util.EventSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;


/**
 * This simulator simulates the execution plan by sending events. These events are generated by
 * generated random values according to given configuration.
 * <p>
 * This simulator class implements EventSimulator Interface
 * <p>
 * For simulation It generates Random values for an event using
 * {@link AttributeGenerator#generateAttributeValue(FeedSimulationStreamAttributeDto, StreamDefinitionRetriever.Type)}
 */
public class RandomDataEventSimulator implements EventSimulator {
    private static final Logger log = Logger.getLogger(RandomDataEventSimulator.class);
    private final Object lock = new Object();
    private volatile boolean isPaused = false;
    private volatile boolean isStopped = false;
    private RandomDataSimulationDto streamConfiguration;

    /**
     * Initialize RandomDataEventSimulator to start the simulation
     *
     * @param streamConfiguration
     */
    public RandomDataEventSimulator(RandomDataSimulationDto streamConfiguration) {
        this.streamConfiguration = streamConfiguration;
    }

    @Override
    public void pause() {
        isPaused = true;
    }

    @Override
    public void resume() {
        isPaused = false;
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    @Override
    public void stop() {
        isPaused = true;
        isStopped = true;
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    @Override
    public FeedSimulationStreamConfiguration getStreamConfiguration() {
        return streamConfiguration;
    }

    /**
     * start simulation for given configuration
     */
    @Override
    public void run() {
        sendEvent(streamConfiguration);
    }


    private void sendEvent(RandomDataSimulationDto randomDataSimulationConfig) {
        int delay = randomDataSimulationConfig.getDelay();
        if (delay <= 0) {
            log.warn("Events will be sent continuously since the delay between events are set to "
                    + delay + "milliseconds");
            delay = 0;
        }

        double nEvents = randomDataSimulationConfig.getEvents();
        LinkedHashMap<String,StreamDefinitionRetriever.Type> streamDefinition =
                EventSimulatorDataHolder.getInstance().getStreamDefinitionService().streamDefinitionService(randomDataSimulationConfig.getStreamName());
        ArrayList<StreamDefinitionRetriever.Type> streamAttributeTypes = new ArrayList<StreamDefinitionRetriever.Type>(streamDefinition.values());
        try {
            // Generate dummy attributes to warm up Random Data generation.
            // Because It takes some ms to generate 1st value.
            // It effects the delay between two events and trade off in performance also
            // So to reduce this draw back Initially it generates some dummy attributes
            String[] dummyAttribute =
                    new String[randomDataSimulationConfig.getFeedSimulationStreamAttributeDto().size()];
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < randomDataSimulationConfig.getFeedSimulationStreamAttributeDto().size(); j++) {
                    dummyAttribute[j] = AttributeGenerator.generateAttributeValue(
                            randomDataSimulationConfig.getFeedSimulationStreamAttributeDto().get(j),
                            streamAttributeTypes.get(j));
                }
            }

            // at this point starts to generate random  attribute values and convert it into siddhi event
            // and send that event to input handler up to no of events reached to events given by user
            for (int i = 0; i < nEvents; i++) {
                int nAttributes = randomDataSimulationConfig.getFeedSimulationStreamAttributeDto().size();
                if (!isPaused) {
                    String[] attributeValue = new String[nAttributes];

                    //Generate Random values for each attribute
                    for (int j = 0; j < nAttributes; j++) {
                        attributeValue[j] = AttributeGenerator.generateAttributeValue(
                                randomDataSimulationConfig.getFeedSimulationStreamAttributeDto().get(j),
                                streamAttributeTypes.get(j));


                    }

                    //convert Attribute values into event
                    Event event = EventConverter.eventConverter(streamDefinition, attributeValue);
                    //calculate percentage that event has send

                    // Percentage of send events
                    double percentage = ((i + 1) * 100) / nEvents;

                    System.out.println("Input Event (random feed) " + Arrays.deepToString(event.getData()) + ". Percentage :" + percentage);
                    //send the event to input handler
                    EventSender.getInstance().sendEvent(randomDataSimulationConfig.getExecutionPlanName(),randomDataSimulationConfig.getStreamName(),event);
                    //delay between two events
                    if (delay > 0) {
                        Thread.sleep(delay);
                    }
                } else if (isStopped) {
                    break;
                } else {
                    synchronized (lock) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            continue;
                        }
                    }
                }
            }
        } catch (EventSimulationException e) {
            log.error("Event dropped due to Error occurred during generating an event" + e.getMessage());
        } catch (InterruptedException e) {
            log.error("Error occurred during send event" + e.getMessage());
        }
    }
}