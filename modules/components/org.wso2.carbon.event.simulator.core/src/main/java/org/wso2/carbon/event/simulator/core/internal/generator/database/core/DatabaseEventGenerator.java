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

package org.wso2.carbon.event.simulator.core.internal.generator.database.core;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.event.simulator.core.exception.EventGenerationException;
import org.wso2.carbon.event.simulator.core.exception.InsufficientAttributesException;
import org.wso2.carbon.event.simulator.core.exception.InvalidConfigException;
import org.wso2.carbon.event.simulator.core.exception.SimulatorInitializationException;
import org.wso2.carbon.event.simulator.core.internal.bean.DBSimulationDTO;
import org.wso2.carbon.event.simulator.core.internal.generator.EventGenerator;
import org.wso2.carbon.event.simulator.core.internal.generator.database.util.DatabaseConnector;
import org.wso2.carbon.event.simulator.core.internal.util.CommonOperations;
import org.wso2.carbon.event.simulator.core.internal.util.EventConverter;
import org.wso2.carbon.event.simulator.core.internal.util.EventSimulatorConstants;
import org.wso2.carbon.event.simulator.core.service.EventSimulatorDataHolder;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.query.api.definition.Attribute;

import static org.wso2.carbon.event.simulator.core.internal.util.CommonOperations.checkAvailability;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * DatabaseEventGenerator class is used to generate events from a database
 */
public class DatabaseEventGenerator implements EventGenerator {
    private static final Logger log = LoggerFactory.getLogger(DatabaseEventGenerator.class);
    private Long timestampStartTime;
    private Long timestampEndTime;
    private DBSimulationDTO dbSimulationConfig;
    private Event nextEvent = null;
    private ResultSet resultSet;
    private DatabaseConnector databaseConnection;
    private List<Attribute> streamAttributes;
    private List<String> columnNames;

    public DatabaseEventGenerator() {
    }

    /**
     * init() methods initializes database event generator
     *
     * @param streamConfiguration JSON object containing configuration for database event generation
     * @throws InsufficientAttributesException if the number of columns specified is not equal to the number of
     *                                         stream attributes
     */
    @Override
    public void init(JSONObject streamConfiguration) throws InvalidConfigException, InsufficientAttributesException {

        dbSimulationConfig = validateDBConfiguration(streamConfiguration);
//        retrieve the stream definition
        streamAttributes = EventSimulatorDataHolder.getInstance().getEventStreamService()
                .getStreamAttributes(dbSimulationConfig.getExecutionPlanName(),
                        dbSimulationConfig.getStreamName());

        if (streamAttributes != null) {
            columnNames = dbSimulationConfig.getColumnNames();

            if (columnNames == null) {
                columnNames = new ArrayList<>();
                streamAttributes.forEach(attribute -> columnNames.add(attribute.getName()));
                dbSimulationConfig.setColumnNames(columnNames);
            }

            /*
            * check whether the number of columns specified is the number of stream attributes
            * if yes, establish a database connection
            * else, throw an exception
            * */
            if (CommonOperations.checkAttributes(columnNames.size(), streamAttributes.size())) {
                databaseConnection = new DatabaseConnector();
                databaseConnection.connectToDatabase(dbSimulationConfig.getDataSourceLocation(),
                        dbSimulationConfig.getUsername(), dbSimulationConfig.getPassword());
            } else {
                throw new InsufficientAttributesException("Simulation of stream '" +
                        dbSimulationConfig.getStreamName() + "' requires " + streamAttributes.size() + " " +
                        "attributes. Number of columns specified is " + columnNames.size());
            }

            if (log.isDebugEnabled()) {
                log.debug("Validate columns names list and Initialize database generator to simulate stream '" +
                        dbSimulationConfig.getStreamName() + "'");
            }
        } else {
            throw new SimulatorInitializationException("Error occurred when initializing database event "
                    + "generator to simulate stream '" + dbSimulationConfig.getStreamName()
                    + "'. Execution plan '" + dbSimulationConfig.getExecutionPlanName() +
                    "' has not been deployed.");
        }
    }

