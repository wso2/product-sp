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
package org.wso2.eventsimulator.core.eventGenerator.util;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.eventsimulator.core.eventGenerator.bean.CSVFileSimulationDto;
import org.wso2.eventsimulator.core.eventGenerator.bean.DatabaseFeedSimulationDto;
import org.wso2.eventsimulator.core.eventGenerator.bean.RandomSimulationDto;
import org.wso2.eventsimulator.core.eventGenerator.bean.SingleEventDto;
import org.wso2.eventsimulator.core.eventGenerator.csvEventGeneration.bean.FileDto;
import org.wso2.eventsimulator.core.eventGenerator.csvEventGeneration.util.FileStore;
import org.wso2.eventsimulator.core.eventGenerator.randomEventGeneration.bean.CustomBasedAttributeDto;
import org.wso2.eventsimulator.core.eventGenerator.randomEventGeneration.bean.PrimitiveBasedAttributeDto;
import org.wso2.eventsimulator.core.eventGenerator.randomEventGeneration.bean.PropertyBasedAttributeDto;
import org.wso2.eventsimulator.core.eventGenerator.randomEventGeneration.bean.RandomAttributeDto;
import org.wso2.eventsimulator.core.eventGenerator.randomEventGeneration.bean.RegexBasedAttributeDto;
import org.wso2.eventsimulator.core.eventGenerator.randomEventGeneration.util.RegexBasedGenerator;
import org.wso2.eventsimulator.core.eventGenerator.util.constants.EventSimulatorConstants;
import org.wso2.eventsimulator.core.eventGenerator.util.exceptions.ConfigurationParserException;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.ArrayList;
import java.util.List;


/**
 * StreamConfigurationParser is an util class used to
 * convert Json string into relevant event simulation configuration object
 */
public class StreamConfigurationParser {
    private static final Logger log = Logger.getLogger(StreamConfigurationParser.class);

    /*
    Initialize StreamConfigurationParser
     */
    private StreamConfigurationParser() {
    }


    /**
     * Convert the singleEventConfiguration string into SingleEventDto Object
     *
     * @param singleEventConfiguration String containing single event simulation configuration
     * @return SingleEventDto Object
     */
    public static SingleEventDto singleEventSimulatorParser(String singleEventConfiguration) {
        SingleEventDto singleEventDto = new SingleEventDto();
        JSONObject singleEventConfig = new JSONObject(singleEventConfiguration);

        /*
         * assign properties to SingleDto object
         * perform the following checks prior to assigning properties
         *
         * 1. has
         * 2. isNull
         * 3. isEmpty
         *
         * assign property if all 3 checks are successful
         * else, throw an exception
         * */
        try {

            if (singleEventConfig.has(EventSimulatorConstants.STREAM_NAME)
                    && !singleEventConfig.isNull(EventSimulatorConstants.STREAM_NAME)
                    && !singleEventConfig.getString(EventSimulatorConstants.STREAM_NAME).isEmpty()) {

                singleEventDto.setStreamName(singleEventConfig
                        .getString(EventSimulatorConstants.STREAM_NAME));

            } else {
                throw new ConfigurationParserException("Stream name is required");
            }

            if (singleEventConfig.has(EventSimulatorConstants.EXECUTION_PLAN_NAME)
                    && !singleEventConfig.isNull(EventSimulatorConstants.EXECUTION_PLAN_NAME)
                    && !singleEventConfig.getString(EventSimulatorConstants.EXECUTION_PLAN_NAME).isEmpty()) {

                singleEventDto.setExecutionPlanName(singleEventConfig
                        .getString(EventSimulatorConstants.EXECUTION_PLAN_NAME));

            } else {
                throw new ConfigurationParserException("Execution plan name is required");
            }
            if (singleEventConfig.has(EventSimulatorConstants.SINGLE_EVENT_TIMESTAMP)
                    && !singleEventConfig.isNull(EventSimulatorConstants.SINGLE_EVENT_TIMESTAMP)
                    && !singleEventConfig.getString(EventSimulatorConstants.SINGLE_EVENT_TIMESTAMP).isEmpty()) {

                singleEventDto.setTimestamp(singleEventConfig.getLong(EventSimulatorConstants.SINGLE_EVENT_TIMESTAMP));
            } else {
                throw new ConfigurationParserException("Single event simulation requires a timestamp value");

            }

            if (singleEventConfig.has(EventSimulatorConstants.SINGLE_EVENT_DATA)
                    && !singleEventConfig.isNull(EventSimulatorConstants.SINGLE_EVENT_DATA)
                    && !singleEventConfig.getString(EventSimulatorConstants.SINGLE_EVENT_DATA).isEmpty()) {

                singleEventDto.setAttributeValues(singleEventConfig
                        .getString(EventSimulatorConstants.SINGLE_EVENT_DATA));

            } else {
                throw new ConfigurationParserException("Single event simulation requires a attribute value for " +
                        "stream '" + singleEventDto.getStreamName() + "'.");
            }

        } catch (ConfigurationParserException e) {
            log.error("Error occurred when parsing single event simulation configuration : "
                    + e.getMessage(), e);
        } catch (JSONException e) {
            log.error("Error occurred when accessing single event simulation configuration : "
                    + e.getMessage(), e);
        }

        return singleEventDto;
    }


