package org.wso2.eventsimulator.core.simulator.databaseFeedSimulation.core;


import org.apache.log4j.Logger;
import org.wso2.siddhi.core.event.Event;
import org.wso2.eventsimulator.core.internal.EventSimulatorDataHolder;
//import org.wso2.eventsimulator.core.util.Event;
import org.wso2.eventsimulator.core.simulator.EventSimulator;
import org.wso2.eventsimulator.core.simulator.bean.FeedSimulationStreamConfiguration;
import org.wso2.eventsimulator.core.util.EventSimulatorConstants;
import org.wso2.eventsimulator.core.simulator.databaseFeedSimulation.DatabaseFeedSimulationDto;
import org.wso2.eventsimulator.core.simulator.databaseFeedSimulation.util.DatabaseConnection;
import org.wso2.eventsimulator.core.simulator.exception.EventSimulationException;
import org.wso2.eventsimulator.core.util.EventConverter;
import org.wso2.eventsimulator.core.util.EventSender;
import org.wso2.eventsimulator.core.util.QueuedEvent;
import org.wso2.streamprocessor.core.StreamDefinitionRetriever;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * This simulator simulates the execution plan by sending events from data in a database.
 * <p>
 * This simulator class implements EventSimulator Interface
 */
public class DatabaseFeedSimulator implements EventSimulator {
    private static final Logger log = Logger.getLogger(DatabaseFeedSimulator.class);
    private final Object lock = new Object();
    private DatabaseFeedSimulationDto streamConfiguration;
    private volatile boolean isPaused = false;
    private volatile boolean isStopped = false;

    /**
     * Initialize DatabaseFeedSimulator to star simulation
     *
     * @param streamConfiguration
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
//        ArrayList<StreamDefinitionRetriever.Type> streamAttributeTypes = new ArrayList<StreamDefinitionRetriever.Type>(streamDefinition.values());
        List<String> columnNames = new ArrayList<>(databaseFeedConfiguration.getColumnNamesAndTypes().keySet());
        boolean valid = columnValidation(columnNames, streamAttributeNames);

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

//                        int i=0;


//                        for (int i = 0; i < streamDefinition.size(); i++) {
                            for (Map.Entry<String,StreamDefinitionRetriever.Type> entry : streamDefinition.entrySet()) {
                                for (int i = 0; i < streamDefinition.size(); i++) {
                                    if ((entry.getValue().compareTo(StreamDefinitionRetriever.Type.STRING)) == 0) {
                                        attributeValues[i] = resultSet.getString(entry.getKey());
//                                        if (i < streamDefinition.size()) {
//                                            i++;
//                                            continue;
//                                        } else {
//                                            break;
//                                        }
                                    } else if ((entry.getValue().compareTo(StreamDefinitionRetriever.Type.INTEGER)) == 0) {
                                        attributeValues[i] = String.valueOf(resultSet.getInt(entry.getKey()));
//                                        if (i < streamDefinition.size()) {
//                                            i++;
//                                            continue;
//                                        } else {
//                                            break;
//                                        }
                                    } else if ((entry.getValue().compareTo(StreamDefinitionRetriever.Type.DOUBLE)) == 0) {
                                        attributeValues[i] = String.valueOf(resultSet.getDouble(entry.getKey()));
//                                        if (i < streamDefinition.size()) {
//                                            i++;
//                                            continue;
//                                        } else {
//                                            break;
//                                        }
                                    } else if ((entry.getValue().compareTo(StreamDefinitionRetriever.Type.FLOAT)) == 0) {
                                        attributeValues[i] = String.valueOf(resultSet.getFloat(entry.getKey()));
//                                        if (i < streamDefinition.size()) {
//                                            i++;
//                                            continue;
//                                        } else {
//                                            break;
//                                        }
                                    } else if ((entry.getValue().compareTo(StreamDefinitionRetriever.Type.BOOLEAN)) == 0) {
                                        attributeValues[i] = String.valueOf(resultSet.getBoolean(entry.getKey()));
//                                        if (i < streamDefinition.size()) {
//                                            i++;
//                                            continue;
//                                        } else {
//                                            break;
//                                        }
                                    } else if ((entry.getValue().compareTo(StreamDefinitionRetriever.Type.LONG)) == 0) {
                                        attributeValues[i] = String.valueOf(resultSet.getLong(entry.getKey()));
//                                        if (i < streamDefinition.size()) {
//                                            i++;
//                                            continue;
//                                        } else {
//                                            break;
//                                        }
                                    }
                                }
                            }
//                        }

                        Event event = EventConverter.eventConverter(streamDefinition, attributeValues);
//                        System.out.println("Input Event (Database feed)" + Arrays.deepToString(event.getData()));

//                        todo R 16/02/2017 figure a way to send the execution plan name here
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