    /**
     * start() method is used to retrieve the resultSet from the data source and to obtain the first event
     */
    @Override
    public void start() {

        try {
            resultSet = databaseConnection.getDatabaseEventItems(dbSimulationConfig.getTableName(),
                    dbSimulationConfig.getColumnNames(), dbSimulationConfig.getTimestampAttribute(),
                    timestampStartTime, timestampEndTime);

            if (resultSet != null && !resultSet.isBeforeFirst()) {
                throw new EventGenerationException("Table " + dbSimulationConfig.getTableName() + " contains " +
                        " no entries for the columns specified.");
            }
            if (log.isDebugEnabled() && resultSet != null) {
                log.debug("Retrieved resultset to simulate stream '" + dbSimulationConfig.getStreamName() +
                        "'");
            }
            getNextEvent();
        } catch (SQLException e) {
            throw new EventGenerationException("Error occurred when retrieving resultset from database ' " +
                    dbSimulationConfig.getDataSourceLocation() + "' to simulate to simulate stream '" +
                    dbSimulationConfig.getStreamName() + "' :", e);
        }

        if (log.isDebugEnabled()) {
            log.debug("Start database generator for stream '" + dbSimulationConfig.getStreamName() + "'");
        }
    }

    /**
     * stop() method is used to close database resources held by the database event generator
     */
    @Override
    public void stop() {
        if (databaseConnection != null) {
            databaseConnection.closeConnection();
        }
        if (log.isDebugEnabled()) {
            log.debug("Stop database generator for stream '" + dbSimulationConfig.getStreamName() + "'");
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
        return nextEvent;
    }

    /**
     * getNextEvent() method is used to get the next event with least timestamp
     */
    @Override
    public void getNextEvent() {
        try {
            /*
            * if the resultset has a next entry, create an event using that entry and assign it to nextEvent
            * else, assign null to nextEvent
            * */
            if (resultSet != null) {
                if (resultSet.next() || resultSet.isBeforeFirst()) {
                    Object[] attributeValues = new Object[streamAttributes.size()];
                    Long timestamp = resultSet.getLong(dbSimulationConfig.getTimestampAttribute());

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
                            default:
                                throw new EventGenerationException("Invalid attribute type '" + attribute.getType() +
                                        "'. Attribute type must be either STRING, INT, DOUBLE, FLOAT, BOOL or LONG");
                        }
                        i++;
                    }
                    nextEvent = EventConverter.eventConverter(streamAttributes, attributeValues, timestamp);
                } else {
                    nextEvent = null;
                }
            }
        } catch (SQLException e) {
            throw new EventGenerationException("Error occurred when accessing result set to simulate to simulate " +
                    "stream '" + dbSimulationConfig.getStreamName() + "' :", e);
        } catch (EventGenerationException e) {
            log.error("Drop even and create next event. Error occurred when generating event using database event " +
                    "generator to simulate stream '" + dbSimulationConfig.getStreamName() + "' : ", e);
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
        this.timestampStartTime = timestampStartTime;
        this.timestampEndTime = timestampEndTime;

        if (log.isDebugEnabled()) {
            log.debug("Timestamp range initiated for random event generator for stream '" +
                    dbSimulationConfig.getStreamName() + "'. Timestamp start time : " + timestampStartTime +
                    " and timestamp end time : " + timestampEndTime);
        }
    }

    /**
     * getStreamName() method returns the name of the stream to which events are generated
     *
     * @return stream name
     */
    @Override
    public String getStreamName() {
        return dbSimulationConfig.getStreamName();
    }

    /**
     * getExecutionPlanName() method returns the name of the execution plan to which events are generated
     *
     * @return execution plan name
     */
    @Override
    public String getExecutionPlanName() {
        return dbSimulationConfig.getExecutionPlanName();
    }

