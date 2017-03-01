/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import scala.util.parsing.combinator.testing.Str;

/**
 * constants related to Event Simulator
 */
public class EventSimulatorConstants {
    public static final String ATTRIBUTETYPE_STRING = "String";
    public static final String ATTRIBUTETYPE_INTEGER = "Integer";
    public static final String ATTRIBUTETYPE_DOUBLE = "Double";
    public static final String ATTRIBUTETYPE_LONG = "Long";
    public static final String ATTRIBUTETYPE_FLOAT = "Float";
    public static final String ATTRIBUTETYPE_BOOLEAN = "Boolean";
    public static final String FEED_SIMULATION_STREAM_CONFIGURATION = "streamConfiguration";
    public static final String ORDER_BY_TIMESTAMP = "orderByTimeStamp";
    public static final String TIMESTAMP_ATTRIBUTE = "timestampAttribute";
    public static final String TIMESTAMP_POSITION = "timestampPosition";
    public static final String ATTRIBUTE_NAME = "attributeName";

    //Feed Simulation type constants
    public static final String RANDOM_DATA_SIMULATION = "RandomDataSimulation";
    public static final String FILE_FEED_SIMULATION = "FileFeedSimulation";
    public static final String DATABASE_FEED_SIMULATION = "DatabaseFeedSimulation";
    public static final String SINGLE_EVENT_SIMULATION = "SingleEventSimulation";

    //Feed Simulation stream Configuration constants

    //RandomDataSimulation constants
    public static final String FEED_SIMULATION_TYPE = "simulationType";
    public static final String STREAM_NAME = "streamName";
    public static final String EVENTS = "events";
    public static final String DELAY = "delay";
    public static final String ATTRIBUTE_CONFIGURATION = "attributeConfiguration";
    public static final String EXECUTION_PLAN_NAME = "executionPlanName";

    //filefeedsimulation constants
    public static final String FILE_NAME = "fileName";
    public static final String DELIMITER = "delimiter";

    //Random data feed simulation constants
    public static final String RANDOMDATAGENERATORTYPE = "type";
    public static final String PROPERTYBASEDATTRIBUTE_CATEGORY = "category";
    public static final String PROPERTYBASEDATTRIBUTE_PROPERTY = "property";
    public static final String REGEXBASEDATTRIBUTE_PATTERN = "pattern";
    public static final String PRIMITIVEBASEDATTRIBUTE_MIN = "min";
    public static final String PRIMITIVEBASEDATTRIBUTE_MAX = "max";
    public static final String PRIMITIVEBASEDATTRIBUTE_LENGTH_DECIMAL = "length";
    public static final String CUSTOMDATABASEDATTRIBUTE_LIST = "list";

//    Database feed simulation constants
    public static final String DATABASE_CONFIGURATION_NAME = "databaseConfigName";
    public static final String DATABASE_NAME = "databaseName";
    public static final String USER_NAME = "username";
    public static final String PASSWORD = "password";
    public static final String TABLE_NAME = "tableName";
    public static final String COLUMN_NAMES_AND_TYPES = "columnNamesAndTypes";
    public static final String COLUMN_NAME = "columnName";
    public static final String COLUMN_TYPE = "type";
}
