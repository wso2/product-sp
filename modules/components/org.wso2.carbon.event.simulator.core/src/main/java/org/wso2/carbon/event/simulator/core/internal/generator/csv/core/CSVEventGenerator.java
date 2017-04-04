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

package org.wso2.carbon.event.simulator.core.internal.generator.csv.core;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.event.simulator.core.exception.InvalidConfigException;
import org.wso2.carbon.event.simulator.core.exception.SimulatorInitializationException;
import org.wso2.carbon.event.simulator.core.internal.bean.CSVSimulationDTO;
import org.wso2.carbon.event.simulator.core.internal.generator.EventGenerator;
import org.wso2.carbon.event.simulator.core.internal.generator.csv.util.CSVReader;
import org.wso2.carbon.event.simulator.core.internal.generator.csv.util.FileStore;
import org.wso2.carbon.event.simulator.core.internal.util.EventSimulatorConstants;
import org.wso2.carbon.event.simulator.core.service.EventSimulatorDataHolder;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.query.api.definition.Attribute;

import static org.wso2.carbon.event.simulator.core.internal.util.CommonOperations.checkAvailability;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * CSVEventGenerator implements EventGenerator interface.
 * This class produces events using csv files
 */
public class CSVEventGenerator implements EventGenerator {
    private final Logger log = LoggerFactory.getLogger(CSVEventGenerator.class);
    private CSVSimulationDTO csvConfiguration;
    private List<Attribute> streamAttributes;
    private long timestampStartTime;
    private long timestampEndTime;
    /**
     * nextEvent variable holds the next event with least timestamp
     */
    private Event nextEvent;
    private CSVReader csvReader;
    private List<Event> currentTimestampEvents;
    private TreeMap<Long, ArrayList<Event>> eventsMap;


    /**
     * constructor for CSVEventGenerator class.
     * performs following actions
     * 1. Create a CSVSimulationDTO object by parsing the csv simulation configuration
     * 2.initialize the start time and end time for timestamps.
     * An even will be sent only if its timestamp falls within the boundaries of the timestamp start timestamp and
     * end time.
     * If we want to send all events with timestamp greater than the timestamp start time, the timestamp end time will
     * be set to -1.
     * 3. Initialize a fileReader
     *
     * @param sourceConfig source configuration object containing configuration for csv simulation
     * @throws InvalidConfigException if invalid configuration is provided for CSV event generation
     */
    public CSVEventGenerator(JSONObject sourceConfig, long timestampStartTime, long timestampEndTime)
            throws InvalidConfigException {
//        create a CSV simulation configuration object
        csvConfiguration = validateCSVConfiguration(sourceConfig);
//        initialize timestamp range
        this.timestampStartTime = timestampStartTime;
        this.timestampEndTime = timestampEndTime;
        if (log.isDebugEnabled()) {
            log.debug("Timestamp range initiated for random event generator for stream '" +
                    csvConfiguration.getStreamName() + "'. Timestamp start time : " + timestampStartTime + " and" +
                    " timestamp end time : " + timestampEndTime);
        }
//        retrieve stream attributes of the stream being simulated
        streamAttributes = EventSimulatorDataHolder.getInstance().getEventStreamService()
                .getStreamAttributes(csvConfiguration.getExecutionPlanName(), csvConfiguration.getStreamName());
        /**
         * check whether the execution plan has been deployed.
         * if streamAttributes == null, it implies that execution plan has not been deployed yet
         */
        if (streamAttributes != null) {
            csvReader = new CSVReader(csvConfiguration.getFileName(), csvConfiguration.getIsOrdered());
            if (log.isDebugEnabled()) {
                log.debug("Initialize CSV generator for file '" + csvConfiguration.getFileName() + "' to simulate" +
                        " stream '" + csvConfiguration.getStreamName() + "'.");
            }
        } else {
            throw new SimulatorInitializationException("Error occurred when initializing CSV event generator" +
                    " for file '" + csvConfiguration.getFileName() + "' to simulate stream '"
                    + csvConfiguration.getStreamName() + "'. Execution plan '" +
                    csvConfiguration.getExecutionPlanName() + "' has not been deployed.");
        }
    }

