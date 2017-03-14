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

package org.wso2.eventsimulator.core.eventGenerator.csvEventGeneration.util;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.eventsimulator.core.simulator.csvFeedSimulation.CSVFileSimulationDto;
import org.wso2.eventsimulator.core.simulator.csvFeedSimulation.core.FileUploader;
import org.wso2.eventsimulator.core.simulator.exception.EventSimulationException;
import org.wso2.eventsimulator.core.util.EventConverter;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import java.util.*;

/**
 * CSVReader class is used to read csv files
 * */
public class CSVReader {
    private final Logger log = LoggerFactory.getLogger(CSVReader.class);
    private String fileName;
    private Reader fileReader =null;
    private BufferedReader bufferedReader = null;
    private CSVParser csvParser = null;
    private String delimiter;
    private String streamName;
    private int timestampPosition;
    private Boolean isOrdered;
    private Long timestampStartTime;
    private Long timestampEndTime;
    private List<Attribute> streamAttributes;

    /**
     * Constructor CSVReader is used to initialize an instance of class CSVReader
     *
     * @param csvConfiguration   : configuration for CSV simulation
     * @param streamAttributes   : list of attributes of the stream to which events are produced
     * @param timestampStartTime : start timestamp of event simulation
     * @param timestampEndTime   : end timestamp of event simulation
     * */
    public CSVReader (CSVFileSimulationDto csvConfiguration, List<Attribute> streamAttributes,Long timestampStartTime, Long timestampEndTime) {

        if (log.isDebugEnabled()) {
            log.debug("Initialize a CSVReader instance for CSV file '" + fileName + "'.");
        }

        this.fileName = csvConfiguration.getFileName();
        this.delimiter = csvConfiguration.getDelimiter();
        this.streamName = csvConfiguration.getStreamName();
        this.timestampPosition = Integer.valueOf(csvConfiguration.getTimestampAttribute()) - 1;
        this.isOrdered = csvConfiguration.getIsOrdered();
        this.streamAttributes = streamAttributes;
        this.timestampStartTime = timestampStartTime;
        this.timestampEndTime = timestampEndTime;
    }

    /**
     * Initialize a file reader for the CSV file.
     * If the CSV file is ordered by timestamp it will create a bufferedReader for the file reader.
     * */
    public void initializeFileReader() {

        try {
            if (log.isDebugEnabled()) {
                log.debug("Initialize a File reader for CSV file '" + fileName + "'.");
            }
            fileReader = new FileReader(String.valueOf(Paths.get(System.getProperty("java.io.tmpdir"), FileUploader.DIRECTORY_NAME,fileName)));
            if (isOrdered) {
                bufferedReader = new BufferedReader(fileReader);
            }
        } catch (IOException e) {
            log.error("Error occurred when initializing file reader for CSV file '" + fileName + "' : " + e.getMessage());
            throw new EventSimulationException("Error occurred when initializing file reader CSV file '" + fileName
                    + "' : " + e.getMessage());
        }
    }


