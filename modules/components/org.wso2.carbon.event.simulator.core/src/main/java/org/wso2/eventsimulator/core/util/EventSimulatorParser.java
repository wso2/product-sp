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
package org.wso2.eventsimulator.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.eventsimulator.core.internal.EventSimulatorDataHolder;
import org.wso2.eventsimulator.core.simulator.bean.FeedSimulationDto;
import org.wso2.eventsimulator.core.simulator.bean.FeedSimulationStreamConfiguration;
import org.wso2.eventsimulator.core.simulator.bean.FileStore;
import org.wso2.eventsimulator.core.simulator.csvFeedSimulation.CSVFileSimulationDto;
import org.wso2.eventsimulator.core.simulator.csvFeedSimulation.core.FileDto;
import org.wso2.eventsimulator.core.simulator.databaseFeedSimulation.DatabaseFeedSimulationDto;
import org.wso2.eventsimulator.core.simulator.exception.EventSimulationException;
import org.wso2.eventsimulator.core.simulator.randomdatafeedsimulation.bean.CustomBasedAttribute;
import org.wso2.eventsimulator.core.simulator.randomdatafeedsimulation.bean.FeedSimulationStreamAttributeDto;
import org.wso2.eventsimulator.core.simulator.randomdatafeedsimulation.bean.PrimitiveBasedAttribute;
import org.wso2.eventsimulator.core.simulator.randomdatafeedsimulation.bean.PropertyBasedAttributeDto;
import org.wso2.eventsimulator.core.simulator.randomdatafeedsimulation.bean.RandomDataSimulationDto;
import org.wso2.eventsimulator.core.simulator.randomdatafeedsimulation.bean.RegexBasedAttributeDto;
import org.wso2.eventsimulator.core.simulator.randomdatafeedsimulation.util.RandomDataGenerator;
import org.wso2.eventsimulator.core.simulator.singleventsimulator.SingleEventDto;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * EventSimulatorParser is an util class used to
 * convert Json string into relevant event simulation configuration object
 */
public class EventSimulatorParser {
    private static final Logger log = Logger.getLogger(EventSimulatorParser.class);

    /*
    Initialize EventSimulatorParser
     */
    private EventSimulatorParser() {
    }