    /**
     * start() method begins event simulation by creating the first event
     */
    @Override
    public void start() {
        /**
         * if the CSV file is ordered by timestamp, create the first event and assign it as the nextEvent of
         * the generator.
         * else, create a treeMap of events. Retrieve the list of events with least timestamp as currentTimestampEvents
         * and assign the first event of the least timestamp as the nextEvent of the generator
         * */
        if (csvConfiguration.getIsOrdered()) {
            nextEvent = csvReader.getNextEvent(csvConfiguration.getStreamName(), streamAttributes,
                    csvConfiguration.getDelimiter(), Integer.parseInt(csvConfiguration.getTimestampAttribute()),
                    csvConfiguration.getTimeInterval(), timestampStartTime, timestampEndTime);
        } else {
            currentTimestampEvents = new ArrayList<>();
            eventsMap = new TreeMap<>();
            eventsMap = csvReader.getEventsMap(csvConfiguration.getDelimiter(), csvConfiguration.getStreamName(),
                    streamAttributes, Integer.parseInt(csvConfiguration.getTimestampAttribute()), timestampStartTime,
                    timestampEndTime);
            currentTimestampEvents = eventsMap.pollFirstEntry().getValue();
            nextEvent = currentTimestampEvents.get(0);
            currentTimestampEvents.remove(0);
        }
        if (log.isDebugEnabled()) {
            log.debug("Start CSV generator for file '" + csvConfiguration.getFileName() + "' for simulation of stream" +
                    " '" + csvConfiguration.getStreamName() + "'.");
        }
    }


    /**
     * stop() method is used to release resources used to read CSV file
     */
    @Override
    public void stop() {
        csvReader.closeParser(csvConfiguration.getIsOrdered());
        if (log.isDebugEnabled()) {
            log.debug("Stop CSV generator for file '" + csvConfiguration.getFileName() + "' for stream '" +
                    csvConfiguration.getStreamName() + "'.");
        }
    }


    /**
     * poll() returns nextEvent of the generator and assign the event with next least timestamp as the nextEvent
     *
     * @return event with least timestamp
     */
    @Override
    public Event poll() {
        Event tempEvent = null;
        /**
         * nextEvent != null implies that the generator may be able to produce more events. Hence, call getNextEvent()
         * to obtain the next event.
         * if nextEvent == null, return null to indicate that the generator will not be producing any more events
         */
        if (nextEvent != null) {
            tempEvent = nextEvent;
            getNextEvent();
        }
        return tempEvent;
    }


    /**
     * peek() method is used to view the nextEvent of the generator
     *
     * @return the event with least timestamp
     */
    @Override
    public Event peek() {
        return nextEvent;
    }

    /**
     * getStreamName() is used to obtain the name of the stream to which events are generated
     *
     * @return name of the stream
     */
    @Override
    public String getStreamName() {
        return csvConfiguration.getStreamName();
    }


    /**
     * getExecutionPlanName() is used to obtain the name of execution plan which is being simulated
     *
     * @return name of the execution plan
     */
    @Override
    public String getExecutionPlanName() {
        return csvConfiguration.getExecutionPlanName();
    }


    /**
     * getNextEvent() is used to obtain the next event with least timestamp
     */
    @Override
    public void getNextEvent() {
        /**
         * if the CSV file is ordered by timestamp, create next event and assign it as the nextEvent of generator
         * else, assign the next event with current timestamp as nextEvent of generator
         */
        if (csvConfiguration.getIsOrdered()) {
            nextEvent = csvReader.getNextEvent(csvConfiguration.getStreamName(), streamAttributes,
                    csvConfiguration.getDelimiter(), Integer.parseInt(csvConfiguration.getTimestampAttribute()),
                    csvConfiguration.getTimeInterval(), timestampStartTime, timestampEndTime);
        } else {
            getNextEventForCurrentTimestamp();
        }
    }


