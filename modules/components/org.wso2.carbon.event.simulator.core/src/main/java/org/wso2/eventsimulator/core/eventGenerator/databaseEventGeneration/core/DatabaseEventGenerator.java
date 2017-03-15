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

package org.wso2.eventsimulator.core.eventGenerator.databaseEventGeneration.core;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.eventsimulator.core.eventGenerator.EventGenerator;
import org.wso2.eventsimulator.core.eventGenerator.bean.DatabaseFeedSimulationDto;
import org.wso2.eventsimulator.core.eventGenerator.databaseEventGeneration.util.DatabaseConnection;
import org.wso2.eventsimulator.core.eventGenerator.util.EventConverter;
import org.wso2.eventsimulator.core.eventGenerator.util.StreamConfigurationParser;
import org.wso2.eventsimulator.core.eventGenerator.util.exceptions.EventGenerationException;
import org.wso2.eventsimulator.core.internal.EventSimulatorDataHolder;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * DatabaseEventGenerator class is used to generate events from a database
 */
public class DatabaseEventGenerator implements EventGenerator {
    private static final Logger log = LoggerFactory.getLogger(DatabaseEventGenerator.class);
    private Long timestampStartTime;
    private Long timestampEndTime;
    private DatabaseFeedSimulationDto databaseFeedConfiguration;
    private Event nextEvent = null;
    private ResultSet resultSet;
    private DatabaseConnection databaseConnection;
    private List<Attribute> streamAttributes;
    private List<String> columnNames;

    public DatabaseEventGenerator() {
    }

    /**
     * init() methods initializes database event generator
     *
     * @param streamConfiguration JSON object containing configuration for database event generation
     */
    @Override
    public void init(JSONObject streamConfiguration) {
        if (log.isDebugEnabled()) {
            log.debug("Initialize database generator for stream '" + databaseFeedConfiguration.getStreamName() + "'");
        }
        try {
            databaseFeedConfiguration = StreamConfigurationParser.databaseFeedSimulationParser(streamConfiguration);

//        retrieve the stream definition
            streamAttributes = EventSimulatorDataHolder.getInstance().getEventStreamService()
                    .getStreamAttributes(databaseFeedConfiguration.getExecutionPlanName(),
                            databaseFeedConfiguration.getStreamName());
            columnNames = databaseFeedConfiguration.getColumnNames();
//        validate column names list
            boolean valid = columnValidation();

            if (valid) {
                databaseConnection = new DatabaseConnection(databaseFeedConfiguration);
                databaseConnection.connectToDatabase();
            }
        } catch (EventGenerationException e) {
            log.error("Error occurred when initializing database event generator for stream '" +
                    databaseFeedConfiguration.getStreamName() + "' :" + e.getMessage(), e);
        }
    }


    /**
     * start() method is used to retrieve the resultSet from the data source and to obtain the first event
     */
    @Override
    public void start() {
        if (log.isDebugEnabled()) {
            log.debug("Start database generator for stream '" + databaseFeedConfiguration.getStreamName() + "'");
        }
        try {
            resultSet = databaseConnection.getDatabaseEventItems(timestampStartTime, timestampEndTime);

            if (!resultSet.isBeforeFirst()) {
                log.error(" Table " + databaseFeedConfiguration.getTableName() + " contains " +
                        " no entries for the columns specified.");
            }
            getNextEvent();
        } catch (SQLException e) {
            log.error("Error occurred when retrieving resultset : " + e.getMessage(), e);
        }
    }


    /**
     * stop() method is used to close database resources held by the database event generator
     */
    @Override
    public void stop() {
        if (log.isDebugEnabled()) {
            log.debug("Stop database generator for stream '" + databaseFeedConfiguration.getStreamName() + "'");
        }
        if (databaseConnection != null) {
            databaseConnection.closeConnection();
        }
    }


