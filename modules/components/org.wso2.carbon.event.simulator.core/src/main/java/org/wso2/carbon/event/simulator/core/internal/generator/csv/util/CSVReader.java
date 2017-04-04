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

package org.wso2.carbon.event.simulator.core.internal.generator.csv.util;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.event.simulator.core.exception.EventGenerationException;
import org.wso2.carbon.event.simulator.core.exception.SimulatorInitializationException;
import org.wso2.carbon.event.simulator.core.internal.util.EventConverter;
import org.wso2.carbon.event.simulator.core.internal.util.EventSimulatorConstants;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;


/**
 * CSVReader class is used to read csv files
 */
public class CSVReader {
    private final Logger log = LoggerFactory.getLogger(CSVReader.class);
    private Reader fileReader = null;
    private BufferedReader bufferedReader = null;
    private CSVParser csvParser = null;
    private String fileName;
    private long lineNumber = 0;

    /**
     * Constructor CSVReader is used to initialize an instance of class CSVReader
     * Initialize a file reader for the CSV file.
     * If the CSV file is ordered by timestamp it will create a bufferedReader for the file reader.
     */
    public CSVReader(String fileName, Boolean isOrdered) {
        try {
            this.fileName = fileName;
            if (new File(String.valueOf(Paths.get(System.getProperty("java" +
                    ".io.tmpdir"), EventSimulatorConstants.DIRECTORY_NAME, fileName))).length() == 0) {
                throw new EventGenerationException("File '" + fileName + "' is empty.");
            }
            fileReader = new InputStreamReader(new FileInputStream(String.valueOf(Paths.get(System.getProperty("java" +
                    ".io.tmpdir"), EventSimulatorConstants.DIRECTORY_NAME, fileName))), "UTF-8");
            if (log.isDebugEnabled()) {
                log.debug("Initialize a File reader for CSV file '" + fileName + "'.");
            }
            if (isOrdered) {
                bufferedReader = new BufferedReader(fileReader);
            }
        } catch (IOException e) {
            log.error("Error occurred when initializing file reader for CSV file '" + fileName + "' : ", e);
            closeParser(isOrdered);
            throw new SimulatorInitializationException("Error occurred when initializing file reader for CSV file '" +
                    fileName + "' : ", e);
        }
    }


