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

package org.wso2.eventsimulator.core.simulator.databaseFeedSimulation.core;

import org.apache.log4j.Logger;
import org.wso2.carbon.stream.processor.core.StreamDefinitionRetriever;
import org.wso2.siddhi.core.event.Event;
import org.wso2.eventsimulator.core.internal.EventSimulatorDataHolder;
import org.wso2.eventsimulator.core.simulator.EventSimulator;
import org.wso2.eventsimulator.core.simulator.bean.FeedSimulationStreamConfiguration;
import org.wso2.eventsimulator.core.simulator.databaseFeedSimulation.DatabaseFeedSimulationDto;
import org.wso2.eventsimulator.core.simulator.databaseFeedSimulation.util.DatabaseConnection;
import org.wso2.eventsimulator.core.simulator.exception.EventSimulationException;
import org.wso2.eventsimulator.core.util.EventConverter;
import org.wso2.eventsimulator.core.util.EventSender;
import org.wso2.eventsimulator.core.util.QueuedEvent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * This simulator implements EventSimulator Interface. It creates events from data in a database.
 */
public class DatabaseFeedSimulator implements EventSimulator {
    private static final Logger log = Logger.getLogger(DatabaseFeedSimulator.class);
    private final Object lock = new Object();
    private DatabaseFeedSimulationDto streamConfiguration;
    private volatile boolean isPaused = false;
    private volatile boolean isStopped = false;

    /**
     * Initialize DatabaseFeedSimulator to start simulation
     *
     * @param streamConfiguration : DatabaseFeedSimulationDto object containing database simulation configuration
     */
    public DatabaseFeedSimulator(DatabaseFeedSimulationDto streamConfiguration) {
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

    @Override
    public void run() {
        sendEvent(streamConfiguration);
    }

    /**
     * Create and send events using data in database
     *
     * @param databaseFeedConfiguration : DatabaseFeedSimulationDto object containing database simulation configuration
     */

    private void sendEvent(DatabaseFeedSimulationDto databaseFeedConfiguration) {
        int delay = databaseFeedConfiguration.getDelay();
        LinkedHashMap<String,StreamDefinitionRetriever.Type> streamDefinition = EventSimulatorDataHolder
                .getInstance().getStreamDefinitionService().streamDefinitionService(databaseFeedConfiguration.getStreamName());
        ArrayList<String> streamAttributeNames = new ArrayList<String>(streamDefinition.keySet());
        boolean valid = columnValidation(databaseFeedConfiguration.getColumnNames(), streamAttributeNames);

        if (valid) {
            DatabaseConnection databaseConnection;
            ResultSet resultSet;
            databaseConnection = new DatabaseConnection();

            try {
                resultSet = databaseConnection.getDatabaseEventItems(databaseFeedConfiguration);

                if (!resultSet.isBeforeFirst()) {
                    throw new EventSimulationException(" Table " + databaseFeedConfiguration.getTableName() + " contains " +
                            " no entries for the columns specified.");
                }

                while (resultSet.next()) {
                    if (!isPaused) {
                        String[] attributeValues = new String[streamDefinition.size()];

                        /*
                        create an event for each row of the result set. Attribute values are generated for each entry in StreamDefinition entry set.
                        The getter method to be used to retrieve the attribute value from the resultset row will be decided by the entry.value(attribute type).
                        The attribute value will be accessed using the entry.key (attribute name).
                        for each resultset row, integer i would increment from 0 to StreamDefinition.size(). This variable i will be used to insert attribute values
                        into the string array in the order in which attributes are specified in the StreamDefinition.
                        */
                        int i =0;

                            for (Map.Entry<String,StreamDefinitionRetriever.Type> entry : streamDefinition.entrySet()) {
                                while (i < streamDefinition.size()) {
                                    if ((entry.getValue().compareTo(StreamDefinitionRetriever.Type.STRING)) == 0) {
                                        attributeValues[i] = resultSet.getString(entry.getKey());
                                    } else if ((entry.getValue().compareTo(StreamDefinitionRetriever.Type.INTEGER)) == 0) {
                                        attributeValues[i] = String.valueOf(resultSet.getInt(entry.getKey()));
                                    } else if ((entry.getValue().compareTo(StreamDefinitionRetriever.Type.DOUBLE)) == 0) {
                                        attributeValues[i] = String.valueOf(resultSet.getDouble(entry.getKey()));
                                    } else if ((entry.getValue().compareTo(StreamDefinitionRetriever.Type.FLOAT)) == 0) {
                                        attributeValues[i] = String.valueOf(resultSet.getFloat(entry.getKey()));
                                    } else if ((entry.getValue().compareTo(StreamDefinitionRetriever.Type.BOOLEAN)) == 0) {
                                        attributeValues[i] = String.valueOf(resultSet.getBoolean(entry.getKey()));
                                    } else if ((entry.getValue().compareTo(StreamDefinitionRetriever.Type.LONG)) == 0) {
                                        attributeValues[i] = String.valueOf(resultSet.getLong(entry.getKey()));
                                    }
                                    break;
                                }
                                i ++;
                            }

                        Event event = EventConverter.eventConverter(streamDefinition, attributeValues);

                        System.out.println("Input Event (Database feed)" + Arrays.deepToString(event.getData()));

//                        if orderByTimestamp flag is set to true, the events will be sent to via a queue, else events will be directly sent.
                        if (databaseFeedConfiguration.getTimestampAttribute().isEmpty()) {
                            EventSender.getInstance().sendEvent(databaseFeedConfiguration.getExecutionPlanName(),databaseFeedConfiguration.getStreamName(),event);
                        } else {
                            EventSender.getInstance().sendEvent(databaseFeedConfiguration.getExecutionPlanName(),databaseFeedConfiguration.getStreamName(),new QueuedEvent(resultSet.getLong
                                    (databaseFeedConfiguration.getTimestampAttribute()),event));
                        }
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
            } catch (SQLException e) {
                log.error("Error occurred when generating event : " + e.getMessage());
            } catch (InterruptedException e) {
                log.error("Error occurred when sending event : " + e.getMessage());
            } finally {
                if (databaseConnection != null) {
                    databaseConnection.closeConnection();
                }
            }
        }
    }


    /**
     * columnValidation method is used to validate the column names provided.
     * It performs the following validations
     * 1. The column names are not null or empty
     * 2. The number of columns provided is equal to the number of attributes in the stream
     * 3. Each attribute has a matching column name
     */

    private boolean columnValidation(List<String> columnNames, List<String> streamAttributeNames) {

        if (columnNames.contains(null) || columnNames.contains("")) {
            throw new EventSimulationException(" Column names cannot contain null values or empty strings");
        }

        if (columnNames.size() != streamAttributeNames.size()) {
            throw new EventSimulationException(" Stream requires " + streamAttributeNames.size() + " attributes. Number of columns " +
                    " specified is " + columnNames.size());
        }

        boolean columnAvailable;

        for (int i = 0; i < streamAttributeNames.size(); i++) {
            columnAvailable = false;
            for (int j = 0; j < columnNames.size(); j++) {
                if ((String.valueOf(streamAttributeNames.get(i))).compareToIgnoreCase(String.valueOf(columnNames.get(j))) == 0) {
                    columnAvailable = true;
                    break;
                }
            }
            if (columnAvailable) {
                continue;
            } else {
                log.error("Column required for attribute : " + streamAttributeNames.get(i));
                return false;
            }
        }
        return true;
    }
}