    /**
     * poll() method is used to retrieve the nextEvent of generator and assign the next event of with least timestamp
     * as nextEvent
     *
     * @return nextEvent
     */
    @Override
    public Event poll() {
        if (log.isDebugEnabled()) {
            log.debug("Poll next event from database generator to stream '" + databaseFeedConfiguration.getStreamName()
                    + "'");
        }

        /*
        * if nextEvent is not null, it implies that more events may be generated by the generator. Hence call
        * getNExtEvent(0 method to assign the next event with least timestamp as nextEvent.
        * else if nextEvent == null, it implies that generator will not generate any more events. Hence return null.
        * */
        Event tempEvent = nextEvent;
        if (tempEvent != null) {
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
        if (log.isDebugEnabled()) {
            log.debug("Peek next event from database generator to stream '" + databaseFeedConfiguration.getStreamName()
                    + "'");
        }
        return nextEvent;
    }


    /**
     * getNextEvent() method is used to get the next event with least timestamp
     */
    @Override
    public void getNextEvent() {

        if (log.isDebugEnabled()) {
            log.debug("get next event of database generator for stream '" + databaseFeedConfiguration.getStreamName()
                    + "'");
        }

        try {
            /*
            * if the resultset has a next entry, create an event using that entry and assign it to nextEvent
            * else, assign null to nextEvent
            * */
            if (resultSet.next() || resultSet.isBeforeFirst()) {
                Object[] attributeValues = new Object[streamAttributes.size()];
                Long timestamp = resultSet.getLong(databaseFeedConfiguration.getTimestampAttribute());

                int i = 0;

                /*
                * For each attribute in streamAttributes, use attribute type to determine the getter method to be
                * used to access the resultset and use the attribute name to access a particular field in resultset
                * */
                for (Attribute attribute : streamAttributes) {
                    switch (attribute.getType()) {
                        case STRING:
                            attributeValues[i] = resultSet.getString(columnNames.get(i));
                            break;
                        case INT:
                            attributeValues[i] = resultSet.getInt(columnNames.get(i));
                            break;
                        case DOUBLE:
                            attributeValues[i] = resultSet.getDouble(columnNames.get(i));
                            break;
                        case FLOAT:
                            attributeValues[i] = resultSet.getFloat(columnNames.get(i));
                            break;
                        case BOOL:
                            attributeValues[i] = resultSet.getBoolean(columnNames.get(i));
                            break;
                        case LONG:
                            attributeValues[i] = resultSet.getLong(columnNames.get(i));
                            break;
                    }
                    i++;
                }
                nextEvent = EventConverter.eventConverter(streamAttributes, attributeValues, timestamp);
            } else {
                nextEvent = null;
            }
        } catch (SQLException e) {
            log.error("Error occurred when accessing result set : " + e.getMessage(), e);
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
        this.timestampStartTime = timestampStartTime;
        this.timestampEndTime = timestampEndTime;

        if (log.isDebugEnabled()) {
            log.debug("Timestamp range initiated for random event generator for stream '" +
                    databaseFeedConfiguration.getStreamName() + "'. Timestamp start time : " + timestampStartTime + " and" +
                    " timestamp end time : " + timestampEndTime);
        }
    }

    /**
     * columnValidation method is used to validate the column names provided.
     * It performs the following validations
     * 1. The column names are not null or empty
     * 2. The number of columns provided is equal to the number of attributes in the stream
     * 3. Each attribute in stream has a matching column name
     *
     * @return true if columns are valid
     */
    private boolean columnValidation() {

        if (log.isDebugEnabled()) {
            log.debug("Column validation for stream '" + databaseFeedConfiguration.getStreamName() + "'");
        }

        if (columnNames.contains(null) || columnNames.contains("")) {
            throw new EventGenerationException(" Column names cannot contain null values or empty strings");
        }

        if (columnNames.size() != streamAttributes.size() + 1) {
            throw new EventGenerationException("Simulation of stream '" + databaseFeedConfiguration.getStreamName() +
                    "' requires " + (streamAttributes.size() + 1) + " attributes. Number of columns specified is "
                    + columnNames.size());
        }

        boolean columnAvailable;

        for (Attribute attribute : streamAttributes) {
            columnAvailable = false;
            for (String column : columnNames) {
                if (attribute.getName().compareToIgnoreCase(column) == 0) {
                    columnAvailable = true;
                    break;
                }
            }
            if (!columnAvailable) {
                throw new EventGenerationException("Column required for attribute '" + attribute.getName() +
                        "' in stream '" + databaseFeedConfiguration.getStreamName() + "'.");
            }
        }
        return true;
    }

    /**
     * getStreamName() method returns the name of the stream to which events are generated
     *
     * @return stream name
     */
    @Override
    public String getStreamName() {
        return databaseFeedConfiguration.getStreamName();
    }


    /**
     * getExecutionPlanName() method returns the name of the execution plan to which events are generated
     *
     * @return execution plan name
     */
    @Override
    public String getExecutionPlanName() {
        return databaseFeedConfiguration.getExecutionPlanName();
    }
}