    /**
     * If the CSV file is ordered by timestamp, this method reads the next line and produces an event
     *
     * @param streamName         stream being simulated
     * @param streamAttributes   list of attributes of the stream to which events are produced
     * @param delimiter          delimiter to be used when parsing CSV file
     * @param timestampPosition  column to be used as timestamp
     * @param timestampStartTime start timestamp of event simulation
     * @param timestampEndTime   end timestamp of event simulation
     * @return event produced
     */
    public Event getNextEvent(String streamName, List<Attribute> streamAttributes, String delimiter,
                              int timestampPosition, long timeInterval, long timestampStartTime,
                              long timestampEndTime) {
        Event event = null;
        try {
            while (true) {
                lineNumber++;
                String line = bufferedReader.readLine();
                if (line != null) {
                    ArrayList<String> attributes = new ArrayList<>(Arrays.asList(line.split(delimiter)));
                    long timestamp;
//                    if the line does not have sufficient data to produce an event, move to next line
                    if (timestampPosition == -1) {
                        if (attributes.size() == streamAttributes.size()) {
                            /**
                             * if timestamp attribute is not specified, take timestampStartTime as the first event
                             * timestamp and the successive timestamps will be lastTimetstamp + timeInterval
                             * */
                            timestamp = timestampStartTime + (lineNumber - 1) * timeInterval;
                        } else {
                            log.warn("Simulation of stream '" + streamName + "' requires " + streamAttributes.size() +
                                    " attribute(s) but number of attributes found in line " + lineNumber + " of " +
                                    "file '" + fileName + "' is " + attributes.size() + ". Line content : '" +
                                    attributes + "'. Ignore line and read next line.");
                            continue;
                        }
                    } else {
                        if (attributes.size() == streamAttributes.size() + 1) {
                            /**
                             * steps in creating an event if timestamp attribute is specified
                             * 1. obtain the value at the timestamp position in the list as the timestamp
                             * 2. check whether the timestamp is between the timestamp boundary specified. if yes
                             * proceed to step 3 else log a warning and read next line
                             * 3. remove the value at the timestamp position in the list
                             * */
                            timestamp = Long.parseLong(attributes.get(timestampPosition));
                            if (timestamp >= timestampStartTime) {
                                if (timestampEndTime == -1 || timestamp <= timestampEndTime) {
                                    attributes.remove(timestampPosition);
                                } else {
                                    continue;
                                }
                            } else {
                                continue;
                            }
                        } else {
                            log.warn("Simulation of stream '" + streamName + "' requires " +
                                    (streamAttributes.size() + 1) + " attribute(s) but number of attributes found in " +
                                    "line " + lineNumber + " of file '" + fileName + "' is " + attributes.size() + "" +
                                    ". Line content : '" + attributes + "'. Ignore line and read " +
                                    "next line.");
                            continue;
                        }
                    }
                    String[] eventAttributes = attributes.toArray(new String[streamAttributes.size()]);
                    event = EventConverter.eventConverter(streamAttributes, eventAttributes, timestamp);
                    break;
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            log.error("Error occurred when reading CSV file '" + fileName + "' to simulate stream '" + streamName +
                    "' :", e);
            closeParser(true);
            throw new EventGenerationException("Error occurred when reading CSV file '" + fileName + "' to simulate" +
                    " stream '" + streamName + "' :", e);
        }
        return event;
    }


    /**
     * If the CSV is not ordered by timestamp, getEventsMap() method is used to create a treeMap of events.
     *
     * @param delimiter          delimiter to be used when parsing CSV file
     * @param streamName         stream being simulated
     * @param streamAttributes   list of attributes of the stream to which events are produced
     * @param timestampPosition  column to be used as timestamp
     * @param timestampStartTime start timestamp of event simulation
     * @param timestampEndTime   end timestamp of event simulation
     * @return treeMap of events
     */
    public TreeMap<Long, ArrayList<Event>> getEventsMap(String delimiter, String streamName,
                                                        List<Attribute> streamAttributes, int timestampPosition,
                                                        long timestampStartTime, long timestampEndTime) {
        try {
            csvParser = parseFile(delimiter);
            return createEventsMap(streamName, streamAttributes, timestampPosition, timestampStartTime,
                    timestampEndTime);
        } catch (IOException e) {
            log.error("Error occurred when initializing CSVParser for CSV file '" + fileName + "' : ", e);
            throw new EventGenerationException("Error occurred when initializing CSVParser for CSV file '" + fileName +
                    "' : ", e);
        } finally {
            closeParser(false);
        }
    }


    /**
     * parseFile() method is used to parse the CSV file using the delimiter specified in CSV simulation Configuration
     *
     * @param delimiter delimiter to be used when parsing CSV file
     * @throws IOException if an error occurs when creating a CSVReader
     */
    private CSVParser parseFile(String delimiter) throws IOException {
        switch (delimiter) {
            case ",":
                return new CSVParser(fileReader, CSVFormat.DEFAULT);
            case ";":
                return new CSVParser(fileReader, CSVFormat.EXCEL);
            case "\\t":
                return new CSVParser(fileReader, CSVFormat.TDF);
            default:
                return new CSVParser(fileReader, CSVFormat.newFormat(delimiter.charAt(0)));
        }
    }


    /**
     * createEventsMap() methods creates a treeMap of events using the data in the CSV file.
     * The key of the treeMap will be the event timestamp and the value will be an array list of events belonging to
     * the timestamp.
     *
     * @param streamName         stream being simulated
     * @param streamAttributes   list of attributes of the stream to which events are produced
     * @param timestampPosition  column to be used as timestamp
     * @param timestampStartTime start timestamp of event simulation
     * @param timestampEndTime   end timestamp of event simulation
     * @return a treeMap of events
     */
    private TreeMap<Long, ArrayList<Event>> createEventsMap(String streamName, List<Attribute> streamAttributes,
                                                            int timestampPosition, long timestampStartTime, long
                                                                    timestampEndTime) {
        TreeMap<Long, ArrayList<Event>> eventsMap = new TreeMap<>();
        long lineNumber;
        if (csvParser != null) {
            for (CSVRecord record : csvParser) {
                lineNumber = csvParser.getCurrentLineNumber();
                ArrayList<String> attributes = new ArrayList<>();
                for (String attribute : record) {
                    attributes.add(attribute);
                }
                /**
                 * if timestamp attribute is specified record should have data values for stream attributes plus
                 * timestamp.
                 * if sufficient data is not found in record log a warning and proceed to next record
                 * retrieve the value at the position specified by timestamp attribute as the timestamp
                 * if the timestamp is within the range specified by the timestampStartTime and timestampEndTime,
                 * remove timestamp attribute from the 'attributes' list and proceed to creating an event
                 * else ignore record and proceed to next record
                 * */
                if (record.size() == (streamAttributes.size() + 1)) {
                    long timestamp = Long.parseLong(attributes.get(timestampPosition));
                    if (timestamp >= timestampStartTime) {
                        if (timestampEndTime == -1 || timestamp <= timestampEndTime) {
                            attributes.remove(timestampPosition);
                            String[] eventData = attributes.toArray(new String[streamAttributes.size()]);
                            Event event = EventConverter.eventConverter(streamAttributes, eventData, timestamp);
                            if (!eventsMap.containsKey(timestamp)) {
                                eventsMap.put(timestamp, new ArrayList<>(Collections.singletonList(event)));
                            } else {
                                eventsMap.get(timestamp).add(event);
                            }
                        }
                    }
                } else {
                    log.warn("Simulation of stream '" + streamName + "' requires " + (streamAttributes.size() + 1) +
                            " attributes. Number of attributes in line " + lineNumber + " of CSV file '" +
                            fileName + "' is " + record.size() + ". Line content : " + record.toString() + ". " +
                            "Ignore line an read next line");
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Create an ordered events map from CSV file '" + fileName + "' to simulate stream '" +
                    streamName + "'.");
        }
        return eventsMap;
    }


    /**
     * closeParser() method is used to release resources created to read the CSV file
     *
     * @param isOrdered bool indicating whether the entries in CSV file are ordered or not
     */
    public void closeParser(Boolean isOrdered) {
        try {
            if (fileReader != null) {
                fileReader.close();
            }
            if (isOrdered) {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } else {
                if (csvParser != null && !csvParser.isClosed()) {
                    csvParser.close();
                }
            }
        } catch (IOException e) {
            log.error("Error occurred when closing CSV resources used for CSV file '" + fileName + "'", e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Close resources used for CSV file '" + fileName + "'.");
        }
    }

}