    /**
     * getEventsForNextTimestamp() is used to get list of events with the next least timestamp
     */
    private void getEventsForNextTimestamp() {
        /**
         * if the events map is not empty, it implies that there are more event. Hence, retrieve the list of events with
         * the next least timestamp
         * else, there are no more events, hence list of events with current timestamp is set to null
         * */
        if (!eventsMap.isEmpty()) {
            currentTimestampEvents = eventsMap.pollFirstEntry().getValue();
        } else {
            currentTimestampEvents = null;
        }
        if (log.isDebugEnabled()) {
            log.debug("Get events for next timestamp from CSV generator for file '" + csvConfiguration.getFileName()
                    + "' for stream '" + csvConfiguration.getStreamName() + "'.");
        }
    }


    /**
     * getNextEventForCurrentTimestamp() method is used to retrieve an event with the least timestamp
     */
    private void getNextEventForCurrentTimestamp() {
        /**
         * if currentTimestampEvents != null , it implies that more events will be created by the generator
         * if currentTimestampEvents list is not empty, get the next event in list as nextEvent and remove that even
         * from the list.
         * else, call getEventsForNextTimestamp() to retrieve a list of events with the next least timestamp.
         * if currentTimestampEvents != null after the method call, it implies that more events will be generated.
         * assign the first event in list as nextEvent and remove it from the list.
         * else if currentTimestampEvents == null, it implies that no more events will be created, hence assign null
         * to nextEvent.
         * */
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
        if (log.isDebugEnabled()) {
            log.debug("Get next event for current timestamp from CSV generator for file '" +
                    csvConfiguration.getFileName() + "' for stream '" + csvConfiguration.getStreamName() + "'.");
        }
    }

