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

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.eventsimulator.core.bean.CSVSimulationDto;
import org.wso2.eventsimulator.core.bean.DBSimulationDto;
import org.wso2.eventsimulator.core.bean.RandomSimulationDto;
import org.wso2.eventsimulator.core.bean.SimulationConfigurationDto;
import org.wso2.eventsimulator.core.bean.SingleEventSimulationDto;
import org.wso2.eventsimulator.core.exception.InvalidConfigException;
import org.wso2.eventsimulator.core.exception.ValidationFailedException;
import org.wso2.eventsimulator.core.generator.EventGenerator;
import org.wso2.eventsimulator.core.generator.csv.util.FileStore;
import org.wso2.eventsimulator.core.generator.random.bean.CustomBasedAttributeDto;
import org.wso2.eventsimulator.core.generator.random.bean.PrimitiveBasedAttributeDto;
import org.wso2.eventsimulator.core.generator.random.bean.PropertyBasedAttributeDto;
import org.wso2.eventsimulator.core.generator.random.bean.RandomAttributeDto;
import org.wso2.eventsimulator.core.generator.random.bean.RegexBasedAttributeDto;
import org.wso2.eventsimulator.core.generator.random.util.RegexBasedGenerator;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * ConfigParserAndValidator is an util class used to
 * convert Json string into relevant event simulation configuration object
 */
public class ConfigParserAndValidator {
    private static final Logger log = Logger.getLogger(ConfigParserAndValidator.class);

    /*
    Initialize ConfigParserAndValidator
     */
    private ConfigParserAndValidator() {
    }


    /**
     * Convert the singleEventConfiguration string into SingleEventSimulationDto Object
     *
     * @param singleEventConfiguration String containing single event simulation configuration
     * @return SingleEventSimulationDto Object
     * @throws InvalidConfigException if the simulation configuration contains invalid data
     */
    public static SingleEventSimulationDto singleEventSimulatorParser(String singleEventConfiguration)
            throws InvalidConfigException {
        SingleEventSimulationDto singleEventSimulationDto = new SingleEventSimulationDto();
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
        if (checkAvailability(singleEventConfig, EventSimulatorConstants.STREAM_NAME)) {

            singleEventSimulationDto.setStreamName(singleEventConfig
                    .getString(EventSimulatorConstants.STREAM_NAME));

        } else {
            throw new InvalidConfigException("Stream name is required for single event simulation");
        }

        if (checkAvailability(singleEventConfig, EventSimulatorConstants.EXECUTION_PLAN_NAME)) {

            singleEventSimulationDto.setExecutionPlanName(singleEventConfig
                    .getString(EventSimulatorConstants.EXECUTION_PLAN_NAME));

        } else {
            throw new InvalidConfigException("Execution plan name is required for single event simulation");
        }
        if (checkAvailability(singleEventConfig, EventSimulatorConstants.SINGLE_EVENT_TIMESTAMP)) {

            singleEventSimulationDto.setTimestamp(singleEventConfig.
                    getLong(EventSimulatorConstants.SINGLE_EVENT_TIMESTAMP));
        } else {
            throw new InvalidConfigException("Single event simulation requires a timestamp value for single" +
                    " event simulation");

        }

        if (checkAvailability(singleEventConfig, EventSimulatorConstants.SINGLE_EVENT_DATA)) {

            String[] attributeValues = getAttributeValues(singleEventConfig
                    .getString(EventSimulatorConstants.SINGLE_EVENT_DATA));
            singleEventSimulationDto.setAttributeValues(attributeValues);

            if (log.isDebugEnabled()) {
                log.debug("Set attribute values for single event simulation");
            }

        } else {
            throw new InvalidConfigException("Single event simulation requires a attribute value for " +
                    "stream '" + singleEventSimulationDto.getStreamName() + "'.");
        }

        return singleEventSimulationDto;
    }


