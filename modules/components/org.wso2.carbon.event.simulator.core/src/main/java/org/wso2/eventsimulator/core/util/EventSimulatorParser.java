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
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.eventsimulator.core.eventGenerator.randomEventGeneration.bean.*;
import org.wso2.eventsimulator.core.eventGenerator.randomEventGeneration.util.RegexBasedGenerator;
import org.wso2.eventsimulator.core.internal.EventSimulatorDataHolder;
import org.wso2.eventsimulator.core.simulator.bean.FeedSimulationDto;
import org.wso2.eventsimulator.core.simulator.bean.FeedSimulationStreamConfiguration;
import org.wso2.eventsimulator.core.simulator.bean.FileStore;
import org.wso2.eventsimulator.core.simulator.csvFeedSimulation.CSVFileSimulationDto;
import org.wso2.eventsimulator.core.simulator.csvFeedSimulation.core.FileDto;
import org.wso2.eventsimulator.core.simulator.databaseFeedSimulation.DatabaseFeedSimulationDto;
import org.wso2.eventsimulator.core.simulator.singleventsimulator.SingleEventDto;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.io.IOException;
import java.util.ArrayList;
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
     * Convert the RandomFeedSimulationConfig string into RandomDataSimulationDto Object
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
     * @param RandomFeedSimulationConfig RandomEventSimulationConfiguration String
     * @return RandomDataSimulationDto Object
     */
    public static RandomSimulationDto randomDataSimulatorParser(JSONObject RandomFeedSimulationConfig) {
        RandomSimulationDto randomSimulationDto = new RandomSimulationDto();

       try {
           //set properties to RandomDataSimulationDto
           if (RandomFeedSimulationConfig.has(EventSimulatorConstants.STREAM_NAME) && !RandomFeedSimulationConfig.getString(EventSimulatorConstants.STREAM_NAME).isEmpty()) {
               randomSimulationDto.setStreamName(RandomFeedSimulationConfig.getString(EventSimulatorConstants.STREAM_NAME));
           } else {
               log.error("Stream name can not be null or an empty value");
               throw new RuntimeException("Stream name can not be null or an empty value");
           }
           if (RandomFeedSimulationConfig.has(EventSimulatorConstants.EXECUTION_PLAN_NAME) && !RandomFeedSimulationConfig.getString(EventSimulatorConstants.EXECUTION_PLAN_NAME).isEmpty()) {
               randomSimulationDto.setExecutionPlanName(RandomFeedSimulationConfig.getString(EventSimulatorConstants.EXECUTION_PLAN_NAME));
           } else {
               log.error("Execution plan name can not be null or an empty value");
               throw new RuntimeException("Execution plan name can not be null or an empty value");
           }
           if (RandomFeedSimulationConfig.has(EventSimulatorConstants.TIME_INTERVAL) && !RandomFeedSimulationConfig.getString(EventSimulatorConstants.TIME_INTERVAL).isEmpty()) {
               randomSimulationDto.setTimeInterval(RandomFeedSimulationConfig.getLong(EventSimulatorConstants.TIME_INTERVAL));
           } else {
               log.warn("Time interval cannot be null or an empty value. Time interval is set to 0 milliseconds");
               randomSimulationDto.setTimeInterval(0L);
           }

           List<Attribute> streamAttributes = EventSimulatorDataHolder
                   .getInstance().getEventStreamService().getStreamAttributes(randomSimulationDto.getExecutionPlanName(),randomSimulationDto.getStreamName());
           List<RandomAttributeDto> attributeConfigurations = new ArrayList<>();

           JSONArray attributeConfigArray;
           if (RandomFeedSimulationConfig.has(EventSimulatorConstants.ATTRIBUTE_CONFIGURATION) && RandomFeedSimulationConfig.getJSONArray(EventSimulatorConstants.ATTRIBUTE_CONFIGURATION).length() > 0) {
               attributeConfigArray = RandomFeedSimulationConfig.getJSONArray(EventSimulatorConstants.ATTRIBUTE_CONFIGURATION);
           } else {
               log.error("Attribute configuration cannot be null or an empty value");
               throw new RuntimeException("Attribute configuration cannot be null or an empty value");
           }

           if (attributeConfigArray.length() != streamAttributes.size()) {
               throw new RuntimeException("Random feed simulation of stream '" + randomSimulationDto.getStreamName() +
                       "' requires attribute configurations for " + streamAttributes.size() + " attributes. Number of attribute" +
                       " configurations provided is " + attributeConfigArray.length());
           }

           //convert each attribute simulation configuration as relevant objects

           RandomAttributeDto.RandomDataGeneratorType type;

           for (int i = 0; i < attributeConfigArray.length(); i++) {

               if (!attributeConfigArray.getJSONObject(i).isNull(EventSimulatorConstants.RANDOM_DATA_GENERATOR_TYPE)
                       && attributeConfigArray.getJSONObject(i).has(EventSimulatorConstants.RANDOM_DATA_GENERATOR_TYPE)
                       && !attributeConfigArray.getJSONObject(i).getString(EventSimulatorConstants.RANDOM_DATA_GENERATOR_TYPE).isEmpty()) {

                   try {
                       type = RandomAttributeDto.RandomDataGeneratorType.valueOf(attributeConfigArray.getJSONObject(i)
                               .getString(EventSimulatorConstants.RANDOM_DATA_GENERATOR_TYPE));
                   } catch (IllegalArgumentException e) {
                       log.error("Invalid random generation type. Generation type must be either '" + RandomAttributeDto.RandomDataGeneratorType.CUSTOM_DATA_BASED +
                               "' or '" + RandomAttributeDto.RandomDataGeneratorType.PRIMITIVE_BASED + "' or '" + RandomAttributeDto.RandomDataGeneratorType.PROPERTY_BASED
                               +  "' or '" + RandomAttributeDto.RandomDataGeneratorType.REGEX_BASED + "'.");
                       throw new RuntimeException("Invalid random generation type. Generation type must be either '" + RandomAttributeDto.RandomDataGeneratorType.CUSTOM_DATA_BASED +
                               "' or '" + RandomAttributeDto.RandomDataGeneratorType.PRIMITIVE_BASED + "' or '" + RandomAttributeDto.RandomDataGeneratorType.PROPERTY_BASED
                               +  "' or '" + RandomAttributeDto.RandomDataGeneratorType.REGEX_BASED + "'.");
                   }

                   switch (type) {

                       case PROPERTY_BASED:
                           PropertyBasedAttributeDto propertyBasedAttributeDto = new PropertyBasedAttributeDto();

                           if (attributeConfigArray.getJSONObject(i).has(EventSimulatorConstants.PROPERTY_BASED_ATTRIBUTE_CATEGORY)
                                   && !attributeConfigArray.getJSONObject(i).isNull(EventSimulatorConstants.PROPERTY_BASED_ATTRIBUTE_CATEGORY)
                                   && !attributeConfigArray.getJSONObject(i).getString(EventSimulatorConstants.PROPERTY_BASED_ATTRIBUTE_CATEGORY).isEmpty()) {

                               propertyBasedAttributeDto.setType(RandomAttributeDto.RandomDataGeneratorType.PROPERTY_BASED);
                               propertyBasedAttributeDto.setCategory(attributeConfigArray.getJSONObject(i).getString(EventSimulatorConstants.PROPERTY_BASED_ATTRIBUTE_CATEGORY));
                           } else {
                               log.error("Category value cannot be null or an empty value for "
                                       + RandomAttributeDto.RandomDataGeneratorType.PROPERTY_BASED + " simulation.");
                               throw new RuntimeException("Category value cannot be null or an empty value for "
                                       + RandomAttributeDto.RandomDataGeneratorType.PROPERTY_BASED + " simulation.");
                           }

                           if (attributeConfigArray.getJSONObject(i).has(EventSimulatorConstants.PROPERTY_BASED_ATTRIBUTE_PROPERTY)
                                   && !attributeConfigArray.getJSONObject(i).isNull(EventSimulatorConstants.PROPERTY_BASED_ATTRIBUTE_PROPERTY)
                                   && !attributeConfigArray.getJSONObject(i).getString(EventSimulatorConstants.PROPERTY_BASED_ATTRIBUTE_PROPERTY).isEmpty()) {

                               propertyBasedAttributeDto.setProperty(attributeConfigArray.getJSONObject(i).getString(EventSimulatorConstants.PROPERTY_BASED_ATTRIBUTE_PROPERTY));
                           } else {
                               log.error("Property value cannot be null or an empty value for "
                                       + RandomAttributeDto.RandomDataGeneratorType.PROPERTY_BASED + " simulation.");
                               throw new RuntimeException("Property value cannot be null or an empty value for "
                                       + RandomAttributeDto.RandomDataGeneratorType.PROPERTY_BASED + " simulation.");
                           }
                           attributeConfigurations.add(propertyBasedAttributeDto);
                           break;

                       case REGEX_BASED:
                           if (attributeConfigArray.getJSONObject(i).has(EventSimulatorConstants.REGEX_BASED_ATTRIBUTE_PATTERN)
                                   && !attributeConfigArray.getJSONObject(i).isNull(EventSimulatorConstants.REGEX_BASED_ATTRIBUTE_PATTERN)
                                   && !attributeConfigArray.getJSONObject(i).getString(EventSimulatorConstants.REGEX_BASED_ATTRIBUTE_PATTERN).isEmpty()) {

                               RegexBasedGenerator.validateRegularExpression(attributeConfigArray.getJSONObject(i)
                                       .getString(EventSimulatorConstants.REGEX_BASED_ATTRIBUTE_PATTERN));
                               RegexBasedAttributeDto regexBasedAttributeDto = new RegexBasedAttributeDto();
                               regexBasedAttributeDto.setType(RandomAttributeDto.RandomDataGeneratorType.REGEX_BASED);
                               regexBasedAttributeDto.setPattern(attributeConfigArray.getJSONObject(i)
                                       .getString(EventSimulatorConstants.REGEX_BASED_ATTRIBUTE_PATTERN));
                               attributeConfigurations.add(regexBasedAttributeDto);
                           } else {
                               log.error("Pattern should not be null or an empty value for " +
                                       RandomAttributeDto.RandomDataGeneratorType.REGEX_BASED + " simulation.");
                               throw new RuntimeException("Pattern should not be null or an empty value for " +
                                       RandomAttributeDto.RandomDataGeneratorType.REGEX_BASED + " simulation.");
                           }
                           break;

                       case PRIMITIVE_BASED:
                           PrimitiveBasedAttributeDto primitiveBasedAttributeDto = new PrimitiveBasedAttributeDto();
                           primitiveBasedAttributeDto.setType(RandomAttributeDto.RandomDataGeneratorType.PRIMITIVE_BASED);
                           Attribute.Type attrType;

                           try {
                               attrType = Attribute.Type.valueOf(attributeConfigArray.getJSONObject(i).getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_TYPE));
                               primitiveBasedAttributeDto.setAttrType(attrType);
                           } catch (IllegalArgumentException e) {
                               log.error("Invalid attribute type '" + attributeConfigArray.getJSONObject(i).getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_TYPE) + "'.");
                               throw new RuntimeException("Invalid attribute type '" +
                                       attributeConfigArray.getJSONObject(i).getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_TYPE) + "'.");
                           }

                           switch (attrType) {

                               case BOOL:
                                   break;

                               case STRING:
                                   if (attributeConfigArray.getJSONObject(i).has(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_LENGTH_DECIMAL)
                                           && !attributeConfigArray.getJSONObject(i).isNull(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_LENGTH_DECIMAL)
                                           && !attributeConfigArray.getJSONObject(i).getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_LENGTH_DECIMAL).isEmpty()) {

                                       primitiveBasedAttributeDto.setLength(attributeConfigArray.getJSONObject(i).getInt(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_LENGTH_DECIMAL));

                                   } else {
                                       log.error("Length should not be null or an empty value for type '" + attrType + "' in " +
                                               RandomAttributeDto.RandomDataGeneratorType.PRIMITIVE_BASED + " simulation.");
                                       throw new RuntimeException("Length should not be null or an empty value for type '" + attrType + "' in " +
                                               RandomAttributeDto.RandomDataGeneratorType.PRIMITIVE_BASED + " simulation.");
                                   }
                                   break;

                               case INT:
                               case LONG:
                                   if (attributeConfigArray.getJSONObject(i).has(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MIN)
                                           && !attributeConfigArray.getJSONObject(i).isNull(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MIN)
                                           && !attributeConfigArray.getJSONObject(i).getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MIN).isEmpty()
                                           && attributeConfigArray.getJSONObject(i).has(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MAX)
                                           && !attributeConfigArray.getJSONObject(i).isNull(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MAX)
                                           && !attributeConfigArray.getJSONObject(i).getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MAX).isEmpty()) {

                                       primitiveBasedAttributeDto.setMin(attributeConfigArray.getJSONObject(i).getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MIN));
                                       primitiveBasedAttributeDto.setMax(attributeConfigArray.getJSONObject(i).getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MAX));

                                   } else {
                                       log.error("Min and Max value is required for type '" + attrType + "' in"
                                               + RandomAttributeDto.RandomDataGeneratorType.PRIMITIVE_BASED + " simulation.");
                                       throw new RuntimeException("Min and Max value is required for type '" + attrType+ "' in"
                                               + RandomAttributeDto.RandomDataGeneratorType.PRIMITIVE_BASED + " simulation.");
                                   }
                                   break;

                               case FLOAT:
                               case DOUBLE:
                                   if (attributeConfigArray.getJSONObject(i).has(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MIN)
                                           && !attributeConfigArray.getJSONObject(i).isNull(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MIN)
                                           && !attributeConfigArray.getJSONObject(i).getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MIN).isEmpty()
                                           && attributeConfigArray.getJSONObject(i).has(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MAX)
                                           && !attributeConfigArray.getJSONObject(i).isNull(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MAX)
                                           && !attributeConfigArray.getJSONObject(i).getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MAX).isEmpty()
                                           && attributeConfigArray.getJSONObject(i).has(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_LENGTH_DECIMAL)
                                           && !attributeConfigArray.getJSONObject(i).isNull(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_LENGTH_DECIMAL)
                                           && !attributeConfigArray.getJSONObject(i).getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_LENGTH_DECIMAL).isEmpty()) {

                                       primitiveBasedAttributeDto.setMin(attributeConfigArray.getJSONObject(i).getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MIN));
                                       primitiveBasedAttributeDto.setMax(attributeConfigArray.getJSONObject(i).getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MAX));
                                       primitiveBasedAttributeDto.setLength(attributeConfigArray.getJSONObject(i).getInt(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_LENGTH_DECIMAL));

                                   } else {
                                       log.error("Min,Max and length value is required for type '" + attrType + "' in "
                                               + RandomAttributeDto.RandomDataGeneratorType.PRIMITIVE_BASED + " simulation.");
                                       throw new RuntimeException("Min,Max and length value is required for type '" + attrType+ "' in "
                                               + RandomAttributeDto.RandomDataGeneratorType.PRIMITIVE_BASED + " simulation.");
                                   }
                                   break;
                           }
                           attributeConfigurations.add(primitiveBasedAttributeDto);
                           break;

                       case CUSTOM_DATA_BASED:
                           if (attributeConfigArray.getJSONObject(i).has(EventSimulatorConstants.CUSTOM_DATA_BASED_ATTRIBUTE_LIST)
                                   && !attributeConfigArray.getJSONObject(i).isNull(EventSimulatorConstants.CUSTOM_DATA_BASED_ATTRIBUTE_LIST)
                                   && !attributeConfigArray.getJSONObject(i).getString(EventSimulatorConstants.CUSTOM_DATA_BASED_ATTRIBUTE_LIST).isEmpty()) {

                               CustomBasedAttributeDto customBasedAttributeDto = new CustomBasedAttributeDto();
                               customBasedAttributeDto.setType(RandomAttributeDto.RandomDataGeneratorType.CUSTOM_DATA_BASED);
                               customBasedAttributeDto.setCustomData(attributeConfigArray.getJSONObject(i).getString(EventSimulatorConstants.CUSTOM_DATA_BASED_ATTRIBUTE_LIST));
                               attributeConfigurations.add(customBasedAttributeDto);
                           } else {
                               log.error("Data list is not given for " + RandomAttributeDto.RandomDataGeneratorType.CUSTOM_DATA_BASED + " simulation.");
                               throw new RuntimeException("Data list is not given for " +
                                       RandomAttributeDto.RandomDataGeneratorType.CUSTOM_DATA_BASED + " simulation.");
                           }
                           break;
                   }
               }
           }
           randomSimulationDto.setAttributeConfigurations(attributeConfigurations);
       } catch (JSONException e) {
           log.error("Error occurred when accessing stream configuration : " + e.getMessage());
           throw new RuntimeException("Error occurred when accessing stream configuration : " + e.getMessage());
       }
        return randomSimulationDto;
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
                throw new RuntimeException("Number of attribute values is not equal to number of attributes in stream '" +
                        singleEventDto.getStreamName() + "' . Required number of attributes : " +
                        streamAttributes.size());
            }
        } catch (IOException e) {
            log.error("Exception occurred when parsing json to Object ");
            throw new RuntimeException("Exception occurred when parsing json to Object : " + e.getMessage());
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
    public static CSVFileSimulationDto fileFeedSimulatorParser(JSONObject csvFileDetail) {
        CSVFileSimulationDto csvFileSimulationDto = new CSVFileSimulationDto();


        if (csvFileDetail.has(EventSimulatorConstants.STREAM_NAME) && !csvFileDetail.getString(EventSimulatorConstants.STREAM_NAME).isEmpty()) {
            csvFileSimulationDto.setStreamName(csvFileDetail.getString(EventSimulatorConstants.STREAM_NAME));
        } else {
            log.error("Stream name cannot be null or an empty value");
            throw new RuntimeException("Stream name cannot be null or an empty value");
        }
        if (csvFileDetail.has(EventSimulatorConstants.EXECUTION_PLAN_NAME) && !csvFileDetail.getString(EventSimulatorConstants.EXECUTION_PLAN_NAME).isEmpty()) {
            csvFileSimulationDto.setExecutionPlanName(csvFileDetail.getString(EventSimulatorConstants.EXECUTION_PLAN_NAME));
        } else {
            log.error("Execution plan name cannot be null or an empty value");
            throw new RuntimeException("Execution plan name cannot be null or an empty value");
        }
        if (csvFileDetail.has(EventSimulatorConstants.FILE_NAME) && !csvFileDetail.getString(EventSimulatorConstants.FILE_NAME).isEmpty()) {
            csvFileSimulationDto.setFileName(csvFileDetail.getString(EventSimulatorConstants.FILE_NAME));
        } else {
            log.error("File name cannot be null or an empty value");
            throw new RuntimeException("File name cannot be null or an empty value");
        }
        if (csvFileDetail.has(EventSimulatorConstants.TIMESTAMP_POSITION) && !csvFileDetail.getString(EventSimulatorConstants.TIMESTAMP_POSITION).isEmpty()) {
            csvFileSimulationDto.setTimestampAttribute(csvFileDetail.getString(EventSimulatorConstants.TIMESTAMP_POSITION));
        } else {
            log.error("Timestamp position cannot be null or an empty value");
            throw new RuntimeException("Timestamp position cannot be null or an empty value");
        }
        if (csvFileDetail.has(EventSimulatorConstants.DELIMITER) && !csvFileDetail.getString(EventSimulatorConstants.DELIMITER).isEmpty()) {
            csvFileSimulationDto.setDelimiter((String) csvFileDetail.get(EventSimulatorConstants.DELIMITER));
        } else {
            log.error("Delimiter cannot be null or an empty value");
            throw new RuntimeException("Delimiter cannot be null or an empty value");
        }
        if (csvFileDetail.has(EventSimulatorConstants.IS_ORDERED) && !csvFileDetail.getString(EventSimulatorConstants.IS_ORDERED).isEmpty()) {
            csvFileSimulationDto.setIsOrdered(csvFileDetail.getBoolean(EventSimulatorConstants.IS_ORDERED));
        } else {
            log.error("isOrdered flag cannot be null or an empty value");
            throw new RuntimeException("isOrdered flag cannot be null or an empty value");
        }

        //get the fileDto from FileStore if file exist and set this value.
        FileDto fileDto;
        if (FileStore.getFileStore().checkExists(csvFileSimulationDto.getFileName())) {
            fileDto = FileStore.getFileStore().getFileInfoMap().get(csvFileSimulationDto.getFileName());
            csvFileSimulationDto.setFileDto(fileDto);
        } else {
            log.error("File '" + csvFileSimulationDto.getFileName() + "' has not been uploaded.");
            throw new RuntimeException("File '" + csvFileSimulationDto.getFileName() + "' has not been uploaded.");
        }

        return csvFileSimulationDto;
    }

    /**
     * Convert the database configuration file into a DatabaseFeedSimulationDto object
     *
     * @param databaseConfigurations : database configuration string
     * @return a DatabaseFeedSimulationDto object
     */

    public static DatabaseFeedSimulationDto databaseFeedSimulationParser(JSONObject databaseConfigurations) {

        DatabaseFeedSimulationDto databaseFeedSimulationDto = new DatabaseFeedSimulationDto();

//       assign values for database configuration attributes


        if (databaseConfigurations.has(EventSimulatorConstants.DATABASE_NAME) && !databaseConfigurations.getString(EventSimulatorConstants.DATABASE_NAME).isEmpty()) {
            databaseFeedSimulationDto.setDatabaseName(databaseConfigurations.getString(EventSimulatorConstants.DATABASE_NAME));
        } else {
            log.error("Database name cannot be null or an empty value");
            throw new RuntimeException("Database name cannot be null or an empty value");
        }
        if (databaseConfigurations.has(EventSimulatorConstants.USER_NAME) && !databaseConfigurations.getString(EventSimulatorConstants.USER_NAME).isEmpty()) {
            databaseFeedSimulationDto.setUsername(databaseConfigurations.getString(EventSimulatorConstants.USER_NAME));
        } else {
            log.error("Username can not be null or an empty value");
            throw new RuntimeException("Username cannot be null or an empty value");
        }
        if (databaseConfigurations.has(EventSimulatorConstants.PASSWORD) && !databaseConfigurations.getString(EventSimulatorConstants.PASSWORD).isEmpty()) {
            databaseFeedSimulationDto.setPassword(databaseConfigurations.getString(EventSimulatorConstants.PASSWORD));
        } else {
            log.error("Password cannot be null or an empty value");
            throw new RuntimeException("Password cannot be null or an empty value");
        }
        if (databaseConfigurations.has(EventSimulatorConstants.TABLE_NAME) && !databaseConfigurations.getString(EventSimulatorConstants.TABLE_NAME).isEmpty()) {
            databaseFeedSimulationDto.setTableName(databaseConfigurations.getString(EventSimulatorConstants.TABLE_NAME));
        } else {
            log.error("Table name cannot be null or an empty value");
            throw new RuntimeException("Table name cannot be null or an empty value");
        }
        if (databaseConfigurations.has(EventSimulatorConstants.STREAM_NAME) && !databaseConfigurations.getString(EventSimulatorConstants.STREAM_NAME).isEmpty()) {
            databaseFeedSimulationDto.setStreamName(databaseConfigurations.getString(EventSimulatorConstants.STREAM_NAME));
        } else {
            log.error("Stream name cannot be null or an empty value");
            throw new RuntimeException("Stream name cannot be null or an empty value");
        }
        if (databaseConfigurations.has(EventSimulatorConstants.EXECUTION_PLAN_NAME) && !databaseConfigurations.getString(EventSimulatorConstants.EXECUTION_PLAN_NAME).isEmpty()) {
            databaseFeedSimulationDto.setExecutionPlanName(databaseConfigurations.getString(EventSimulatorConstants.EXECUTION_PLAN_NAME));
        } else {
            log.error("Execution plan name cannot be null or an empty value");
            throw new RuntimeException("Execution plan name cannot be null or an empty value");
        }
        if (databaseConfigurations.has(EventSimulatorConstants.TIMESTAMP_ATTRIBUTE) && !databaseConfigurations.getString(EventSimulatorConstants.TIMESTAMP_ATTRIBUTE).isEmpty()) {
            databaseFeedSimulationDto.setTimestampAttribute(databaseConfigurations.getString(EventSimulatorConstants.TIMESTAMP_ATTRIBUTE));
        } else {
            log.error("Timestamp attribute cannot be null or an empty value");
            throw new RuntimeException("Timestamp attribute cannot be null or an empty value");
        }

//      insert the specified column names into a list and set it to database configuration;
        if (databaseConfigurations.has(EventSimulatorConstants.COLUMN_NAMES_LIST) && !databaseConfigurations.getString(EventSimulatorConstants.COLUMN_NAMES_LIST).isEmpty()) {
            databaseFeedSimulationDto.setColumnNames(databaseConfigurations.getString(EventSimulatorConstants.COLUMN_NAMES_LIST));
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
            JSONArray attributeConfigArray = null;

            if (jsonObject.has(EventSimulatorConstants.FEED_SIMULATION_STREAM_CONFIGURATION)
                    && jsonObject.getJSONArray(EventSimulatorConstants.FEED_SIMULATION_STREAM_CONFIGURATION).length() > 0) {
                attributeConfigArray = jsonObject.getJSONArray(EventSimulatorConstants.FEED_SIMULATION_STREAM_CONFIGURATION);
            } else {
                log.error("Stream configuration cannot be null or empty");
                throw new RuntimeException("Stream configuration cannot be null or empty");
            }

            if (jsonObject.has(EventSimulatorConstants.ORDER_BY_TIMESTAMP) && !jsonObject.getString(EventSimulatorConstants.ORDER_BY_TIMESTAMP).isEmpty()) {
                if (jsonObject.getBoolean(EventSimulatorConstants.ORDER_BY_TIMESTAMP)) {
                    feedSimulationDto.setOrderByTimeStamp(jsonObject.getBoolean(EventSimulatorConstants.ORDER_BY_TIMESTAMP));
//                todo 01/03/2017 currently if orderByTimestamp flag is set to true, its assumed that the entire simulation configuration generates events to one stream. should this be changed?
//                set the number of data sources used to generate event as 'NoOfParallelSimulationSources'
                    feedSimulationDto.setNoOfParallelSimulationSources(attributeConfigArray.length());
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

            for (int i = 0; i < attributeConfigArray.length(); i++) {
                if (!attributeConfigArray.getJSONObject(i).isNull(EventSimulatorConstants.FEED_SIMULATION_TYPE)
                        && !attributeConfigArray.getJSONObject(i).getString(EventSimulatorConstants.FEED_SIMULATION_TYPE).isEmpty()) {
                    FeedSimulationStreamConfiguration.SimulationType feedSimulationType = FeedSimulationStreamConfiguration.SimulationType.valueOf
                            (attributeConfigArray.getJSONObject(i).getString(EventSimulatorConstants.FEED_SIMULATION_TYPE).toUpperCase());

                    switch (feedSimulationType) {
                        case RANDOM_DATA_SIMULATION:

                        case FILE_SIMULATION:
                            break;

                        case DATABASE_SIMULATION:
                            break;
                        default:
                            log.error("Invalid simulation type '" + feedSimulationType + "'. Valid simulation types : '" +
                                    FeedSimulationStreamConfiguration.SimulationType.RANDOM_DATA_SIMULATION
                                    + "' or '" + FeedSimulationStreamConfiguration.SimulationType.FILE_SIMULATION + "' or '" +
                                    FeedSimulationStreamConfiguration.SimulationType.DATABASE_SIMULATION + "'");
                            throw new RuntimeException("Invalid simulation type '" + feedSimulationType +
                                    "'. Valid simulation types : '" + FeedSimulationStreamConfiguration.SimulationType.RANDOM_DATA_SIMULATION
                                    + "' or '" + FeedSimulationStreamConfiguration.SimulationType.FILE_SIMULATION + "' or '" +
                                    FeedSimulationStreamConfiguration.SimulationType.DATABASE_SIMULATION + "'");
                    }
                } else {
                    log.error("Simulation type is not specified");
                    throw new RuntimeException("Simulation type  is not specified. Simulation types : '"
                            + FeedSimulationStreamConfiguration.SimulationType.RANDOM_DATA_SIMULATION
                            + "' or '" + FeedSimulationStreamConfiguration.SimulationType.FILE_SIMULATION + "' or '" +
                            FeedSimulationStreamConfiguration.SimulationType.DATABASE_SIMULATION + "'");
                }
            }
            feedSimulationDto.setStreamConfigurationList(feedSimulationStreamConfigurationList);
            return feedSimulationDto;
    }

}