    /**
     * Convert the RandomFeedSimulationConfig JSONObject into RandomSimulationDto Object
     * <p>
     * RandomFeedSimulationConfig can have one or more attribute configurations of the following types
     * 1.PRIMITIVEBASED : String/Integer/Long/Float/Double/Boolean
     * 2.PROPERTYBASED  : generates meaning full data.
     * 3.REGEXBASED     : generates data using given regex
     * 4.CUSTOMDATA     : generates data using a given data list
     * <p>
     *
     * @param RandomFeedSimulationConfig JSON object containing configuration for random simulation
     * @return RandomSimulationDto Object
     */
    public static RandomSimulationDto randomDataSimulatorParser(JSONObject RandomFeedSimulationConfig) {
        RandomSimulationDto randomSimulationDto = new RandomSimulationDto();

            /*
            * set properties to RandomSimulationDto.
            *
            * Perform the following checks prior to setting the properties.
            * 1. has
            * 2. isNull
            * 3. isEmpty
            *
            * if any of the above checks fail, throw an exception indicating which property is missing.
            * */

        try {

            if (RandomFeedSimulationConfig.has(EventSimulatorConstants.STREAM_NAME)
                    && !RandomFeedSimulationConfig.isNull(EventSimulatorConstants.STREAM_NAME)
                    && !RandomFeedSimulationConfig.getString(EventSimulatorConstants.STREAM_NAME).isEmpty()) {

                randomSimulationDto.setStreamName(RandomFeedSimulationConfig
                        .getString(EventSimulatorConstants.STREAM_NAME));

            } else {
                throw new ConfigurationParserException("Stream name is required");
            }

            if (RandomFeedSimulationConfig.has(EventSimulatorConstants.EXECUTION_PLAN_NAME)
                    && !RandomFeedSimulationConfig.isNull(EventSimulatorConstants.EXECUTION_PLAN_NAME)
                    && !RandomFeedSimulationConfig.getString(EventSimulatorConstants.EXECUTION_PLAN_NAME).isEmpty()) {

                randomSimulationDto.setExecutionPlanName(RandomFeedSimulationConfig
                        .getString(EventSimulatorConstants.EXECUTION_PLAN_NAME));

            } else {
                throw new ConfigurationParserException("Execution plan name is required");
            }
            if (RandomFeedSimulationConfig.has(EventSimulatorConstants.TIME_INTERVAL)
                    && !RandomFeedSimulationConfig.isNull(EventSimulatorConstants.TIME_INTERVAL)
                    && !RandomFeedSimulationConfig.getString(EventSimulatorConstants.TIME_INTERVAL).isEmpty()) {

                randomSimulationDto.setTimeInterval(RandomFeedSimulationConfig
                        .getLong(EventSimulatorConstants.TIME_INTERVAL));

            } else {
                log.warn("Time interval is required. Time interval is set to 0 milliseconds");
                randomSimulationDto.setTimeInterval(0L);
            }

            List<RandomAttributeDto> attributeConfigurations = new ArrayList<>();

            JSONArray attributeConfigArray;
            if (RandomFeedSimulationConfig.has(EventSimulatorConstants.ATTRIBUTE_CONFIGURATION)
                    && !RandomFeedSimulationConfig.isNull(EventSimulatorConstants.ATTRIBUTE_CONFIGURATION)
                    && RandomFeedSimulationConfig.getJSONArray(EventSimulatorConstants.ATTRIBUTE_CONFIGURATION)
                    .length() > 0) {

                attributeConfigArray = RandomFeedSimulationConfig
                        .getJSONArray(EventSimulatorConstants.ATTRIBUTE_CONFIGURATION);

            } else {
                throw new ConfigurationParserException("Attribute configuration is required");
            }

            //convert each attribute simulation configuration as relevant objects

            RandomAttributeDto.RandomDataGeneratorType type;

            for (int i = 0; i < attributeConfigArray.length(); i++) {

                if (attributeConfigArray.getJSONObject(i).has(EventSimulatorConstants.RANDOM_DATA_GENERATOR_TYPE)
                        && !attributeConfigArray.getJSONObject(i)
                        .isNull(EventSimulatorConstants.RANDOM_DATA_GENERATOR_TYPE)
                        && !attributeConfigArray.getJSONObject(i)
                        .getString(EventSimulatorConstants.RANDOM_DATA_GENERATOR_TYPE).isEmpty()) {

                    try {
                        /*
                        * for each attribute configuration, switch using the random generation type and create
                        * respective attribute configuration objects.
                        * */
                        type = RandomAttributeDto.RandomDataGeneratorType.valueOf(attributeConfigArray.getJSONObject(i)
                                .getString(EventSimulatorConstants.RANDOM_DATA_GENERATOR_TYPE));
                    } catch (IllegalArgumentException e) {
                        throw new ConfigurationParserException("Invalid random generation type. Generation type must " +
                                "be either '" + RandomAttributeDto.RandomDataGeneratorType.CUSTOM_DATA_BASED + "' or '" +
                                RandomAttributeDto.RandomDataGeneratorType.PRIMITIVE_BASED + "' or '" +
                                RandomAttributeDto.RandomDataGeneratorType.PROPERTY_BASED + "' or '" +
                                RandomAttributeDto.RandomDataGeneratorType.REGEX_BASED + "'.");
                    }

                    switch (type) {

                        case PROPERTY_BASED:
                            PropertyBasedAttributeDto propertyBasedAttributeDto = new PropertyBasedAttributeDto();

                            if (attributeConfigArray.getJSONObject(i)
                                    .has(EventSimulatorConstants.PROPERTY_BASED_ATTRIBUTE_CATEGORY)
                                    && !attributeConfigArray.getJSONObject(i)
                                    .isNull(EventSimulatorConstants.PROPERTY_BASED_ATTRIBUTE_CATEGORY)
                                    && !attributeConfigArray.getJSONObject(i)
                                    .getString(EventSimulatorConstants.PROPERTY_BASED_ATTRIBUTE_CATEGORY).isEmpty()) {

                                propertyBasedAttributeDto
                                        .setType(RandomAttributeDto.RandomDataGeneratorType.PROPERTY_BASED);
                                propertyBasedAttributeDto.setCategory(attributeConfigArray.getJSONObject(i)
                                        .getString(EventSimulatorConstants.PROPERTY_BASED_ATTRIBUTE_CATEGORY));
                            } else {
                                throw new ConfigurationParserException("Category value is required for "
                                        + RandomAttributeDto.RandomDataGeneratorType.PROPERTY_BASED + " simulation.");
                            }

                            if (attributeConfigArray.getJSONObject(i)
                                    .has(EventSimulatorConstants.PROPERTY_BASED_ATTRIBUTE_PROPERTY)
                                    && !attributeConfigArray.getJSONObject(i)
                                    .isNull(EventSimulatorConstants.PROPERTY_BASED_ATTRIBUTE_PROPERTY)
                                    && !attributeConfigArray.getJSONObject(i)
                                    .getString(EventSimulatorConstants.PROPERTY_BASED_ATTRIBUTE_PROPERTY).isEmpty()) {

                                propertyBasedAttributeDto.setProperty(attributeConfigArray.getJSONObject(i)
                                        .getString(EventSimulatorConstants.PROPERTY_BASED_ATTRIBUTE_PROPERTY));
                            } else {
                                throw new ConfigurationParserException("Property value is required for "
                                        + RandomAttributeDto.RandomDataGeneratorType.PROPERTY_BASED + " simulation.");
                            }
                            attributeConfigurations.add(propertyBasedAttributeDto);
                            break;

                        case REGEX_BASED:
                            if (attributeConfigArray.getJSONObject(i)
                                    .has(EventSimulatorConstants.REGEX_BASED_ATTRIBUTE_PATTERN)
                                    && !attributeConfigArray.getJSONObject(i)
                                    .isNull(EventSimulatorConstants.REGEX_BASED_ATTRIBUTE_PATTERN)
                                    && !attributeConfigArray.getJSONObject(i)
                                    .getString(EventSimulatorConstants.REGEX_BASED_ATTRIBUTE_PATTERN).isEmpty()) {

                                /*
                                * validate regex pattern.
                                * if pattern is valid, create a RegexBasedAttributeDto object
                                * else, throw an exception
                                * */
                                RegexBasedGenerator.validateRegularExpression(attributeConfigArray.getJSONObject(i)
                                        .getString(EventSimulatorConstants.REGEX_BASED_ATTRIBUTE_PATTERN));
                                RegexBasedAttributeDto regexBasedAttributeDto = new RegexBasedAttributeDto();
                                regexBasedAttributeDto.setType(RandomAttributeDto.RandomDataGeneratorType.REGEX_BASED);
                                regexBasedAttributeDto.setPattern(attributeConfigArray.getJSONObject(i)
                                        .getString(EventSimulatorConstants.REGEX_BASED_ATTRIBUTE_PATTERN));
                                attributeConfigurations.add(regexBasedAttributeDto);
                            } else {
                                throw new ConfigurationParserException("Pattern is required for " +
                                        RandomAttributeDto.RandomDataGeneratorType.REGEX_BASED + " simulation.");
                            }
                            break;

                        case PRIMITIVE_BASED:
                            PrimitiveBasedAttributeDto primitiveBasedAttributeDto = new PrimitiveBasedAttributeDto();
                            primitiveBasedAttributeDto
                                    .setType(RandomAttributeDto.RandomDataGeneratorType.PRIMITIVE_BASED);
                            Attribute.Type attrType;

                            try {
                                /*
                                * retrieve the primitive type that need to be produced by primitive based random data
                                * generation.
                                * switch by the type so that only the required properties will be access and it will
                                * also enable to provide more meaningful exceptions indicating which properties
                                * are needed
                                *
                                * Given below are the properties needed for each primitive type generation
                                * BOOL - none
                                * STRING - length value
                                * INT, LONG - min and max value
                                * FLOAT, DOUBLE - min, max and length value.
                                *
                                * */
                                attrType = Attribute.Type.valueOf(attributeConfigArray.getJSONObject(i)
                                        .getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_TYPE));
                                primitiveBasedAttributeDto.setAttrType(attrType);
                            } catch (IllegalArgumentException e) {
                                throw new ConfigurationParserException("Invalid attribute type '" +
                                        attributeConfigArray.getJSONObject(i)
                                                .getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_TYPE)
                                        + "'.");
                            }

                            switch (attrType) {

                                case BOOL:
                                    break;

                                case STRING:
                                    if (attributeConfigArray.getJSONObject(i)
                                            .has(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_LENGTH_DECIMAL)
                                            && !attributeConfigArray.getJSONObject(i)
                                            .isNull(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_LENGTH_DECIMAL)
                                            && !attributeConfigArray.getJSONObject(i)
                                            .getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_LENGTH_DECIMAL)
                                            .isEmpty()) {

                                        primitiveBasedAttributeDto.setLength(attributeConfigArray.getJSONObject(i)
                                                .getInt(EventSimulatorConstants.
                                                        PRIMITIVE_BASED_ATTRIBUTE_LENGTH_DECIMAL));

                                    } else {
                                        throw new ConfigurationParserException("Length is required for " + "type '" +
                                                attrType + "' in " +
                                                RandomAttributeDto.RandomDataGeneratorType.PRIMITIVE_BASED +
                                                " simulation.");
                                    }
                                    break;

                                case INT:
                                case LONG:
                                    if (attributeConfigArray.getJSONObject(i)
                                            .has(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MIN)
                                            && !attributeConfigArray.getJSONObject(i)
                                            .isNull(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MIN)
                                            && !attributeConfigArray.getJSONObject(i)
                                            .getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MIN).isEmpty()
                                            && attributeConfigArray.getJSONObject(i)
                                            .has(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MAX)
                                            && !attributeConfigArray.getJSONObject(i)
                                            .isNull(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MAX)
                                            && !attributeConfigArray.getJSONObject(i)
                                            .getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MAX)
                                            .isEmpty()) {

                                        primitiveBasedAttributeDto.setMin(attributeConfigArray.getJSONObject(i)
                                                .getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MIN));
                                        primitiveBasedAttributeDto.setMax(attributeConfigArray.getJSONObject(i)
                                                .getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MAX));

                                    } else {
                                        throw new ConfigurationParserException("Min and Max value is required for" +
                                                " type '" + attrType + "' in" +
                                                RandomAttributeDto.RandomDataGeneratorType.PRIMITIVE_BASED +
                                                " simulation.");
                                    }
                                    break;

                                case FLOAT:
                                case DOUBLE:
                                    if (attributeConfigArray.getJSONObject(i)
                                            .has(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MIN)
                                            && !attributeConfigArray.getJSONObject(i)
                                            .isNull(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MIN)
                                            && !attributeConfigArray.getJSONObject(i)
                                            .getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MIN).isEmpty()
                                            && attributeConfigArray.getJSONObject(i)
                                            .has(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MAX)
                                            && !attributeConfigArray.getJSONObject(i)
                                            .isNull(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MAX)
                                            && !attributeConfigArray.getJSONObject(i)
                                            .getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MAX).isEmpty()
                                            && attributeConfigArray.getJSONObject(i).
                                            has(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_LENGTH_DECIMAL)
                                            && !attributeConfigArray.getJSONObject(i)
                                            .isNull(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_LENGTH_DECIMAL)
                                            && !attributeConfigArray.getJSONObject(i)
                                            .getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_LENGTH_DECIMAL)
                                            .isEmpty()) {

                                        primitiveBasedAttributeDto.setMin(attributeConfigArray.getJSONObject(i)
                                                .getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MIN));
                                        primitiveBasedAttributeDto.setMax(attributeConfigArray.getJSONObject(i)
                                                .getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MAX));
                                        primitiveBasedAttributeDto.setLength(attributeConfigArray.getJSONObject(i)
                                                .getInt(EventSimulatorConstants
                                                        .PRIMITIVE_BASED_ATTRIBUTE_LENGTH_DECIMAL));

                                    } else {
                                        throw new ConfigurationParserException("Min,Max and length value is required " +
                                                "for type '" + attrType + "' in " +
                                                RandomAttributeDto.RandomDataGeneratorType.PRIMITIVE_BASED +
                                                " simulation.");
                                    }
                                    break;
                            }
                            attributeConfigurations.add(primitiveBasedAttributeDto);
                            break;

                        case CUSTOM_DATA_BASED:
                            if (attributeConfigArray.getJSONObject(i)
                                    .has(EventSimulatorConstants.CUSTOM_DATA_BASED_ATTRIBUTE_LIST)
                                    && !attributeConfigArray.getJSONObject(i)
                                    .isNull(EventSimulatorConstants.CUSTOM_DATA_BASED_ATTRIBUTE_LIST)
                                    && !attributeConfigArray.getJSONObject(i)
                                    .getString(EventSimulatorConstants.CUSTOM_DATA_BASED_ATTRIBUTE_LIST).isEmpty()) {

                                CustomBasedAttributeDto customBasedAttributeDto = new CustomBasedAttributeDto();
                                customBasedAttributeDto.setType(RandomAttributeDto
                                        .RandomDataGeneratorType.CUSTOM_DATA_BASED);
                                customBasedAttributeDto.setCustomData(attributeConfigArray.getJSONObject(i)
                                        .getString(EventSimulatorConstants.CUSTOM_DATA_BASED_ATTRIBUTE_LIST));
                                attributeConfigurations.add(customBasedAttributeDto);
                            } else {
                                throw new ConfigurationParserException("Data list is not given for " +
                                        RandomAttributeDto.RandomDataGeneratorType.CUSTOM_DATA_BASED + " simulation.");
                            }
                            break;
                    }
                }
            }
            randomSimulationDto.setAttributeConfigurations(attributeConfigurations);
        } catch (ConfigurationParserException e) {
            log.error("Error occurred when parsing random simulation configuration : " + e.getMessage(), e);
        } catch (JSONException e) {
            log.error("Error occurred when accessing random simulation configuration : " + e.getMessage(), e);
        }
        return randomSimulationDto;
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

        /*
        * set properties to CSVFileSimulationDto.
        *
        * Perform the following checks prior to setting the properties.
        * 1. has
        * 2. isNull
        * 3. isEmpty
        *
        * if any of the above checks fail, throw an exception indicating which property is missing.
        * */

        try {
            if (csvFileDetail.has(EventSimulatorConstants.STREAM_NAME)
                    && !csvFileDetail.isNull(EventSimulatorConstants.STREAM_NAME)
                    && !csvFileDetail.getString(EventSimulatorConstants.STREAM_NAME).isEmpty()) {

                csvFileSimulationDto.setStreamName(csvFileDetail.getString(EventSimulatorConstants.STREAM_NAME));
            } else {
                throw new ConfigurationParserException("Stream name is required");
            }

            if (csvFileDetail.has(EventSimulatorConstants.EXECUTION_PLAN_NAME)
                    && !csvFileDetail.isNull(EventSimulatorConstants.EXECUTION_PLAN_NAME)
                    && !csvFileDetail.getString(EventSimulatorConstants.EXECUTION_PLAN_NAME).isEmpty()) {

                csvFileSimulationDto.setExecutionPlanName(csvFileDetail
                        .getString(EventSimulatorConstants.EXECUTION_PLAN_NAME));
            } else {
                throw new ConfigurationParserException("Execution plan name is required");
            }

            if (csvFileDetail.has(EventSimulatorConstants.FILE_NAME)
                    && !csvFileDetail.isNull(EventSimulatorConstants.FILE_NAME)
                    && !csvFileDetail.getString(EventSimulatorConstants.FILE_NAME).isEmpty()) {

                csvFileSimulationDto.setFileName(csvFileDetail.getString(EventSimulatorConstants.FILE_NAME));
            } else {
                throw new ConfigurationParserException("File name is required");
            }

            if (csvFileDetail.has(EventSimulatorConstants.TIMESTAMP_POSITION)
                    && !csvFileDetail.isNull(EventSimulatorConstants.TIMESTAMP_POSITION)
                    && !csvFileDetail.getString(EventSimulatorConstants.TIMESTAMP_POSITION).isEmpty()) {

                csvFileSimulationDto.setTimestampAttribute(csvFileDetail
                        .getString(EventSimulatorConstants.TIMESTAMP_POSITION));
            } else {
                throw new ConfigurationParserException("Timestamp position is required");
            }

            if (csvFileDetail.has(EventSimulatorConstants.DELIMITER)
                    && !csvFileDetail.isNull(EventSimulatorConstants.DELIMITER)
                    && !csvFileDetail.getString(EventSimulatorConstants.DELIMITER).isEmpty()) {

                csvFileSimulationDto.setDelimiter((String) csvFileDetail.get(EventSimulatorConstants.DELIMITER));
            } else {
                throw new ConfigurationParserException("Delimiter is required");
            }

            if (csvFileDetail.has(EventSimulatorConstants.IS_ORDERED)
                    && !csvFileDetail.isNull(EventSimulatorConstants.IS_ORDERED)) {

                csvFileSimulationDto.setIsOrdered(csvFileDetail.getBoolean(EventSimulatorConstants.IS_ORDERED));
            } else {
                throw new ConfigurationParserException("isOrdered flag is required");
            }

            /*
            * check whether the CSV file has been uploaded.
            * if yes, assign the fileDto property
            * else, throw an exception to indicate that the file has not been uploaded
            * */
            FileDto fileDto;
            if (FileStore.getFileStore().checkExists(csvFileSimulationDto.getFileName())) {
                fileDto = FileStore.getFileStore().getFileInfoMap().get(csvFileSimulationDto.getFileName());
                csvFileSimulationDto.setFileDto(fileDto);
            } else {
                throw new ConfigurationParserException("File '" + csvFileSimulationDto.getFileName() +
                        "' has not been uploaded.");
            }
        } catch (ConfigurationParserException e) {
            log.error("Error occurred when parsing CSV simulation configuration : " + e.getMessage(), e);
        } catch (JSONException e) {
            log.error("Error occurred when accessing CSV simulation configuration : " + e.getMessage(), e);
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

        try {

            /*
             * set properties to DatabaseFeedSimulationDto.
             *
             * Perform the following checks prior to setting the properties.
             * 1. has
             * 2. isNull
             * 3. isEmpty
             *
             * if any of the above checks fail, throw an exception indicating which property is missing.
             * */

            if (databaseConfigurations.has(EventSimulatorConstants.DATABASE_NAME)
                    && !databaseConfigurations.isNull(EventSimulatorConstants.DATABASE_NAME)
                    && !databaseConfigurations.getString(EventSimulatorConstants.DATABASE_NAME).isEmpty()) {

                databaseFeedSimulationDto.setDatabaseName(databaseConfigurations
                        .getString(EventSimulatorConstants.DATABASE_NAME));
            } else {
                throw new ConfigurationParserException("Database name is required");
            }

            if (databaseConfigurations.has(EventSimulatorConstants.USER_NAME)
                    && !databaseConfigurations.isNull(EventSimulatorConstants.USER_NAME)
                    && !databaseConfigurations.getString(EventSimulatorConstants.USER_NAME).isEmpty()) {

                databaseFeedSimulationDto.setUsername(databaseConfigurations
                        .getString(EventSimulatorConstants.USER_NAME));
            } else {
                throw new ConfigurationParserException("Username is required");
            }

            if (databaseConfigurations.has(EventSimulatorConstants.PASSWORD)
                    && !databaseConfigurations.isNull(EventSimulatorConstants.PASSWORD)
                    && !databaseConfigurations.getString(EventSimulatorConstants.PASSWORD).isEmpty()) {

                databaseFeedSimulationDto.setPassword(databaseConfigurations
                        .getString(EventSimulatorConstants.PASSWORD));
            } else {
                throw new ConfigurationParserException("Password is required");
            }

            if (databaseConfigurations.has(EventSimulatorConstants.TABLE_NAME)
                    && !databaseConfigurations.isNull(EventSimulatorConstants.TABLE_NAME)
                    && !databaseConfigurations.getString(EventSimulatorConstants.TABLE_NAME).isEmpty()) {

                databaseFeedSimulationDto.setTableName(databaseConfigurations
                        .getString(EventSimulatorConstants.TABLE_NAME));
            } else {
                throw new ConfigurationParserException("Table name is required");
            }

            if (databaseConfigurations.has(EventSimulatorConstants.STREAM_NAME)
                    && !databaseConfigurations.isNull(EventSimulatorConstants.STREAM_NAME)
                    && !databaseConfigurations.getString(EventSimulatorConstants.STREAM_NAME).isEmpty()) {

                databaseFeedSimulationDto.setStreamName(databaseConfigurations
                        .getString(EventSimulatorConstants.STREAM_NAME));
            } else {
                throw new ConfigurationParserException("Stream name is required");
            }

            if (databaseConfigurations.has(EventSimulatorConstants.EXECUTION_PLAN_NAME)
                    && !databaseConfigurations.isNull(EventSimulatorConstants.EXECUTION_PLAN_NAME)
                    && !databaseConfigurations.getString(EventSimulatorConstants.EXECUTION_PLAN_NAME).isEmpty()) {

                databaseFeedSimulationDto.setExecutionPlanName(databaseConfigurations
                        .getString(EventSimulatorConstants.EXECUTION_PLAN_NAME));
            } else {
                throw new ConfigurationParserException("Execution plan name is required");
            }

            if (databaseConfigurations.has(EventSimulatorConstants.TIMESTAMP_ATTRIBUTE)
                    && !databaseConfigurations.isNull(EventSimulatorConstants.TIMESTAMP_ATTRIBUTE)
                    && !databaseConfigurations.getString(EventSimulatorConstants.TIMESTAMP_ATTRIBUTE).isEmpty()) {

                databaseFeedSimulationDto.setTimestampAttribute(databaseConfigurations
                        .getString(EventSimulatorConstants.TIMESTAMP_ATTRIBUTE));
            } else {
                throw new ConfigurationParserException("Timestamp attribute is required");
            }

//      insert the specified column names into a list and set it to database configuration;
            if (databaseConfigurations.has(EventSimulatorConstants.COLUMN_NAMES_LIST)
                    && !databaseConfigurations.isNull(EventSimulatorConstants.COLUMN_NAMES_LIST)
                    && !databaseConfigurations.getString(EventSimulatorConstants.COLUMN_NAMES_LIST).isEmpty()) {

                databaseFeedSimulationDto.setColumnNames(databaseConfigurations
                        .getString(EventSimulatorConstants.COLUMN_NAMES_LIST));
            } else {
                throw new ConfigurationParserException("Column names list is required");
            }

        } catch (ConfigurationParserException e) {
            log.error("Error occurred when parsing database simulation configuration : " + e.getMessage(), e);
        } catch (JSONException e) {
            log.error("Error occurred when accessing database simulation configuration : " + e.getMessage(), e);
        }

        return databaseFeedSimulationDto;
    }

}