    /**
     * Convert the randomSimulationConfig JSONObject into RandomSimulationDto Object
     * <p>
     * randomSimulationConfig can have one or more attribute configurations of the following types
     * 1.PRIMITIVEBASED : String/Integer/Long/Float/Double/Boolean
     * 2.PROPERTYBASED  : generates meaning full data.
     * 3.REGEXBASED     : generates data using given regex
     * 4.CUSTOMDATA     : generates data using a given data list
     * <p>
     *
     * @param randomSimulationConfig JSON object containing configuration for random simulation
     * @return RandomSimulationDto Object
     * @throws InvalidConfigException    if the simulation configuration contains invalid data
     * @throws ValidationFailedException if the regex pattern provided is invalid
     */
    private static RandomSimulationDto randomDataSimulatorParser(JSONObject randomSimulationConfig)
            throws InvalidConfigException, ValidationFailedException {
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

        if (checkAvailability(randomSimulationConfig, EventSimulatorConstants.STREAM_NAME)) {

            randomSimulationDto.setStreamName(randomSimulationConfig
                    .getString(EventSimulatorConstants.STREAM_NAME));

        } else {
            throw new InvalidConfigException("Stream name is required for random data simulation");
        }

        if (checkAvailability(randomSimulationConfig, EventSimulatorConstants.EXECUTION_PLAN_NAME)) {

            randomSimulationDto.setExecutionPlanName(randomSimulationConfig
                    .getString(EventSimulatorConstants.EXECUTION_PLAN_NAME));

        } else {
            throw new InvalidConfigException("Execution plan name is required for random data simulation");
        }
        if (checkAvailability(randomSimulationConfig, EventSimulatorConstants.TIME_INTERVAL)) {

            randomSimulationDto.setTimeInterval(randomSimulationConfig
                    .getLong(EventSimulatorConstants.TIME_INTERVAL));

        } else {
            log.warn("Time interval is required for random data simulation. Time interval is set to 0 milliseconds");
            randomSimulationDto.setTimeInterval(0L);
        }

        List<RandomAttributeDto> attributeConfigurations = new ArrayList<>();

        JSONArray attributeConfigArray;
        if (checkAvailabilityOfArray(randomSimulationConfig, EventSimulatorConstants.ATTRIBUTE_CONFIGURATION)) {

            attributeConfigArray = randomSimulationConfig
                    .getJSONArray(EventSimulatorConstants.ATTRIBUTE_CONFIGURATION);

        } else {
            throw new InvalidConfigException("Attribute configuration is required for random data simulation");
        }

        //convert each attribute simulation configuration as relevant objects

        RandomAttributeDto.RandomDataGeneratorType type;

        for (int i = 0; i < attributeConfigArray.length(); i++) {

            if (checkAvailability(attributeConfigArray.getJSONObject(i),
                    EventSimulatorConstants.RANDOM_DATA_GENERATOR_TYPE)) {

                  /*
                  * for each attribute configuration, switch using the random generation type and create
                  * respective attribute configuration objects.
                  * */
                try {
                    type = RandomAttributeDto.RandomDataGeneratorType.valueOf(attributeConfigArray.getJSONObject(i)
                            .getString(EventSimulatorConstants.RANDOM_DATA_GENERATOR_TYPE));
                } catch (IllegalArgumentException e) {
                    throw new InvalidConfigException("Invalid random generation type. Generation type must " +
                            "be either '" + RandomAttributeDto.RandomDataGeneratorType.CUSTOM_DATA_BASED + "' or '"
                            + RandomAttributeDto.RandomDataGeneratorType.PRIMITIVE_BASED + "' or '" +
                            RandomAttributeDto.RandomDataGeneratorType.PROPERTY_BASED + "' or '" +
                            RandomAttributeDto.RandomDataGeneratorType.REGEX_BASED + "'.");
                }

                switch (type) {

                    case PROPERTY_BASED:
                        PropertyBasedAttributeDto propertyBasedAttributeDto = new PropertyBasedAttributeDto();

                        if (checkAvailability(attributeConfigArray.getJSONObject(i),
                                EventSimulatorConstants.PROPERTY_BASED_ATTRIBUTE_CATEGORY)) {

                            propertyBasedAttributeDto
                                    .setType(RandomAttributeDto.RandomDataGeneratorType.PROPERTY_BASED);
                            propertyBasedAttributeDto.setCategory(attributeConfigArray.getJSONObject(i)
                                    .getString(EventSimulatorConstants.PROPERTY_BASED_ATTRIBUTE_CATEGORY));
                        } else {
                            throw new InvalidConfigException("Category value is required for "
                                    + RandomAttributeDto.RandomDataGeneratorType.PROPERTY_BASED + " simulation.");
                        }

                        if (checkAvailability(attributeConfigArray.getJSONObject(i),
                                EventSimulatorConstants.PROPERTY_BASED_ATTRIBUTE_PROPERTY)) {

                            propertyBasedAttributeDto.setProperty(attributeConfigArray.getJSONObject(i)
                                    .getString(EventSimulatorConstants.PROPERTY_BASED_ATTRIBUTE_PROPERTY));
                        } else {
                            throw new InvalidConfigException("Property value is required for "
                                    + RandomAttributeDto.RandomDataGeneratorType.PROPERTY_BASED + " simulation.");
                        }
                        attributeConfigurations.add(propertyBasedAttributeDto);
                        break;

                    case REGEX_BASED:
                        if (checkAvailability(attributeConfigArray.getJSONObject(i),
                                EventSimulatorConstants.REGEX_BASED_ATTRIBUTE_PATTERN)) {

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
                            throw new InvalidConfigException("Pattern is required for " +
                                    RandomAttributeDto.RandomDataGeneratorType.REGEX_BASED + " simulation.");
                        }
                        break;

                    case PRIMITIVE_BASED:
                        PrimitiveBasedAttributeDto primitiveBasedAttributeDto = new PrimitiveBasedAttributeDto();
                        primitiveBasedAttributeDto
                                .setType(RandomAttributeDto.RandomDataGeneratorType.PRIMITIVE_BASED);
                        Attribute.Type attrType;

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
                        * since Min and mx values are used by 4 primitive types, its saved as a string so that it could
                        * later be parsed to the specific primitive type
                        **/
                        try {
                            attrType = Attribute.Type.valueOf(attributeConfigArray.getJSONObject(i)
                                    .getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_TYPE));
                            primitiveBasedAttributeDto.setAttrType(attrType);
                        } catch (IllegalArgumentException e) {
                            throw new InvalidConfigException("Invalid attribute type '" +
                                    attributeConfigArray.getJSONObject(i)
                                            .getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_TYPE)
                                    + "'.");
                        }

                        switch (attrType) {

                            case BOOL:
                                break;

                            case STRING:
                                if (checkAvailability(attributeConfigArray.getJSONObject(i),
                                        EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_LENGTH_DECIMAL)) {

                                    primitiveBasedAttributeDto.setLength(attributeConfigArray.getJSONObject(i)
                                            .getInt(EventSimulatorConstants.
                                                    PRIMITIVE_BASED_ATTRIBUTE_LENGTH_DECIMAL));

                                } else {
                                    throw new InvalidConfigException("Property 'Length' is required for type"
                                            + " '" + attrType + "' in " +
                                            RandomAttributeDto.RandomDataGeneratorType.PRIMITIVE_BASED +
                                            " simulation.");
                                }
                                break;

                            case INT:
                            case LONG:
                                if (checkAvailability(attributeConfigArray.getJSONObject(i),
                                        EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MIN)
                                        && checkAvailability(attributeConfigArray.getJSONObject(i),
                                        EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MAX)) {

                                    primitiveBasedAttributeDto.setMin(attributeConfigArray.getJSONObject(i)
                                            .getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MIN));
                                    primitiveBasedAttributeDto.setMax(attributeConfigArray.getJSONObject(i)
                                            .getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MAX));

                                } else {
                                    throw new InvalidConfigException("Properties 'Min' and 'Max' are required " +
                                            "for type '" + attrType + "' in" +
                                            RandomAttributeDto.RandomDataGeneratorType.PRIMITIVE_BASED +
                                            " simulation.");
                                }
                                break;

                            case FLOAT:
                            case DOUBLE:
                                if (checkAvailability(attributeConfigArray.getJSONObject(i),
                                        EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MIN)
                                        && checkAvailability(attributeConfigArray.getJSONObject(i),
                                        EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MAX)
                                        && checkAvailability(attributeConfigArray.getJSONObject(i),
                                        EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_LENGTH_DECIMAL)) {

                                    primitiveBasedAttributeDto.setMin(attributeConfigArray.getJSONObject(i)
                                            .getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MIN));
                                    primitiveBasedAttributeDto.setMax(attributeConfigArray.getJSONObject(i)
                                            .getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MAX));
                                    primitiveBasedAttributeDto.setLength(attributeConfigArray.getJSONObject(i)
                                            .getInt(EventSimulatorConstants
                                                    .PRIMITIVE_BASED_ATTRIBUTE_LENGTH_DECIMAL));

                                } else {
                                    throw new InvalidConfigException("Properties 'Min','Max' and 'Length' are " +
                                            "required for type '" + attrType + "' in " +
                                            RandomAttributeDto.RandomDataGeneratorType.PRIMITIVE_BASED +
                                            " simulation.");
                                }
                                break;

                            default:
                                throw new InvalidConfigException("Invalid attribute type '" + attrType +
                                        "'. Attribute type must be either STRING, INT, DOUBLE, FLOAT, BOOL or LONG");
                        }
                        attributeConfigurations.add(primitiveBasedAttributeDto);
                        break;

                    case CUSTOM_DATA_BASED:
                        if (checkAvailability(attributeConfigArray.getJSONObject(i),
                                EventSimulatorConstants.CUSTOM_DATA_BASED_ATTRIBUTE_LIST)) {

                            CustomBasedAttributeDto customBasedAttributeDto = new CustomBasedAttributeDto();
                            customBasedAttributeDto.setType(RandomAttributeDto
                                    .RandomDataGeneratorType.CUSTOM_DATA_BASED);

                            String[] dataList = getAttributeValues(attributeConfigArray.getJSONObject(i)
                                    .getString(EventSimulatorConstants.CUSTOM_DATA_BASED_ATTRIBUTE_LIST));

                            customBasedAttributeDto.setCustomData(dataList);

                            if (log.isDebugEnabled()) {
                                log.debug("Set data list for custom based random simulation.");
                            }

                            attributeConfigurations.add(customBasedAttributeDto);
                        } else {
                            throw new InvalidConfigException("Data list is not given for " +
                                    RandomAttributeDto.RandomDataGeneratorType.CUSTOM_DATA_BASED + " simulation.");
                        }
                        break;
                }
            }
        }
        randomSimulationDto.setAttributeConfigurations(attributeConfigurations);

        return randomSimulationDto;
    }


    /**
     * Convert the csvSimulationConfig string into CSVSimulationDto Object
     * <p>
     * Initialize CSVSimulationDto
     * Initialize FileStore
     *
     * @param csvSimulationConfig csvSimulationConfig String
     * @return CSVSimulationDto Object
     * @throws InvalidConfigException if the simulation configuration contains invalid data
     */
    private static CSVSimulationDto csvSimulationParser(JSONObject csvSimulationConfig) throws InvalidConfigException {
        CSVSimulationDto csvFileSimulationDto = new CSVSimulationDto();

        /*
        * set properties to CSVSimulationDto.
        *
        * Perform the following checks prior to setting the properties.
        * 1. has
        * 2. isNull
        * 3. isEmpty
        *
        * if any of the above checks fail, throw an exception indicating which property is missing.
        * */
        if (checkAvailability(csvSimulationConfig, EventSimulatorConstants.STREAM_NAME)) {

            csvFileSimulationDto.setStreamName(csvSimulationConfig.getString(EventSimulatorConstants.STREAM_NAME));
        } else {
            throw new InvalidConfigException("Stream name is required for CSV simulation");
        }

        if (checkAvailability(csvSimulationConfig, EventSimulatorConstants.EXECUTION_PLAN_NAME)) {

            csvFileSimulationDto.setExecutionPlanName(csvSimulationConfig
                    .getString(EventSimulatorConstants.EXECUTION_PLAN_NAME));
        } else {
            throw new InvalidConfigException("Execution plan name is required for CSV simulation");
        }

        if (checkAvailability(csvSimulationConfig, EventSimulatorConstants.FILE_NAME)) {

            csvFileSimulationDto.setFileName(csvSimulationConfig.getString(EventSimulatorConstants.FILE_NAME));
        } else {
            throw new InvalidConfigException("File name is required for CSV simulation");
        }

        if (checkAvailability(csvSimulationConfig, EventSimulatorConstants.TIMESTAMP_POSITION)) {

            csvFileSimulationDto.setTimestampAttribute(csvSimulationConfig
                    .getString(EventSimulatorConstants.TIMESTAMP_POSITION));
        } else {
            throw new InvalidConfigException("Timestamp position is required for CSV simulation");
        }

        if (checkAvailability(csvSimulationConfig, EventSimulatorConstants.DELIMITER)) {

            csvFileSimulationDto.setDelimiter((String) csvSimulationConfig.get(EventSimulatorConstants.DELIMITER));
        } else {
            throw new InvalidConfigException("Delimiter is required for CSV simulation");
        }

        if (checkAvailability(csvSimulationConfig, EventSimulatorConstants.IS_ORDERED)) {

            csvFileSimulationDto.setIsOrdered(csvSimulationConfig.getBoolean(EventSimulatorConstants.IS_ORDERED));
        } else {
            throw new InvalidConfigException("isOrdered flag is required for CSV simulation");
        }

            /*
            * check whether the CSV file has been uploaded.
            * if yes, assign the fileInfo to the csvConfig property
            * else, throw an exception to indicate that the file has not been uploaded
            * */
        if (FileStore.getFileStore().checkExists(csvFileSimulationDto.getFileName())) {
            FileInfo fileInfo = FileStore.getFileStore().getFileInfoMap().get(csvFileSimulationDto.getFileName());
            csvFileSimulationDto.setFileInfo(fileInfo);
        } else {
            throw new InvalidConfigException("File '" + csvFileSimulationDto.getFileName() +
                    "' has not been uploaded.");
        }

        return csvFileSimulationDto;
    }

    /**
     * Convert the database configuration file into a DBSimulationDto object
     *
     * @param dbSimulationConfig database configuration string
     * @return a DBSimulationDto object
     * @throws InvalidConfigException if the simulation configuration contains invalid data
     */

    private static DBSimulationDto dbSimulationParser(JSONObject dbSimulationConfig)
            throws InvalidConfigException {

        DBSimulationDto dbSimulationDto = new DBSimulationDto();
/*
             * set properties to DBSimulationDto.
             *
             * Perform the following checks prior to setting the properties.
             * 1. has
             * 2. isNull
             * 3. isEmpty
             *
             * if any of the above checks fail, throw an exception indicating which property is missing.
             * */

        if (checkAvailability(dbSimulationConfig, EventSimulatorConstants.DATABASE_NAME)) {

            dbSimulationDto.setDatabaseName(dbSimulationConfig.getString(EventSimulatorConstants.DATABASE_NAME));
        } else {
            throw new InvalidConfigException("Database name is required for database simulation");
        }

        if (checkAvailability(dbSimulationConfig, EventSimulatorConstants.USER_NAME)) {

            dbSimulationDto.setUsername(dbSimulationConfig.getString(EventSimulatorConstants.USER_NAME));
        } else {
            throw new InvalidConfigException("Username is required for database simulation");
        }

        if (checkAvailability(dbSimulationConfig, EventSimulatorConstants.PASSWORD)) {

            dbSimulationDto.setPassword(dbSimulationConfig.getString(EventSimulatorConstants.PASSWORD));
        } else {
            throw new InvalidConfigException("Password is required for database simulation");
        }

        if (checkAvailability(dbSimulationConfig, EventSimulatorConstants.TABLE_NAME)) {

            dbSimulationDto.setTableName(dbSimulationConfig.getString(EventSimulatorConstants.TABLE_NAME));
        } else {
            throw new InvalidConfigException("Table name is required for database simulation");
        }

        if (checkAvailability(dbSimulationConfig, EventSimulatorConstants.STREAM_NAME)) {

            dbSimulationDto.setStreamName(dbSimulationConfig.getString(EventSimulatorConstants.STREAM_NAME));
        } else {
            throw new InvalidConfigException("Stream name is required for database simulation");
        }

        if (checkAvailability(dbSimulationConfig, EventSimulatorConstants.EXECUTION_PLAN_NAME)) {

            dbSimulationDto.setExecutionPlanName(dbSimulationConfig
                    .getString(EventSimulatorConstants.EXECUTION_PLAN_NAME));
        } else {
            throw new InvalidConfigException("Execution plan name is required for database simulation");
        }

        if (checkAvailability(dbSimulationConfig, EventSimulatorConstants.TIMESTAMP_ATTRIBUTE)) {

            dbSimulationDto.setTimestampAttribute(dbSimulationConfig
                    .getString(EventSimulatorConstants.TIMESTAMP_ATTRIBUTE));
        } else {
            throw new InvalidConfigException("Timestamp attribute is required for database simulation");
        }

//      insert the specified column names into a list and set it to database configuration;
        if (checkAvailability(dbSimulationConfig, EventSimulatorConstants.COLUMN_NAMES_LIST)) {

            List<String> columns = getColumnsList(dbSimulationConfig
                    .getString(EventSimulatorConstants.COLUMN_NAMES_LIST));

            dbSimulationDto.setColumnNames(columns);
        } else {
            throw new InvalidConfigException("Column names list is required for database simulation");
        }

        return dbSimulationDto;
    }


    /**
     * parseAndValidateConfig() is used to parse the simulation configuration and to create stream configuration
     * objects based on the simulation type.
     *
     * @param simulationConfigDetails a string containing simulation configuration
     * @return SimulationConfigurationDto object containing simulation configuration details
     * @throws InvalidConfigException    if the simulation configuration contains invalid data
     * @throws ValidationFailedException if an invalid regex pattern is provided
     */
    public static SimulationConfigurationDto parseAndValidateConfig(String simulationConfigDetails)
            throws InvalidConfigException, ValidationFailedException {
        SimulationConfigurationDto simulationConfigurationDto = new SimulationConfigurationDto();
        JSONObject simulationConfiguration = new JSONObject(simulationConfigDetails);

        if (checkAvailability(simulationConfiguration, EventSimulatorConstants.DELAY)) {
            simulationConfigurationDto.setDelay(simulationConfiguration.getLong(EventSimulatorConstants.DELAY));
        } else {
            throw new InvalidConfigException("Delay is not specified.");
        }

        if (checkAvailability(simulationConfiguration, EventSimulatorConstants.TIMESTAMP_START_TIME)) {
            simulationConfigurationDto.setTimestampStartTime(
                    simulationConfiguration.getLong(EventSimulatorConstants.TIMESTAMP_START_TIME));
        } else {
            throw new InvalidConfigException("TimestampStartTime is required");
        }

        if (simulationConfiguration.has(EventSimulatorConstants.TIMESTAMP_END_TIME)) {
            if (simulationConfiguration.isNull(EventSimulatorConstants.TIMESTAMP_END_TIME)) {
                simulationConfigurationDto.setTimestampEndTime(null);
            } else if (!simulationConfiguration.getString(EventSimulatorConstants.TIMESTAMP_END_TIME).isEmpty()) {
                simulationConfigurationDto.setTimestampEndTime(
                        simulationConfiguration.getLong(EventSimulatorConstants.TIMESTAMP_END_TIME));
            } else {
                throw new InvalidConfigException("TimestampEndTime is not specified.");
            }
        } else {
            throw new InvalidConfigException("TimestampEndTime is not specified.");
        }

        JSONArray streamConfigurations;
        if (checkAvailabilityOfArray(simulationConfiguration,
                EventSimulatorConstants.EVENT_SIMULATION_STREAM_CONFIGURATION)) {

            streamConfigurations = simulationConfiguration
                    .getJSONArray(EventSimulatorConstants.EVENT_SIMULATION_STREAM_CONFIGURATION);
        } else {
            throw new InvalidConfigException("Stream configuration is required");
        }

        EventGenerator.GeneratorType simulationType;

        for (int i = 0; i < streamConfigurations.length(); i++) {
            if (checkAvailability(streamConfigurations.getJSONObject(i),
                    EventSimulatorConstants.EVENT_SIMULATION_TYPE)) {

                    /*
                    * for each stream configuration retrieve the simulation type.
                    * Switch by the simulation type to determine which type of parser is needed to parse the
                    * simulation configuration
                    * */

                try {
                    simulationType = EventGenerator.GeneratorType.valueOf(streamConfigurations.getJSONObject(i)
                            .getString(EventSimulatorConstants.EVENT_SIMULATION_TYPE));
                } catch (IllegalArgumentException e) {
                    throw new InvalidConfigException("Invalid simulation type. Simulation type must be " +
                            "either '" + EventGenerator.GeneratorType.FILE_SIMULATION + "' or '" +
                            EventGenerator.GeneratorType.DATABASE_SIMULATION + "' or '" +
                            EventGenerator.GeneratorType.RANDOM_DATA_SIMULATION + "'.");
                }

                switch (simulationType) {
                    case DATABASE_SIMULATION:
                        DBSimulationDto dbSimulationDto =
                                dbSimulationParser(streamConfigurations.getJSONObject(i));
                        dbSimulationDto.setGeneratorType(EventGenerator.GeneratorType.DATABASE_SIMULATION);
                        simulationConfigurationDto.addStreamConfiguration(dbSimulationDto);
                        break;

                    case FILE_SIMULATION:
                        CSVSimulationDto csvSimulationDto = csvSimulationParser(streamConfigurations
                                .getJSONObject(i));
                        csvSimulationDto.setGeneratorType(EventGenerator.GeneratorType.FILE_SIMULATION);
                        simulationConfigurationDto.addStreamConfiguration(csvSimulationDto);
                        break;

                    case RANDOM_DATA_SIMULATION:
                        RandomSimulationDto randomSimulationDto = randomDataSimulatorParser(streamConfigurations
                                .getJSONObject(i));
                        randomSimulationDto.setGeneratorType(EventGenerator.GeneratorType.RANDOM_DATA_SIMULATION);
                        simulationConfigurationDto.addStreamConfiguration(randomSimulationDto);
                        break;
                }
            } else {
                throw new InvalidConfigException("Simulation type is not specified. Simulation type must" +
                        " be either '" + EventGenerator.GeneratorType.FILE_SIMULATION + "' or '" +
                        EventGenerator.GeneratorType.DATABASE_SIMULATION + "' or '" +
                        EventGenerator.GeneratorType.RANDOM_DATA_SIMULATION + "'.");
            }
        }

        return simulationConfigurationDto;
    }


    /**
     * checkAvailability() performs the following checks on the the json object and key provided
     * 1. has
     * 2. isNull
     * 3. isEmpty
     *
     * @param configuration JSON object containing configuration
     * @param key           name of key
     * @return true if checks are successful, else false
     */
    private static Boolean checkAvailability(JSONObject configuration, String key) {

        return configuration.has(key)
                && !configuration.isNull(key)
                && !configuration.getString(key).isEmpty();
    }

    /**
     * checkAvailability() performs the following checks on the the json object and key provided.
     * This method is used for key's that contains json array values.
     * 1. has
     * 2. isNull
     * 3. isEmpty
     *
     * @param configuration JSON object containing configuration
     * @param key           name of key
     * @return true if checks are successful, else false
     */
    private static Boolean checkAvailabilityOfArray(JSONObject configuration, String key) {

        return configuration.has(key)
                && !configuration.isNull(key)
                && configuration.getJSONArray(key).length() > 0;
    }


    /**
     * getAttributeValues() created a string array of the attribute values specified and validates that the array
     * does not contain any empty strings
     *
     * @param attributes string of comma seperated attribute values
     * @return string array of attribute values if the array has no empty strings, else throw an exception
     * @throws InvalidConfigException if the simulation configuration contains invalid data
     */
    private static String[] getAttributeValues(String attributes) throws InvalidConfigException {
        /*
        * convert the string of attribute values to a string array by splitting at "."
        * check whether all attribute values specified are not empty
        * if yes, return the array
        * else, throw an exception
        * */
        String[] attributeValues = attributes.split("\\s*,\\s*");

        for (String attribute : attributeValues) {
            if (attribute.isEmpty()) {
                throw new InvalidConfigException("Attribute values cannot contain empty values");
            }
        }

        return attributeValues;
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