    /**
     * validateCSVConfiguration() parsers the source configuration in to csv configuration object
     *
     * @param sourceConfig JSON object containing configuration required to simulate stream
     * @return CSVSimulationDTO containing csv simulation configuration
     * @throws InvalidConfigException if configuration is invalid
     */
    private CSVSimulationDTO validateCSVConfiguration(JSONObject sourceConfig) throws InvalidConfigException {
        /**
         * set properties to CSVSimulationDTO.
         *
         * Perform the following checks prior to setting the properties.
         * 1. has
         * 2. isNull
         * 3. isEmpty
         *
         * if any of the above checks fail, throw an exception indicating which property is missing.
         * */
        if (!checkAvailability(sourceConfig, EventSimulatorConstants.STREAM_NAME)) {
            throw new InvalidConfigException("Stream name is required for CSV simulation. Invalid source " +
                    "configuration : " + sourceConfig.toString());
        }
        if (!checkAvailability(sourceConfig, EventSimulatorConstants.EXECUTION_PLAN_NAME)) {
            throw new InvalidConfigException("Execution plan name is required for CSV simulation of stream '" +
                    sourceConfig.getString(EventSimulatorConstants.STREAM_NAME) + "'. Invalid source " +
                    "configuration : " + sourceConfig.toString());
        }
        if (!checkAvailability(sourceConfig, EventSimulatorConstants.FILE_NAME)) {
            throw new InvalidConfigException("File name is required for CSV simulation of stream '" +
                    sourceConfig.getString(EventSimulatorConstants.STREAM_NAME) + "'. Invalid source " +
                    "configuration : " + sourceConfig.toString());
        }
        /**
         * either a timestamp attribute must be specified or the timeInterval between timestamps of 2 consecutive
         * events must be specified.
         * if time interval is specified the timestamp of the first event will be the timestampStartTime and
         * consecutive event will have timestamp = last timestamp + time interval
         * if both timestamp attribute and time interval are not specified set time interval to 1 second
         * */
        String timestampAttribute = "-1";
        long timeInterval = -1;
        /**
         * since the isOrdered timestamp will be retrieved only if the timestamp attribute is specified, set the
         * isOrdered = true, since if the CSV file doesnt have a timestamp attribute, then the default value of the
         * flag will be used and it implies that there will be no need to order the records prior to starting event
         * simulation
         * */
        boolean isOrdered = true;
        if (checkAvailability(sourceConfig, EventSimulatorConstants.TIMESTAMP_ATTRIBUTE)) {
            timestampAttribute = sourceConfig.getString(EventSimulatorConstants.TIMESTAMP_ATTRIBUTE);
            /**
             * check for the availability of the flag isOrdered only if the timestampAttribute is specified since
             * isOrdered flag indicates whether a csv file is ordered by the timestamp attribute or not
             * */
            if (sourceConfig.has(EventSimulatorConstants.IS_ORDERED)
                    && !sourceConfig.isNull(EventSimulatorConstants.IS_ORDERED)) {
                isOrdered = sourceConfig.getBoolean(EventSimulatorConstants.IS_ORDERED);
            } else {
                throw new InvalidConfigException("isOrdered flag is required for CSV simulation of stream '" +
                        sourceConfig.getString(EventSimulatorConstants.STREAM_NAME) + "'. Invalid source " +
                        "configuration : " + sourceConfig.toString());
            }
        } else if (checkAvailability(sourceConfig, EventSimulatorConstants.TIME_INTERVAL)) {
            timeInterval = sourceConfig.getLong(EventSimulatorConstants.TIME_INTERVAL);
            if (timeInterval < 0) {
                throw new InvalidConfigException("Time interval for CSV simulation of stream '" +
                        sourceConfig.getString(EventSimulatorConstants.STREAM_NAME) + "' must be positive. " +
                        "Invalid source configuration : " + sourceConfig.toString());
            }
        } else {
            log.warn("Either timestamp end time or time interval is required for CSV simulation of stream '" +
                    sourceConfig.getString(EventSimulatorConstants.STREAM_NAME) + "'. Time interval will " +
                    "be set to 1 second for source configuration : " + sourceConfig.toString());
            timeInterval = 1000;
        }
        if (!checkAvailability(sourceConfig, EventSimulatorConstants.DELIMITER)) {
            throw new InvalidConfigException("Delimiter is required for CSV simulation of stream '" +
                    sourceConfig.getString(EventSimulatorConstants.STREAM_NAME) + "'. Invalid source " +
                    "configuration : " + sourceConfig.toString());
        }
        /**
         * check whether the CSV file has been uploaded.
         * if yes, assign the fileInfo to the csvConfig property
         * else, throw an exception to indicate that the file has not been uploaded
         *
         */
        if (!FileStore.getFileStore().checkExists(sourceConfig.getString(EventSimulatorConstants.FILE_NAME))) {
            throw new InvalidConfigException("File '" + sourceConfig.getString(EventSimulatorConstants.FILE_NAME)
                    + "' required for simulation of stream '" +
                    sourceConfig.getString(EventSimulatorConstants.STREAM_NAME) + "' has not been " +
                    "uploaded.");
        }
//        create CSVSimulationDTO containing csv simulation configuration
        CSVSimulationDTO csvSimulationConfig = new CSVSimulationDTO();
        csvSimulationConfig.setStreamName(sourceConfig.getString(EventSimulatorConstants.STREAM_NAME));
        csvSimulationConfig.setExecutionPlanName(sourceConfig.getString(EventSimulatorConstants.EXECUTION_PLAN_NAME));
        csvSimulationConfig.setFileName(sourceConfig.getString(EventSimulatorConstants.FILE_NAME));
        csvSimulationConfig.setTimestampAttribute(timestampAttribute);
        csvSimulationConfig.setTimeInterval(timeInterval);
        csvSimulationConfig.setDelimiter((String) sourceConfig.get(EventSimulatorConstants.DELIMITER));
        csvSimulationConfig.setIsOrdered(isOrdered);
        return csvSimulationConfig;
    }
}
