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

package org.wso2.eventsimulator.core.eventGenerator.csvEventGeneration;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.eventsimulator.core.eventGenerator.EventGenerator;
import org.wso2.eventsimulator.core.eventGenerator.csvEventGeneration.util.CSVReader;
import org.wso2.eventsimulator.core.internal.EventSimulatorDataHolder;
import org.wso2.eventsimulator.core.simulator.csvFeedSimulation.CSVFileSimulationDto;
import org.wso2.eventsimulator.core.simulator.exception.EventSimulationException;
import org.wso2.eventsimulator.core.util.EventSimulatorParser;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * CSVEventGenerator implements EventGenerator interface.
 * This class produces events using csv files
 * */
public class CSVEventGenerator implements EventGenerator{
    private final Logger log = LoggerFactory.getLogger(CSVEventGenerator.class);
    private CSVFileSimulationDto csvConfiguration;
    private Long timestampStartTime;
    private Long timestampEndTime;
    private List<Attribute> streamAttributes;
    private Event nextEvent;
    private List<Event> currentTimestampEvents = new ArrayList<Event>();
    private CSVReader csvReader;
    private TreeMap<Long, ArrayList<Event>> eventsMap = new TreeMap<Long, ArrayList<Event>>();

    /**
     * constructor for CSVEventGenerator class.
     * */
    public CSVEventGenerator(){}


    /**
     * init() method performs following actions
     * 1. Create a CSVFileSimulationDto object by parsing the csv simulation configuration
     * 2. Initialize a fileReader
     *
     * @param streamConfiguration  :  json object containing configuration for csv simulation
     * */
    @Override
    public void init(JSONObject streamConfiguration) {
        if (log.isDebugEnabled()) {
            log.debug("Initialize a CSV generator for file '" + csvConfiguration.getFileName() + "' for stream '" +
                    csvConfiguration.getStreamName() + "'.");
        }
        csvConfiguration = EventSimulatorParser.fileFeedSimulatorParser(streamConfiguration);
        streamAttributes = EventSimulatorDataHolder.getInstance().getEventStreamService()
                .getStreamAttributes(csvConfiguration.getExecutionPlanName(),csvConfiguration.getStreamName());
        csvReader = new CSVReader(csvConfiguration, streamAttributes, timestampStartTime, timestampEndTime);
        csvReader.initializeFileReader();
    }

    /**
     * start method
     * */
    @Override
    public void start() {
        if (log.isDebugEnabled()) {
            log.debug("Start CSV generator for file '" + csvConfiguration.getFileName() + "' for stream '" +
                    csvConfiguration.getStreamName() + "'.");
        }
        if (csvConfiguration.getIsOrdered()) {
            nextEvent = csvReader.getNextEvent();
        } else {
            eventsMap = csvReader.getEventsMap();
            if (!eventsMap.isEmpty()) {
                currentTimestampEvents = eventsMap.pollFirstEntry().getValue();
                nextEvent = currentTimestampEvents.get(0);
                currentTimestampEvents.remove(0);
            } else {
                log.error("File '" + csvConfiguration.getFileName() + "' does not have data required to produce events.");
                throw new EventSimulationException("File '" + csvConfiguration.getFileName() + "' does not have data required to produce events.");
            }
        }
    }

    @Override
    public void stop() {
        if (log.isDebugEnabled()) {
            log.debug("Stop CSV generator for file '" + csvConfiguration.getFileName() + "' for stream '" +
                    csvConfiguration.getStreamName() + "'.");
        }
        csvReader.closeParser();
    }

    @Override
    public Event poll() {
        if (log.isDebugEnabled()) {
            log.debug("Poll next event of CSV generator for file '" + csvConfiguration.getFileName() + "' for stream '" +
                    csvConfiguration.getStreamName() + "'.");
        }
        Event tempEvent = null;
        try {
            if (nextEvent != null) {
                tempEvent = nextEvent;
                getNextEvent();
            }
        } catch (IndexOutOfBoundsException e) {
            log.error("Error occurred when accessing next event : " + e.getMessage());
        }
        return tempEvent;
    }

    @Override
    public Event peek() {
        if (log.isDebugEnabled()) {
            log.debug("Peek next event of CSV generator for file '" + csvConfiguration.getFileName() + "' for stream '" +
                    csvConfiguration.getStreamName() + "'.");
        }
        return nextEvent;
    }

    @Override
    public void initTimestamp(Long timestampStartTime, Long timestampEndTime) {
        if (log.isDebugEnabled()) {
            log.debug("Initialize timestamps CSV generator for file '" + csvConfiguration.getFileName() + "' for stream '" +
                    csvConfiguration.getStreamName() + "'.");
        }
        this.timestampStartTime = timestampStartTime;
        this.timestampEndTime = timestampEndTime;
    }

    @Override
    public String getStreamName() {
        if (log.isDebugEnabled()) {
            log.debug("Get stream name from CSV generator for file '" + csvConfiguration.getFileName() + "'.");
        }return csvConfiguration.getStreamName();
    }

    @Override
    public String getExecutionPlanName() {
        if (log.isDebugEnabled()) {
            log.debug("Get execution plan name from CSV generator for file '" + csvConfiguration.getFileName() + "'.");
        }
        return csvConfiguration.getExecutionPlanName();
    }

    @Override
    public void getNextEvent() {
        if (log.isDebugEnabled()) {
            log.debug("Get next event from CSV generator for file '" + csvConfiguration.getFileName() + "' for stream '" +
                    csvConfiguration.getStreamName() + "'.");
        }
       if (csvConfiguration.getIsOrdered()) {
           nextEvent = csvReader.getNextEvent();
       } else {
           getNextEventForCurrentTimestamp();
       }
    }

    private void getEventsForNextTimestamp() {
        if (log.isDebugEnabled()) {
            log.debug("Get events for next timestamp from CSV generator for file '" + csvConfiguration.getFileName()
                    + "' for stream '" + csvConfiguration.getStreamName() + "'.");
        }
        if (!eventsMap.isEmpty()) {
            currentTimestampEvents = eventsMap.pollFirstEntry().getValue();
        } else {
            currentTimestampEvents = null;
        }
    }

    private void getNextEventForCurrentTimestamp() {
        if (log.isDebugEnabled()) {
            log.debug("Get next event for current timestamp from CSV generator for file '" + csvConfiguration.getFileName()
                    + "' for stream '" + csvConfiguration.getStreamName() + "'.");
        }
        if (currentTimestampEvents != null) {
            if (!currentTimestampEvents.isEmpty()) {
                nextEvent = currentTimestampEvents.get(0);
                currentTimestampEvents.remove(0);
            } else {
                getEventsForNextTimestamp();
                if (currentTimestampEvents != null) {
                    nextEvent = currentTimestampEvents.get(0);
                    currentTimestampEvents.remove(0);
                } else {
                    nextEvent = null;
                }
            }
        }
    }
}
