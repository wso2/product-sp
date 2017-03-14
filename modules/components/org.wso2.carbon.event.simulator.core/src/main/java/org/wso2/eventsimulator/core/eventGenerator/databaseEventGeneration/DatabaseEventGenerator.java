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

package org.wso2.eventsimulator.core.eventGenerator.databaseEventGeneration;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.eventsimulator.core.eventGenerator.EventGenerator;
import org.wso2.eventsimulator.core.internal.EventSimulatorDataHolder;
import org.wso2.eventsimulator.core.simulator.databaseFeedSimulation.DatabaseFeedSimulationDto;
import org.wso2.eventsimulator.core.simulator.databaseFeedSimulation.util.DatabaseConnection;
import org.wso2.eventsimulator.core.simulator.exception.EventSimulationException;
import org.wso2.eventsimulator.core.util.EventConverter;
import org.wso2.eventsimulator.core.util.EventSimulatorParser;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DatabaseEventGenerator implements EventGenerator {
    private static final Logger log = LoggerFactory.getLogger(DatabaseEventGenerator.class);
    private Long timestampStartTime;
    private Long timestampEndTime;
    private DatabaseFeedSimulationDto databaseFeedConfiguration;
    private Event nextEvent = null;
    private ResultSet resultSet;
    private DatabaseConnection databaseConnection;
    private List<Attribute> streamAttributes;

    public DatabaseEventGenerator() {}

    @Override
    public void init(JSONObject streamConfiguration) {
        databaseFeedConfiguration = EventSimulatorParser.databaseFeedSimulationParser(streamConfiguration);

//        retrieve the stream definition
        streamAttributes = EventSimulatorDataHolder.getInstance().getEventStreamService()
                .getStreamAttributes(databaseFeedConfiguration.getExecutionPlanName(),databaseFeedConfiguration.getStreamName());
//        validate column names list
        boolean valid = columnValidation(databaseFeedConfiguration.getColumnNames(), streamAttributes);

        if (valid) {
            databaseConnection = new DatabaseConnection(databaseFeedConfiguration);
            databaseConnection.connectToDatabase();
        }
    }

    @Override
    public void start() {
        try {
            resultSet = databaseConnection.getDatabaseEventItems(timestampStartTime,timestampEndTime);

            if (!resultSet.isBeforeFirst()) {
                throw new EventSimulationException(" Table " + databaseFeedConfiguration.getTableName() + " contains " +
                        " no entries for the columns specified.");
            }
            getNextEvent();
        } catch (SQLException e) {
            log.error("Error occurred when generating event : " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        if (databaseConnection != null) {
            databaseConnection.closeConnection();
        }
    }

    @Override
    public Event poll() {
        Event tempEvent = nextEvent;
        if (tempEvent != null) {
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
        try {
            if (resultSet.next() || resultSet.isBeforeFirst() ) {
                Object[] attributeValues = new Object[streamAttributes.size()];
                Long timestamp = resultSet.getLong(databaseFeedConfiguration.getTimestampAttribute());

                int i = 0;
//                todo remove the while?
                while (i < streamAttributes.size()) {
                    for (Attribute attribute : streamAttributes) {
                        switch (attribute.getType()) {
                            case STRING:
                                attributeValues[i] = resultSet.getString(attribute.getName());
                                break;
                            case INT:
                                attributeValues[i] = resultSet.getInt(attribute.getName());
                                break;
                            case DOUBLE:
                                attributeValues[i] = resultSet.getDouble(attribute.getName());
                                break;
                            case FLOAT:
                                attributeValues[i] = resultSet.getFloat(attribute.getName());
                                break;
                            case BOOL:
                                attributeValues[i] = resultSet.getBoolean(attribute.getName());
                                break;
                            case LONG:
                                attributeValues[i] = resultSet.getLong(attribute.getName());
                                break;
                        }
                        i++;
                    }
                }
                nextEvent = EventConverter.eventConverter(streamAttributes, attributeValues, timestamp);
            } else {
                nextEvent = null;
            }
        } catch (SQLException e) {
            log.error("Error occurred when accessing result set : " + e.getMessage());
        }
    }

/*    @Override
    public Long getNextTimestamp() {
        return peek().getTimestamp();
    }*/

    @Override
    public void initTimestamp(Long timestampStartTime, Long timestampEndTime) {
        this.timestampStartTime = timestampStartTime;
        this.timestampEndTime = timestampEndTime;
        log.info("Timestamp range initiated. Start time : " + timestampStartTime + " and end time : " + timestampEndTime );
    }

    /**
     * columnValidation method is used to validate the column names provided.
     * It performs the following validations
     * 1. The column names are not null or empty
     * 2. The number of columns provided is equal to the number of attributes in the stream
     * 3. Each attribute has a matching column name
     */

    private boolean columnValidation(List<String> columnNames, List<Attribute> streamAttributes) {

        if (columnNames.contains(null) || columnNames.contains("")) {
            throw new EventSimulationException(" Column names cannot contain null values or empty strings");
        }

        if (columnNames.size() != streamAttributes.size() + 1) {
            throw new EventSimulationException(" Stream requires " + (streamAttributes.size()+1) + " attributes. Number of columns " +
                    " specified is " + columnNames.size());
        }

        boolean columnAvailable;

        for (int i = 0; i < streamAttributes.size(); i++) {
            columnAvailable = false;
            for (int j = 0; j < columnNames.size(); j++) {
                if (streamAttributes.get(i).getName().compareToIgnoreCase(columnNames.get(j)) == 0) {
                    columnAvailable = true;
                    break;
                }
            }
            if (columnAvailable) {
                continue;
            } else {
                log.error("Column required for attribute : " + streamAttributes.get(i).getName());
                return false;
            }
        }
        return true;
    }

    @Override
    public String getStreamName() {
        return databaseFeedConfiguration.getStreamName();
    }

    @Override
    public String getExecutionPlanName() {
        return databaseFeedConfiguration.getExecutionPlanName();
    }
}
