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
package org.wso2.carbon.event.simulator.core.internal.util;

/**
 * constants related to Event Simulator
 */
public class EventSimulatorConstants {
    public static final String DIRECTORY_NAME = "eventSimulator";

    public static final String EVENT_SIMULATION_PROPERTIES = "properties";
    public static final String EVENT_SIMULATION_NAME = "simulationName";
    public static final String EVENT_SIMULATION_SOURCES = "sources";
    public static final String TIMESTAMP_ATTRIBUTE = "timestampAttribute";
    public static final String TIMESTAMP_START_TIME = "timestampStartTime";
    public static final String TIMESTAMP_END_TIME = "timestampEndTime";
    public static final String SIMULATION_TIME_INTERVAL = "timeInterval";
    public static final String NUMBER_OF_EVENTS_REQUIRED = "noOfEvents";


    //Feed Simulation stream Configuration constants

    //SingleEventSimulation constants
    public static final String SINGLE_EVENT_TIMESTAMP = "timestamp";
    public static final String SINGLE_EVENT_DATA = "data";


    //RandomDataSimulation constants
    public static final String EVENT_SIMULATION_TYPE = "simulationType";
    public static final String STREAM_NAME = "streamName";
    public static final String TIME_INTERVAL = "timeInterval";
    public static final String ATTRIBUTE_CONFIGURATION = "attributeConfiguration";
    public static final String EXECUTION_PLAN_NAME = "executionPlanName";

    //file simulation constants
    public static final String FILE_NAME = "fileName";
    public static final String DELIMITER = "delimiter";
    public static final String IS_ORDERED = "isOrdered";


    //Random data simulation constants
    public static final String RANDOM_DATA_GENERATOR_TYPE = "type";
    public static final String PROPERTY_BASED_ATTRIBUTE_PROPERTY = "property";
    public static final String REGEX_BASED_ATTRIBUTE_PATTERN = "pattern";
    public static final String PRIMITIVE_BASED_ATTRIBUTE_TYPE = "primitiveType";
    public static final String PRIMITIVE_BASED_ATTRIBUTE_MIN = "min";
    public static final String PRIMITIVE_BASED_ATTRIBUTE_MAX = "max";
    public static final String PRIMITIVE_BASED_ATTRIBUTE_LENGTH = "length";
    public static final String CUSTOM_DATA_BASED_ATTRIBUTE_LIST = "list";

    //    Database simulation constants
    public static final String DRIVER = "driver";
    public static final String DATA_SOURCE_LOCATION = "dataSourceLocation";
    public static final String USER_NAME = "username";
    public static final String PASSWORD = "password";
    public static final String TABLE_NAME = "tableName";
    public static final String COLUMN_NAMES_LIST = "columnNamesList";
}