    /**
     * validateDBConfiguration() method parses the database simulation configuration into a DBSimulationDTO object
     *
     * @param streamConfig JSON object containing configuration required to simulate stream
     * @return DBSimulationDTO containing database simulation configuration
     * @throws InvalidConfigException if the stream configuration is invalid
     */
    private DBSimulationDTO validateDBConfiguration(JSONObject streamConfig) throws InvalidConfigException {
             /*
             * set properties to DBSimulationDTO.
             *
             * Perform the following checks prior to setting the properties.
             * 1. has
             * 2. isNull
             * 3. isEmpty
             *
             * if any of the above checks fail, throw an exception indicating which property is missing.
             * */

        if (!checkAvailability(streamConfig, EventSimulatorConstants.DATA_SOURCE_LOCATION)) {
            throw new InvalidConfigException("Data source location is required for database simulation. Invalid " +
                    "stream configuration : " + streamConfig.toString());
        }

        if (!checkAvailability(streamConfig, EventSimulatorConstants.USER_NAME)) {
            throw new InvalidConfigException("Username is required for database simulation. Invalid " +
                    "stream configuration : " + streamConfig.toString());
        }

        if (!checkAvailability(streamConfig, EventSimulatorConstants.PASSWORD)) {
            throw new InvalidConfigException("Password is required for database simulation. Invalid " +
                    "stream configuration : " + streamConfig.toString());
        }

        if (!checkAvailability(streamConfig, EventSimulatorConstants.TABLE_NAME)) {
            throw new InvalidConfigException("Table name is required for database simulation. Invalid " +
                    "stream configuration : " + streamConfig.toString());
        }

        if (!checkAvailability(streamConfig, EventSimulatorConstants.STREAM_NAME)) {
            throw new InvalidConfigException("Stream name is required for database simulation. Invalid " +
                    "stream configuration : " + streamConfig.toString());
        }

        if (!checkAvailability(streamConfig, EventSimulatorConstants.EXECUTION_PLAN_NAME)) {
            throw new InvalidConfigException("Execution plan name is required for database simulation. Invalid " +
                    "stream configuration : " + streamConfig.toString());
        }

        if (!checkAvailability(streamConfig, EventSimulatorConstants.TIMESTAMP_ATTRIBUTE)) {
            throw new InvalidConfigException("Timestamp attribute is required for database simulation. Invalid " +
                    "stream configuration : " + streamConfig.toString());
        }

      /*
      * insert the specified column names into a list and set it to database configuration
      * check whether the stream configuration has columnNames element
      * if not, throw an exception
      * else, check whether the column names are null. this is inferred as user implying that he wants to take
      * all the columns in the database and the column names are identical to the stream attribute names
      * if the column names are not null but is empty throw an exception
      * else convert the string of comma seperated column names into an arraylist
      * if the arraylist does not contain any empty strings, set property columnNames
      * */

        List<String> columns;

        if (!streamConfig.has(EventSimulatorConstants.COLUMN_NAMES_LIST)) {
            throw new InvalidConfigException("Column names list is required for database simulation. Invalid " +
                    "stream configuration : " + streamConfig.toString());
        } else {
            if (streamConfig.isNull(EventSimulatorConstants.COLUMN_NAMES_LIST)) {
                columns = null;
            } else {
                if (streamConfig.getString(EventSimulatorConstants.COLUMN_NAMES_LIST).isEmpty()) {
                    throw new InvalidConfigException("Column names list is required for database simulation. Invalid " +
                            "stream configuration : " + streamConfig.toString());
                } else {
                    columns = getColumnsList(streamConfig
                            .getString(EventSimulatorConstants.COLUMN_NAMES_LIST));
                }
            }
        }

        DBSimulationDTO dbSimulationDTO = new DBSimulationDTO();
        dbSimulationDTO.setDataSourceLocation(streamConfig.getString(EventSimulatorConstants.DATA_SOURCE_LOCATION));
        dbSimulationDTO.setUsername(streamConfig.getString(EventSimulatorConstants.USER_NAME));
        dbSimulationDTO.setPassword(streamConfig.getString(EventSimulatorConstants.PASSWORD));
        dbSimulationDTO.setTableName(streamConfig.getString(EventSimulatorConstants.TABLE_NAME));
        dbSimulationDTO.setStreamName(streamConfig.getString(EventSimulatorConstants.STREAM_NAME));
        dbSimulationDTO.setExecutionPlanName(streamConfig.getString(EventSimulatorConstants.EXECUTION_PLAN_NAME));
        dbSimulationDTO.setTimestampAttribute(streamConfig.getString(EventSimulatorConstants.TIMESTAMP_ATTRIBUTE));
        dbSimulationDTO.setColumnNames(columns);
        return dbSimulationDTO;
    }


    /**
     * getColumnsList() validates that the list of column names provided does not contain empty strings
     *
     * @param columnNames a comma separated string of column names
     * @return a list of column names if it does not contain empty strings, else throw an exception
     * @throws InvalidConfigException if the simulation configuration contains invalid data
     */
    private static List<String> getColumnsList(String columnNames) throws InvalidConfigException {
        /*
        * convert the column names in to an array list
        * check whether the column names contain empty string or null values.
        * if yes, throw an exception
        * else, set to the columnNames list
        * */
        List<String> columns = Arrays.asList(columnNames.split("\\s*,\\s*"));

        if (columns.contains("")) {
            throw new InvalidConfigException("Column name cannot contain empty values");
        }
        return columns;
    }
}