    /**
     * If the CSV file is ordered by timestamp, this method reads the next line and produces an event
     *
     * @return event produced
     * */
    public Event getNextEvent() {
        if (log.isDebugEnabled()) {
            log.debug("Create next event from CSV file '" + fileName + "'.");
        }
        Event event = null;
        try {
            while (true) {
                String line = bufferedReader.readLine();
                if (line != null) {
                    int lineLength = line.split(delimiter).length;
                    if (lineLength != streamAttributes.size() + 1) {
                        log.warn("Simulation of stream '" + streamName + "' requires " + streamAttributes.size() + 1 +
                                " but number of attributes found in line is " + lineLength + ". Read next line.");
                        continue;
                    } else {
                        ArrayList<String> attributes = new ArrayList<String>(Arrays.asList(line.split(delimiter)));
                        long timestamp = Long.valueOf(attributes.get(timestampPosition));
                        if (timestamp >= timestampStartTime) {
                            if (timestamp <= timestampEndTime || timestampEndTime == null) {
                                attributes.remove(timestampPosition);
                                String[] eventAttributes = attributes.toArray(new String[streamAttributes.size()]);
                                event = EventConverter.eventConverter(streamAttributes, eventAttributes, timestamp);
                                break;
                            } else {
                                continue;
                            }
                        } else {
                            continue;
                        }
                    }
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            log.error("Error occurred when reading CSV file : " + e.getMessage());
            throw new EventSimulationException("Error occurred when reading CSV file : " + e.getMessage());
        }
        return event;
    }


    /**
     * If the CSV is not ordered by timestamp, getEventsMap() method is used to create a treeMap of events.
     *
     * @return treeMap of events
     * */
    public TreeMap<Long, ArrayList<Event>> getEventsMap() {
        if (log.isDebugEnabled()) {
            log.debug("Retrieve an ordered events map from CSV file '" + fileName + "'.");
        }
        parseFile();
        return createEventsMap(streamAttributes);
    }


    /**
     * parseFile() method is used to parse the CSV file using the delimiter specified in CSV simulation Configuration
     * */
    private void parseFile() {
        if (log.isDebugEnabled()) {
            log.debug("Parse CSV file '" + fileName + "'.");
        }
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
           log.error("Error occurred when initializing CSVParser for CSV file '" + fileName + "' : " + e.getMessage());
           throw new EventSimulationException("Error occurred when initializing CSV parser for CSV file '"
                   + fileName + "' : " + e.getMessage());
       }
    }



    /**
     * createEventsMap() methods creates a treeMap of events using the data in the CSV file.
     * The key of the treeMap will be the event timestamp and the value will be an array list of events belonging to the timestamp.
     *
     * @param streamAttributes : list of attributes of the stream for which events are produced
     * @return a treeMap of events
     * */
    private TreeMap<Long, ArrayList<Event>> createEventsMap(List<Attribute> streamAttributes) {
        if (log.isDebugEnabled()) {
            log.debug("Create an ordered events map from CSV file '" + fileName + "'.");
        }
        TreeMap<Long, ArrayList<Event>> eventsMap = new TreeMap<>();
        long lineNumber;
        long timestamp;

        if (csvParser != null) {
            for (CSVRecord record : csvParser) {
                lineNumber = csvParser.getCurrentLineNumber();
                if (record.size() != streamAttributes.size() + 1) {
                    log.warn("Stream '" + streamName + "' requires " + streamAttributes.size() + " attributes. Number of attributes in line "
                            + lineNumber + " of CSV file '" + fileName + "' is " + record.size() + ". ");
                    continue;
                }

                String[] attributes = new String[streamAttributes.size() + 1];

                for (int i = 0; i < record.size(); i++) {
                    attributes[i] = record.get(i);
                }

                ArrayList<String> dataList = new ArrayList<String>(Arrays.asList(attributes));
                timestamp = Long.valueOf(dataList.get(timestampPosition));
                dataList.remove(timestampPosition);
                String[] eventData = dataList.toArray(new String[streamAttributes.size()]);

                if (timestamp >= timestampStartTime) {
                    if (timestamp <= timestampEndTime || timestampEndTime == null) {
                        //convert eventData values into event
                        Event event = EventConverter.eventConverter(streamAttributes, eventData, timestamp);

                        if (!eventsMap.containsKey(timestamp)) {
                            eventsMap.put(timestamp, new ArrayList<Event>() {{ add(event);}});
                        } else {
                            eventsMap.get(timestamp).add(event);
                        }
                    }
                }
            }
        }
        return eventsMap;
    }


    /**
     * closeParser() method is used to release resources created to read the CSV file
     * */
    public void closeParser() {
        if (log.isDebugEnabled()) {
            log.debug("Close resources used for CSV file '" + fileName + "'.");
        }
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
            throw new EventSimulationException("Error occurred when closing CSV resources : " + e.getMessage());
        }
    }

}