    /**
     * Convert the RandomFeedSimulationString string into RandomDataSimulationDto Object
     * RandomRandomDataSimulationConfig can have one or more attribute simulation configuration.
     * these can be one of below types
     * 1.PRIMITIVEBASED : String/Integer/Float/Double/Boolean
     * 2.PROPERTYBASED  : this type indicates the type which generates meaning full data.
     * eg: If full name it generate meaning full name
     * 3.REGEXBASED     : this type indicates the type which generates data using given regex
     * 4.CUSTOMDATA     : this type indicates the type which generates data in given data list
     * <p>
     * Initialize RandomDataSimulationDto
     *
     * @param RandomFeedSimulationString RandomEventSimulationConfiguration String
     * @return RandomDataSimulationDto Object
     */
    private static RandomDataSimulationDto randomDataSimulatorParser(String RandomFeedSimulationString) {
        RandomDataSimulationDto randomDataSimulationDto = new RandomDataSimulationDto();

        JSONObject jsonObject = new JSONObject(RandomFeedSimulationString);
        //set properties to RandomDataSimulationDto
        if (jsonObject.has(EventSimulatorConstants.STREAM_NAME) && !jsonObject.getString(EventSimulatorConstants.STREAM_NAME).isEmpty()) {
            randomDataSimulationDto.setStreamName(jsonObject.getString(EventSimulatorConstants.STREAM_NAME));
        } else {
            log.error("Stream name can not be null or an empty value");
            throw new RuntimeException("Stream name can not be null or an empty value");
        }
        if (jsonObject.has(EventSimulatorConstants.EXECUTION_PLAN_NAME) && !jsonObject.getString(EventSimulatorConstants.EXECUTION_PLAN_NAME).isEmpty()) {
            randomDataSimulationDto.setExecutionPlanName(jsonObject.getString(EventSimulatorConstants.EXECUTION_PLAN_NAME));
        } else {
            log.error("Execution plan name can not be null or an empty value");
            throw new RuntimeException("Execution plan name can not be null or an empty value");
        }
        if (jsonObject.has(EventSimulatorConstants.EVENTS) && !jsonObject.getString(EventSimulatorConstants.EVENTS).isEmpty()) {
            if (jsonObject.getInt(EventSimulatorConstants.EVENTS) <= 0) {
                log.error("Number of events to be generated cannot be a negative value");
                throw new RuntimeException("Number of events to be generated cannot be a negative value");
            } else {
                randomDataSimulationDto.setEvents(jsonObject.getInt(EventSimulatorConstants.EVENTS));
            }
        } else {
            log.error("Number of events to be generated cannot be  null or an empty value");
            throw new RuntimeException("Number of events to be generated cannot be null or an empty value");
        }
        if (jsonObject.has(EventSimulatorConstants.DELAY) && !jsonObject.getString(EventSimulatorConstants.DELAY).isEmpty()) {
            randomDataSimulationDto.setDelay(jsonObject.getInt(EventSimulatorConstants.DELAY));
        } else {
            log.warn("Delay cannot be null or an empty value. Delay is set to 0 milliseconds");
            randomDataSimulationDto.setDelay(0);
        }

        List<Attribute> streamAttributes = EventSimulatorDataHolder
                .getInstance().getEventStreamService().getStreamAttributes(randomDataSimulationDto.getExecutionPlanName(),randomDataSimulationDto.getStreamName());
        List<FeedSimulationStreamAttributeDto> feedSimulationStreamAttributeDto = new ArrayList<>();

        JSONArray jsonArray;
        if (jsonObject.has(EventSimulatorConstants.ATTRIBUTE_CONFIGURATION) && jsonObject.getJSONArray(EventSimulatorConstants.ATTRIBUTE_CONFIGURATION).length() > 0) {
            jsonArray = jsonObject.getJSONArray(EventSimulatorConstants.ATTRIBUTE_CONFIGURATION);
        } else {
            log.error("Attribute configuration cannot be null or an empty value");
            throw new RuntimeException("Attribute configuration cannot be null or an empty value");
        }

        if (jsonArray.length() != streamAttributes.size()) {
            throw new EventSimulationException("Random feed simulation of stream '" + randomDataSimulationDto.getStreamName() +
                    "' requires attribute configurations for " + streamAttributes.size() + " attributes. Number of attribute" +
                    " configurations provided is " + jsonArray.length());
        }

        //convert each attribute simulation configuration as relevant objects

        Gson gson = new Gson();
        for (int i = 0; i < jsonArray.length(); i++) {
            if (!jsonArray.getJSONObject(i).isNull(EventSimulatorConstants.RANDOM_DATA_GENERATOR_TYPE)
                    && !jsonArray.getJSONObject(i).getString(EventSimulatorConstants.RANDOM_DATA_GENERATOR_TYPE).isEmpty()) {
                if (jsonArray.getJSONObject(i).getString(EventSimulatorConstants.RANDOM_DATA_GENERATOR_TYPE).
                        compareTo(FeedSimulationStreamAttributeDto.RandomDataGeneratorType.PROPERTY_BASED.toString()) == 0) {
                    if (!jsonArray.getJSONObject(i).isNull(EventSimulatorConstants.PROPERTY_BASED_ATTRIBUTE_CATEGORY)
                            && !jsonArray.getJSONObject(i).getString(EventSimulatorConstants.PROPERTY_BASED_ATTRIBUTE_CATEGORY).isEmpty()
                            && !jsonArray.getJSONObject(i).isNull(EventSimulatorConstants.PROPERTY_BASED_ATTRIBUTE_PROPERTY)
                            && !jsonArray.getJSONObject(i).getString(EventSimulatorConstants.PROPERTY_BASED_ATTRIBUTE_PROPERTY).isEmpty()) {
                        PropertyBasedAttributeDto propertyBasedAttributeDto =
                                gson.fromJson(String.valueOf(jsonArray.getJSONObject(i)), PropertyBasedAttributeDto.class);

                        feedSimulationStreamAttributeDto.add(propertyBasedAttributeDto);
                    } else {
                        log.error("Category and property values should not be null value for " +
                                FeedSimulationStreamAttributeDto.RandomDataGeneratorType.PROPERTY_BASED + " simulation. Configuration " +
                                "provided is, category : '" + jsonArray.getJSONObject(i).getString(EventSimulatorConstants.PROPERTY_BASED_ATTRIBUTE_CATEGORY) +
                                "' and property : '" + jsonArray.getJSONObject(i).getString(EventSimulatorConstants.PROPERTY_BASED_ATTRIBUTE_PROPERTY) +"'.");
                        throw new EventSimulationException("Category and property should not be null values for " +
                                FeedSimulationStreamAttributeDto.RandomDataGeneratorType.PROPERTY_BASED + " simulation. Configuration " +
                                "provided is, category : '" + jsonArray.getJSONObject(i).getString(EventSimulatorConstants.PROPERTY_BASED_ATTRIBUTE_CATEGORY) +
                                "' and property : '" + jsonArray.getJSONObject(i).getString(EventSimulatorConstants.PROPERTY_BASED_ATTRIBUTE_PROPERTY) +"'.");
                    }
                } else if (jsonArray.getJSONObject(i).getString(EventSimulatorConstants.RANDOM_DATA_GENERATOR_TYPE).
                        compareTo(FeedSimulationStreamAttributeDto.RandomDataGeneratorType.REGEX_BASED.toString()) == 0) {
                    if (!jsonArray.getJSONObject(i).isNull(EventSimulatorConstants.REGEX_BASED_ATTRIBUTE_PATTERN)
                            && !jsonArray.getJSONObject(i).getString(EventSimulatorConstants.REGEX_BASED_ATTRIBUTE_PATTERN).isEmpty()) {
                        RegexBasedAttributeDto regexBasedAttributeDto =
                                gson.fromJson(String.valueOf(jsonArray.getJSONObject(i)), RegexBasedAttributeDto.class);
                        log.info(regexBasedAttributeDto.toString());
                        RandomDataGenerator.validateRegularExpression(regexBasedAttributeDto.getPattern());
                        feedSimulationStreamAttributeDto.add(regexBasedAttributeDto);
                    } else {
                        log.error("Pattern should not be null or an empty value for " +
                                FeedSimulationStreamAttributeDto.RandomDataGeneratorType.REGEX_BASED + " simulation.");
                        throw new EventSimulationException("Pattern should not be null or an empty value for " +
                                FeedSimulationStreamAttributeDto.RandomDataGeneratorType.REGEX_BASED + " simulation.");
                    }
                } else if (jsonArray.getJSONObject(i).getString(EventSimulatorConstants.RANDOM_DATA_GENERATOR_TYPE).
                        compareTo(FeedSimulationStreamAttributeDto.RandomDataGeneratorType.PRIMITIVE_BASED.toString()) == 0) {
                    if (!jsonArray.getJSONObject(i).isNull(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MIN)
                            && !jsonArray.getJSONObject(i).getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MIN).isEmpty()
                            && !jsonArray.getJSONObject(i).isNull(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MAX)
                            && !jsonArray.getJSONObject(i).getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MAX).isEmpty()
                            && !jsonArray.getJSONObject(i).isNull(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_LENGTH_DECIMAL)
                            && !jsonArray.getJSONObject(i).getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_LENGTH_DECIMAL).isEmpty()) {
                        PrimitiveBasedAttribute primitiveBasedAttribute =
                                gson.fromJson(String.valueOf(jsonArray.getJSONObject(i)), PrimitiveBasedAttribute.class);
                        feedSimulationStreamAttributeDto.add(primitiveBasedAttribute);
                    } else {
                        log.error("Min,Max and Length values should not be null or an empty value for " +
                                FeedSimulationStreamAttributeDto.RandomDataGeneratorType.PRIMITIVE_BASED + " simulation." +
                                " Configuration provided is Min : '" + jsonArray.getJSONObject(i).getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MIN)
                                + "', Max : '" + jsonArray.getJSONObject(i).getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MAX)
                                + "' and Length : '" + jsonArray.getJSONObject(i).getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_LENGTH_DECIMAL) + "'.");
                        throw new EventSimulationException("Min,Max and Length values should not be null or an empty value for " +
                                FeedSimulationStreamAttributeDto.RandomDataGeneratorType.PRIMITIVE_BASED + " simulation." +
                                " Configuration provided is Min : '" + jsonArray.getJSONObject(i).getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MIN)
                                + "', Max : '" + jsonArray.getJSONObject(i).getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MAX)
                                + "' and Length : '" + jsonArray.getJSONObject(i).getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_LENGTH_DECIMAL) + "'.");
                    }
                } else if (jsonArray.getJSONObject(i).getString(EventSimulatorConstants.RANDOM_DATA_GENERATOR_TYPE).
                        compareTo(FeedSimulationStreamAttributeDto.RandomDataGeneratorType.CUSTOM_DATA_BASED.toString()) == 0) {
                    if (!jsonArray.getJSONObject(i).isNull(EventSimulatorConstants.CUSTOM_DATA_BASED_ATTRIBUTE_LIST)
                            && !jsonArray.getJSONObject(i).getString(EventSimulatorConstants.CUSTOM_DATA_BASED_ATTRIBUTE_LIST).isEmpty()) {
                        CustomBasedAttribute customBasedAttribute = new CustomBasedAttribute();
                        customBasedAttribute.setType(FeedSimulationStreamAttributeDto.RandomDataGeneratorType.CUSTOM_DATA_BASED);
                        customBasedAttribute.setCustomData(jsonArray.getJSONObject(i).getString(EventSimulatorConstants.CUSTOM_DATA_BASED_ATTRIBUTE_LIST));
                        feedSimulationStreamAttributeDto.add(customBasedAttribute);
                    } else {
                        log.error("Data list is not given for " + FeedSimulationStreamAttributeDto.RandomDataGeneratorType.CUSTOM_DATA_BASED + " simulation.");
                        throw new EventSimulationException("Data list is not given for " +
                                FeedSimulationStreamAttributeDto.RandomDataGeneratorType.CUSTOM_DATA_BASED + " simulation.");
                    }
                } else {
                    log.error("Invalid random data generator type '" + jsonArray.getJSONObject(i).getString(EventSimulatorConstants.RANDOM_DATA_GENERATOR_TYPE) +
                            "'. Generator type should be : " + FeedSimulationStreamAttributeDto.RandomDataGeneratorType.PROPERTY_BASED
                            + " / " + FeedSimulationStreamAttributeDto.RandomDataGeneratorType.REGEX_BASED + " / "
                            +FeedSimulationStreamAttributeDto.RandomDataGeneratorType.PRIMITIVE_BASED +
                            " / " + FeedSimulationStreamAttributeDto.RandomDataGeneratorType.CUSTOM_DATA_BASED);
                    throw new EventSimulationException("Invalid random data generator type '" +
                            jsonArray.getJSONObject(i).getString(EventSimulatorConstants.RANDOM_DATA_GENERATOR_TYPE) + "'. Generator type should be : " +
                            FeedSimulationStreamAttributeDto.RandomDataGeneratorType.PROPERTY_BASED + " / " +
                            FeedSimulationStreamAttributeDto.RandomDataGeneratorType.REGEX_BASED + " / " +
                            FeedSimulationStreamAttributeDto.RandomDataGeneratorType.PRIMITIVE_BASED +
                            " / " + FeedSimulationStreamAttributeDto.RandomDataGeneratorType.CUSTOM_DATA_BASED);
                }
            } else {
                log.error("Random data generator type is required  for an attribute. Generator type should be : " +
                        FeedSimulationStreamAttributeDto.RandomDataGeneratorType.PROPERTY_BASED + " / " +
                        FeedSimulationStreamAttributeDto.RandomDataGeneratorType.REGEX_BASED + " / " +
                        FeedSimulationStreamAttributeDto.RandomDataGeneratorType.PRIMITIVE_BASED +
                        " / " + FeedSimulationStreamAttributeDto.RandomDataGeneratorType.CUSTOM_DATA_BASED);
                throw new EventSimulationException("Random data generator type is required  for an attribute. Generator type should be : " +
                        FeedSimulationStreamAttributeDto.RandomDataGeneratorType.PROPERTY_BASED + " / " +
                        FeedSimulationStreamAttributeDto.RandomDataGeneratorType.REGEX_BASED + " / " +
                        FeedSimulationStreamAttributeDto.RandomDataGeneratorType.PRIMITIVE_BASED +
                        " / " + FeedSimulationStreamAttributeDto.RandomDataGeneratorType.CUSTOM_DATA_BASED);
            }
        }
        randomDataSimulationDto.setFeedSimulationStreamAttributeDto(feedSimulationStreamAttributeDto);

        return randomDataSimulationDto;
    }


    /**
     * Convert the singleEventSimulationConfigurationString string into SingleEventDto Object
     * Initialize SingleEventDto
     *
     * @param singleEventSimulationConfigurationString singleEventSimulationConfigurationString String
     * @return SingleEventDto Object
     */
    public static SingleEventDto singleEventSimulatorParser(String singleEventSimulationConfigurationString) {
        SingleEventDto singleEventDto;
        ObjectMapper mapper = new ObjectMapper();
        //Convert the singleEventSimulationConfigurationString string into SingleEventDto Object
        try {
            singleEventDto = mapper.readValue(singleEventSimulationConfigurationString, SingleEventDto.class);
            singleEventDto.setSimulationType(FeedSimulationStreamConfiguration.SimulationType.SINGLE_EVENT);
            singleEventDto.setTimestampAttribute(null);

            List<Attribute> streamAttributes = EventSimulatorDataHolder
                    .getInstance().getEventStreamService().getStreamAttributes(singleEventDto.getExecutionPlanName(),singleEventDto.getStreamName());
            if (singleEventDto.getAttributeValues().size() != streamAttributes.size()) {
                log.error("Number of attribute values is not equal to number of attributes in stream '" +
                        singleEventDto.getStreamName() + "' . Required number of attributes : " +
                        streamAttributes.size());
                throw new EventSimulationException("Number of attribute values is not equal to number of attributes in stream '" +
                        singleEventDto.getStreamName() + "' . Required number of attributes : " +
                        streamAttributes.size());
            }
        } catch (IOException e) {
            log.error("Exception occurred when parsing json to Object ");
            throw new EventSimulationException("Exception occurred when parsing json to Object : " + e.getMessage());
        }
        return singleEventDto;
    }

    /**
     * Convert the csvFileDetail string into CSVFileSimulationDto Object
     * <p>
     * Initialize CSVFileSimulationDto
     * Initialize FileStore
     *
     * @param csvFileDetail csvFileDetail String
     * @return CSVFileSimulationDto Object
     */
    private static CSVFileSimulationDto fileFeedSimulatorParser(String csvFileDetail) {
        CSVFileSimulationDto csvFileSimulationDto = new CSVFileSimulationDto();
        FileStore fileStore = FileStore.getFileStore();

        JSONObject jsonObject = new JSONObject(csvFileDetail);
        if (jsonObject.has(EventSimulatorConstants.STREAM_NAME) && !jsonObject.getString(EventSimulatorConstants.STREAM_NAME).isEmpty()) {
            csvFileSimulationDto.setStreamName(jsonObject.getString(EventSimulatorConstants.STREAM_NAME));
        } else {
            log.error("Stream name cannot be null or an empty value");
            throw new RuntimeException("Stream name cannot be null or an empty value");
        }
        if (jsonObject.has(EventSimulatorConstants.EXECUTION_PLAN_NAME) && !jsonObject.getString(EventSimulatorConstants.EXECUTION_PLAN_NAME).isEmpty()) {
            csvFileSimulationDto.setExecutionPlanName(jsonObject.getString(EventSimulatorConstants.EXECUTION_PLAN_NAME));
        } else {
            log.error("Execution plan name cannot be null or an empty value");
            throw new RuntimeException("Execution plan name cannot be null or an empty value");
        }
        if (jsonObject.has(EventSimulatorConstants.FILE_NAME) && !jsonObject.getString(EventSimulatorConstants.FILE_NAME).isEmpty()) {
            csvFileSimulationDto.setFileName(jsonObject.getString(EventSimulatorConstants.FILE_NAME));
        } else {
            log.error("File name cannot be null or an empty value");
            throw new RuntimeException("File name cannot be null or an empty value");
        }
        //get the fileDto from FileStore if file exist and set this value.
        FileDto fileDto;
        try {
            if (fileStore.checkExists(csvFileSimulationDto.getFileName())) {
                fileDto = fileStore.getFileInfoMap().get(csvFileSimulationDto.getFileName());
            } else {
                log.error("File does not Exist : " + csvFileSimulationDto.getFileName());
                throw new EventSimulationException("File does not Exist");
            }
            csvFileSimulationDto.setFileDto(fileDto);

            if (jsonObject.has(EventSimulatorConstants.DELIMITER) && !jsonObject.getString(EventSimulatorConstants.DELIMITER).isEmpty()) {
                csvFileSimulationDto.setDelimiter((String) jsonObject.get(EventSimulatorConstants.DELIMITER));
            } else {
                log.error("Delimiter cannot be null or an empty value");
                throw new RuntimeException("Delimiter cannot be null or an empty value");
            }
            if (jsonObject.has(EventSimulatorConstants.DELAY) && !jsonObject.getString(EventSimulatorConstants.DELAY).isEmpty()) {
                csvFileSimulationDto.setDelay(jsonObject.getInt(EventSimulatorConstants.DELAY));
            } else {
                csvFileSimulationDto.setDelay(0);
                log.warn("Delay cannot be null or an empty value. Delay is set to 0 milliseconds");
            }

        } catch (Exception FileNotFound) {
            System.out.println("File not found : " + FileNotFound.getMessage());
        }
        return csvFileSimulationDto;
    }

    /**
     * Convert the database configuration file into a DatabaseFeedSimulationDto object
     *
     * @param databaseConfigurations : database configuration string
     * @return a DatabaseFeedSimulationDto object
     */

    private static DatabaseFeedSimulationDto databaseFeedSimulationParser(String databaseConfigurations) {

        DatabaseFeedSimulationDto databaseFeedSimulationDto = new DatabaseFeedSimulationDto();
        JSONObject jsonObject = new JSONObject(databaseConfigurations);

//       assign values for database configuration attributes

        if (jsonObject.has(EventSimulatorConstants.DATABASE_CONFIGURATION_NAME) && !jsonObject.getString(EventSimulatorConstants.DATABASE_CONFIGURATION_NAME).isEmpty()) {
            databaseFeedSimulationDto.setDatabaseConfigName(jsonObject.getString(EventSimulatorConstants.DATABASE_CONFIGURATION_NAME));
        } else {
            log.error("Database configuration name cannot be null or an empty value");
            throw new RuntimeException("Database configuration name can not be null or an empty value");
        }
        if (jsonObject.has(EventSimulatorConstants.DATABASE_NAME) && !jsonObject.getString(EventSimulatorConstants.DATABASE_NAME).isEmpty()) {
            databaseFeedSimulationDto.setDatabaseName(jsonObject.getString(EventSimulatorConstants.DATABASE_NAME));
        } else {
            log.error("Database name cannot be null or an empty value");
            throw new RuntimeException("Database name cannot be null or an empty value");
        }
        if (jsonObject.has(EventSimulatorConstants.USER_NAME) && !jsonObject.getString(EventSimulatorConstants.USER_NAME).isEmpty()) {
            databaseFeedSimulationDto.setUsername(jsonObject.getString(EventSimulatorConstants.USER_NAME));
        } else {
            log.error("Username can not be null or an empty value");
            throw new RuntimeException("Username cannot be null or an empty value");
        }
        if (jsonObject.has(EventSimulatorConstants.PASSWORD) && !jsonObject.getString(EventSimulatorConstants.PASSWORD).isEmpty()) {
            databaseFeedSimulationDto.setPassword(jsonObject.getString(EventSimulatorConstants.PASSWORD));
        } else {
            log.error("Password cannot be null or an empty value");
            throw new RuntimeException("Password cannot be null or an empty value");
        }
        if (jsonObject.has(EventSimulatorConstants.TABLE_NAME) && !jsonObject.getString(EventSimulatorConstants.TABLE_NAME).isEmpty()) {
            databaseFeedSimulationDto.setTableName(jsonObject.getString(EventSimulatorConstants.TABLE_NAME));
        } else {
            log.error("Table name cannot be null or an empty value");
            throw new RuntimeException("Table name cannot be null or an empty value");
        }
        if (jsonObject.has(EventSimulatorConstants.STREAM_NAME) && !jsonObject.getString(EventSimulatorConstants.STREAM_NAME).isEmpty()) {
            databaseFeedSimulationDto.setStreamName(jsonObject.getString(EventSimulatorConstants.STREAM_NAME));
        } else {
            log.error("Stream name cannot be null or an empty value");
            throw new RuntimeException("Stream name cannot be null or an empty value");
        }
        if (jsonObject.has(EventSimulatorConstants.EXECUTION_PLAN_NAME) && !jsonObject.getString(EventSimulatorConstants.EXECUTION_PLAN_NAME).isEmpty()) {
            databaseFeedSimulationDto.setExecutionPlanName(jsonObject.getString(EventSimulatorConstants.EXECUTION_PLAN_NAME));
        } else {
            log.error("Execution plan name cannot be null or an empty value");
            throw new RuntimeException("Execution plan name cannot be null or an empty value");
        }
        if (jsonObject.has(EventSimulatorConstants.DELAY) && !jsonObject.getString(EventSimulatorConstants.DELAY).isEmpty()) {
            databaseFeedSimulationDto.setDelay(jsonObject.getInt(EventSimulatorConstants.DELAY));
        } else {
            log.error("Delay cannot be null or an empty value. Delay is set to 0.");
            databaseFeedSimulationDto.setDelay(0);
        }

//      insert the specified column names into a list and set it to database configuration;
        if (jsonObject.has(EventSimulatorConstants.COLUMN_NAMES_LIST)) {
            List<String> columns = new ArrayList<String>(
                    Arrays.asList(jsonObject.getString(EventSimulatorConstants.COLUMN_NAMES_LIST).split("\\s*,\\s*")));
            for (int i = 0;  i < columns.size(); i++) {
                if (!columns.get(i).isEmpty() && columns.get(i) != null){
                    continue;
                } else {
                    throw new EventSimulationException("Column name cannot contain null or empty values");
                }
            }
            databaseFeedSimulationDto.setColumnNames(columns);
        } else {
            log.error("Column names list cannot be null or an empty value");
            throw new RuntimeException("Column names list cannot be null or an empty value");
        }
        return databaseFeedSimulationDto;
    }


    /**
     * Convert the feedSimulationDetails string into FeedSimulationDto Object
     * Three types of feed simulation are applicable for an input stream
     * These types are
     * 1. CSV file feed simulation : Simulate using CSV File
     * 2. Database Simulation : Simulate using Database source
     * 3. Random data simulation : Simulate using Generated random Data
     * <p>
     * Initialize FeedSimulationDto
     *
     * @param feedSimulationDetails feedSimulationDetails
     * @return FeedSimulationDto Object
     */
    public static FeedSimulationDto feedSimulationParser(String feedSimulationDetails) {

            FeedSimulationDto feedSimulationDto = new FeedSimulationDto();
            JSONObject jsonObject = new JSONObject(feedSimulationDetails);

            List<FeedSimulationStreamConfiguration> feedSimulationStreamConfigurationList = new ArrayList<>();
            JSONArray jsonArray = null;

            if (jsonObject.has(EventSimulatorConstants.FEED_SIMULATION_STREAM_CONFIGURATION)
                    && jsonObject.getJSONArray(EventSimulatorConstants.FEED_SIMULATION_STREAM_CONFIGURATION).length() > 0) {
                jsonArray = jsonObject.getJSONArray(EventSimulatorConstants.FEED_SIMULATION_STREAM_CONFIGURATION);
            } else {
                log.error("Stream configuration cannot be null or empty");
                throw new RuntimeException("Stream configuration cannot be null or empty");
            }

            if (jsonObject.has(EventSimulatorConstants.ORDER_BY_TIMESTAMP) && !jsonObject.getString(EventSimulatorConstants.ORDER_BY_TIMESTAMP).isEmpty()) {
                if (jsonObject.getBoolean(EventSimulatorConstants.ORDER_BY_TIMESTAMP)) {
                    feedSimulationDto.setOrderByTimeStamp(jsonObject.getBoolean(EventSimulatorConstants.ORDER_BY_TIMESTAMP));
//                todo 01/03/2017 currently if orderByTimestamp flag is set to true, its assumed that the entire simulation configuration generates events to one stream. should this be changed?
//                set the number of data sources used to generate event as 'NoOfParallelSimulationSources'
                    feedSimulationDto.setNoOfParallelSimulationSources(jsonArray.length());
//                the minimum size for the queue used for sorting will be twice as the number of data sources generating events
                    EventSender.getInstance().setMinQueueSize(2 * (feedSimulationDto.getNoOfParallelSimulationSources()));
                }
            } else {
                log.warn("Simulation configuration does not contain the orderByTimestamp flag. Flag value will be set to 'false'");
            }

            //check the simulation type for databaseFeedSimulation given stream and convert the string to relevant configuration object
            //            1. CSV file feed simulation : Simulate using CSV File
            //            2. Database Simulation : Simulate using Database source
            //            3. Random data simulation : Simulate using random Data generated

            for (int i = 0; i < jsonArray.length(); i++) {
                if (!jsonArray.getJSONObject(i).isNull(EventSimulatorConstants.FEED_SIMULATION_TYPE)
                        && !jsonArray.getJSONObject(i).getString(EventSimulatorConstants.FEED_SIMULATION_TYPE).isEmpty()) {
                    FeedSimulationStreamConfiguration.SimulationType feedSimulationType = FeedSimulationStreamConfiguration.SimulationType.valueOf
                            (jsonArray.getJSONObject(i).getString(EventSimulatorConstants.FEED_SIMULATION_TYPE).toUpperCase());

                    switch (feedSimulationType) {
                        case RANDOM_DATA_SIMULATION:
                            RandomDataSimulationDto randomDataSimulationDto =
                                    randomDataSimulatorParser(String.valueOf(jsonArray.getJSONObject(i)));
                            randomDataSimulationDto.setSimulationType(FeedSimulationStreamConfiguration.SimulationType.RANDOM_DATA_SIMULATION);
                            feedSimulationStreamConfigurationList.add(randomDataSimulationDto);
                            break;
                        case FILE_SIMULATION:
                            CSVFileSimulationDto csvFileConfig = EventSimulatorParser.
                                    fileFeedSimulatorParser(String.valueOf(jsonArray.getJSONObject(i)));
                            if (feedSimulationDto.getOrderByTimeStamp()) {
                                if (jsonArray.getJSONObject(i).has(EventSimulatorConstants.TIMESTAMP_POSITION) &&
                                        !(jsonArray.getJSONObject(i).getString(EventSimulatorConstants.TIMESTAMP_POSITION).isEmpty())) {
                                    csvFileConfig.setTimestampAttribute(String.valueOf(jsonArray.getJSONObject(i).
                                            getString(EventSimulatorConstants.TIMESTAMP_POSITION)));
                                } else {
                                    csvFileConfig.setTimestampAttribute(String.valueOf(1));
                                }
                            }
                            csvFileConfig.setSimulationType(FeedSimulationStreamConfiguration.SimulationType.FILE_SIMULATION);
                            feedSimulationStreamConfigurationList.add(csvFileConfig);
                            break;

                        case DATABASE_SIMULATION:
                            DatabaseFeedSimulationDto databaseFeedSimulationDto =
                                    EventSimulatorParser.databaseFeedSimulationParser(String.valueOf(jsonArray.getJSONObject(i)));
                            if (feedSimulationDto.getOrderByTimeStamp()) {
                                if (jsonArray.getJSONObject(i).has(EventSimulatorConstants.TIMESTAMP_ATTRIBUTE) &&
                                        !(jsonArray.getJSONObject(i).getString(EventSimulatorConstants.TIMESTAMP_ATTRIBUTE).isEmpty())) {
                                    databaseFeedSimulationDto.setTimestampAttribute(String.valueOf(jsonArray.getJSONObject(i).
                                            getString(EventSimulatorConstants.TIMESTAMP_ATTRIBUTE)));
                                } else {
                                    databaseFeedSimulationDto.setTimestampAttribute(String.valueOf(databaseFeedSimulationDto.getColumnNames().get(0)));
                                }
                            }
                            databaseFeedSimulationDto.setSimulationType(FeedSimulationStreamConfiguration.SimulationType.DATABASE_SIMULATION);
                            feedSimulationStreamConfigurationList.add(databaseFeedSimulationDto);

                            break;
                        default:
                            log.error("Invalid simulation type '" + feedSimulationType + "'. Valid simulation types : '" +
                                    FeedSimulationStreamConfiguration.SimulationType.RANDOM_DATA_SIMULATION
                                    + "' or '" + FeedSimulationStreamConfiguration.SimulationType.FILE_SIMULATION + "' or '" +
                                    FeedSimulationStreamConfiguration.SimulationType.DATABASE_SIMULATION + "'");
                            throw new EventSimulationException("Invalid simulation type '" + feedSimulationType +
                                    "'. Valid simulation types : '" + FeedSimulationStreamConfiguration.SimulationType.RANDOM_DATA_SIMULATION
                                    + "' or '" + FeedSimulationStreamConfiguration.SimulationType.FILE_SIMULATION + "' or '" +
                                    FeedSimulationStreamConfiguration.SimulationType.DATABASE_SIMULATION + "'");
                    }
                } else {
                    log.error("Simulation type is not specified");
                    throw new EventSimulationException("Simulation type  is not specified. Simulation types : '"
                            + FeedSimulationStreamConfiguration.SimulationType.RANDOM_DATA_SIMULATION
                            + "' or '" + FeedSimulationStreamConfiguration.SimulationType.FILE_SIMULATION + "' or '" +
                            FeedSimulationStreamConfiguration.SimulationType.DATABASE_SIMULATION + "'");
                }
            }
            feedSimulationDto.setStreamConfigurationList(feedSimulationStreamConfigurationList);
            return feedSimulationDto;
    }

}
