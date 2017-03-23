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

package org.wso2.event.simulator.core.generator.csv.util;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.event.simulator.core.exception.EventGenerationException;
import org.wso2.event.simulator.core.exception.SimulatorInitializationException;
import org.wso2.event.simulator.core.util.CommonOperations;
import org.wso2.event.simulator.core.util.EventConverter;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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

    /**
     * Constructor CSVReader is used to initialize an instance of class CSVReader
     */
    public CSVReader() {
    }

    /**
     * Initialize a file reader for the CSV file.
     * If the CSV file is ordered by timestamp it will create a bufferedReader for the file reader.
     */
    public void initializeFileReader(String fileName, Boolean isOrdered) {

        try {
            this.fileName = fileName;
            fileReader = new InputStreamReader(new FileInputStream(String.valueOf(Paths.get(System.getProperty("java" +
                    ".io.tmpdir"), FileUploader.DIRECTORY_NAME, fileName))), "UTF-8");
//            fileReader = new FileReader(String.valueOf(Paths.get(System.getProperty("java.io.tmpdir"),
//                    FileUploader.DIRECTORY_NAME, fileName)));
            if (log.isDebugEnabled()) {
                log.debug("Initialize a File reader for CSV file '" + fileName + "'.");
            }
            if (isOrdered) {
                bufferedReader = new BufferedReader(fileReader);
            }
        } catch (IOException e) {
            log.error("Error occurred when initializing file reader for CSV file '" + fileName + "' : ", e);
            throw new SimulatorInitializationException("Error occurred when initializing file reader for CSV file '" +
                    fileName + "' : ", e);
        }
    }


    /**
     * If the CSV file is ordered by timestamp, this method reads the next line and produces an event
     *
     * @param streamName stream being simulated
     * @param streamAttributes   list of attributes of the stream to which events are produced
     * @param delimiter delimiter to be used when parsing CSV file
     * @param timestampPosition column to be used as timestamp
     * @param timestampStartTime start timestamp of event simulation
     * @param timestampEndTime   end timestamp of event simulation
     * @return event produced
     */
    public Event getNextEvent(String streamName, List<Attribute> streamAttributes, String delimiter,
                              int timestampPosition, Long timestampStartTime, Long timestampEndTime) {
        Event event = null;
        try {
            while (true) {
                String line = bufferedReader.readLine();

                if (line != null) {
                    int lineLength = line.split(delimiter).length;
//                    if the line does not have sufficient data to produce an event, move to next line
                    if (CommonOperations.checkAttributes(lineLength, streamAttributes.size() + 1)) {
                        /*
                        * steps in creating an event
                        * 1. create an array list by splitting the line at the delimiter.
                        * 2. obtain the value at the timestamp position in the list as the timestamp
                        * 3. remove the value at the timestamp position in the list
                        * 4. convert the array list to a string array.
                        * 5. send the string array, stream attributes list and timestamp to Event converter to
                        *    create an event
                        * */
                        ArrayList<String> attributes = new ArrayList<>(Arrays.asList(line.split(delimiter)));
                        long timestamp = Long.parseLong(attributes.get(timestampPosition));
                        if (timestamp >= timestampStartTime) {

                            if (timestampEndTime == null || timestamp <= timestampEndTime) {
                                attributes.remove(timestampPosition);
                                String[] eventAttributes = attributes.toArray(new String[streamAttributes.size()]);
                                event = EventConverter.eventConverter(streamAttributes, eventAttributes, timestamp);
                                break;
                            }
                        }
                    } else {
                        log.warn("Simulation of stream '" + streamName + "' requires " + streamAttributes.size() + 1 +
                                " but number of attributes found in line is " + lineLength + ". Read next line.");
                    }
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            log.error("Error occurred when reading CSV file '" + fileName + "' to simulate stream '" + streamName +
                    "' :", e);
            throw new EventGenerationException("Error occurred when reading CSV file '" + fileName + "' to simulate" +
                    " stream '" + streamName + "' :", e);
        }
        return event;
    }


    /**
     * If the CSV is not ordered by timestamp, getEventsMap() method is used to create a treeMap of events.
     *
     * @param delimiter delimiter to be used when parsing CSV file
     * @param streamName stream being simulated
     * @param streamAttributes   list of attributes of the stream to which events are produced
     * @param timestampPosition column to be used as timestamp
     * @param timestampStartTime start timestamp of event simulation
     * @param timestampEndTime   end timestamp of event simulation
     * @return treeMap of events
     */
    public TreeMap<Long, ArrayList<Event>> getEventsMap(String delimiter, String streamName,
                                                        List<Attribute> streamAttributes, int timestampPosition,
                                                        Long timestampStartTime, Long timestampEndTime) {
        parseFile(delimiter);
        return createEventsMap(streamName, streamAttributes, timestampPosition, timestampStartTime, timestampEndTime);
    }


    /**
     * parseFile() method is used to parse the CSV file using the delimiter specified in CSV simulation Configuration
     *
     * @param delimiter delimiter to be used when parsing CSV file
     */
    private void parseFile(String delimiter) {
        try {
            switch (delimiter) {
                case ",":
                    csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT);
                    break;
                case ";":
                    csvParser = new CSVParser(fileReader, CSVFormat.EXCEL);
                    break;
                case "\\t":
                    csvParser = new CSVParser(fileReader, CSVFormat.TDF);
                    break;
                default:
                    csvParser = new CSVParser(fileReader, CSVFormat.newFormat(delimiter.charAt(0)));
            }
        } catch (IOException e) {
            log.error("Error occurred when initializing CSVParser for CSV file '" + fileName + "' : ", e);
            throw new EventGenerationException("Error occurred when initializing CSVParser for CSV file '" + fileName +
                    "' : ", e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Parse CSV file '" + fileName + "'.");
        }
    }


    /**
     * createEventsMap() methods creates a treeMap of events using the data in the CSV file.
     * The key of the treeMap will be the event timestamp and the value will be an array list of events belonging to
     * the timestamp.
     *
     * @param streamName stream being simulated
     * @param streamAttributes   list of attributes of the stream to which events are produced
     * @param timestampPosition column to be used as timestamp
     * @param timestampStartTime start timestamp of event simulation
     * @param timestampEndTime   end timestamp of event simulation
     * @return a treeMap of events
     */
    private TreeMap<Long, ArrayList<Event>> createEventsMap(String streamName, List<Attribute> streamAttributes, int
            timestampPosition, Long timestampStartTime, Long timestampEndTime) {
        TreeMap<Long, ArrayList<Event>> eventsMap = new TreeMap<>();
        long lineNumber;
        long timestamp;

        if (csvParser != null) {
            for (CSVRecord record : csvParser) {
                lineNumber = csvParser.getCurrentLineNumber();
                /*
                * check whether the number of columns specified is the number of stream attributes
                * if yes, proceed with initialization of generator
                * else, throw an exception
                * */
                if (CommonOperations.checkAttributes(record.size(), streamAttributes.size() + 1)) {

                 /*
                 * steps in creating an event
                 * 1. create an array list for each CSV record in CSV parser
                 * 2. obtain the value at the timestamp position in the list as the timestamp
                 * 3. check whether the timestamp falls within the boundaries of star and end timestamp. if yes proceed
                 *    to step 4. else, read next record
                 * 4. remove the value at the timestamp position in the list
                 * 5. convert the array list to a string array.
                 * 6. send the string array, stream attributes list and timestamp to Event converter to create an event
                 * */

                    ArrayList<String> dataList = new ArrayList<>();

                    for (String attribute : record) {
                        dataList.add(attribute);
                    }

                    timestamp = Long.parseLong(dataList.get(timestampPosition));

                /*
                * if the timestamp of event is between the boundaries of timestamp start and end time,
                * check whether the treeMap has entries for the even timestamp.
                * if there are no entries, add the timestamp as key and an array list with the event as values to
                * treeMap else, retrieve the values for the timestamp and add the event to the values
                * */
                    if (timestamp >= timestampStartTime) {
                        if (timestampEndTime == null || timestamp <= timestampEndTime) {
                            dataList.remove(timestampPosition);
                            String[] eventData = dataList.toArray(new String[streamAttributes.size()]);
                            //convert eventData values into event
                            Event event = EventConverter.eventConverter(streamAttributes, eventData, timestamp);

                            if (!eventsMap.containsKey(timestamp)) {
                                eventsMap.put(timestamp, new ArrayList<>(Arrays.asList(event)));
                            } else {
                                eventsMap.get(timestamp).add(event);
                            }
                        }
                    }
                } else {
                    log.warn("Simulation of stream '" + streamName + "' requires " + streamAttributes.size() + " " +
                            "attributes. Number of attributes in line " + lineNumber + " of CSV file '" + fileName +
                            "' is " + record.size() + ". ");
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
            log.error("Error occurred when closing CSV resources : ", e);
        }

        if (log.isDebugEnabled()) {
            log.debug("Close resources used for CSV file '" + fileName + "'.");
        }
    }

}
