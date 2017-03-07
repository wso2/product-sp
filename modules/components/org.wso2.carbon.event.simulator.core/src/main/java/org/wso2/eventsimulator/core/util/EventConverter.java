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

import org.wso2.eventsimulator.core.simulator.exception.EventSimulationException;
import org.wso2.streamprocessor.core.StreamDefinitionRetriever;
import org.wso2.siddhi.core.event.Event;

import java.util.ArrayList;
import java.util.LinkedHashMap;


/**
 * Event converter convert the given attribute list as event
 */
public class EventConverter {
    /**
     * Initialize the EventConverter
     */
    private EventConverter() {
    }

    /**
     * Initialize Event
     * Convert convert the given attribute list as event
     *
     * @param streamDefinition  LinkedHashMap containing stream attribute names and types
     * @param dataList          list of attribute values to be converted to as event data
     * @return created Event
     */
    public static Event eventConverter(LinkedHashMap<String,StreamDefinitionRetriever.Type> streamDefinition, String[] dataList) {

        Event event = new Event();
        Object[] eventData = new Object[streamDefinition.size()];
        ArrayList<String> streamAttributeNames = new ArrayList<String>(streamDefinition.keySet());
        ArrayList<StreamDefinitionRetriever.Type> streamAttributeTypes = new ArrayList<StreamDefinitionRetriever.Type>(streamDefinition.values());

        //Convert attribute values according to attribute type in stream definition
        for (int j = 0; j < dataList.length; j++) {

            StreamDefinitionRetriever.Type type = streamAttributeTypes.get(j);
            switch (type) {
                case INTEGER:
                    try {
                        eventData[j] = Integer.parseInt(String.valueOf(dataList[j]));
                    } catch (NumberFormatException e) {
                        throw new EventSimulationException("Incorrect value types for the attribute '" +
                                streamAttributeNames.get(j) +
                                "', expected '" + streamAttributeTypes.get(j) + "' : " + e.getMessage());
                    }
                    break;
                case LONG:
                    try {
                        eventData[j] = Long.parseLong(String.valueOf(dataList[j]));
                    } catch (NumberFormatException e) {
                        throw new EventSimulationException("Incorrect value types for the attribute '" +
                                streamAttributeNames.get(j) +
                                "', expected '" + streamAttributeTypes.get(j) + "' : " + e.getMessage());
                    }
                    break;
                case FLOAT:
                    try {
                        eventData[j] = Float.parseFloat(String.valueOf(dataList[j]));
                    } catch (NumberFormatException e) {
                        throw new EventSimulationException("Incorrect value types for the attribute '" +
                                streamAttributeNames.get(j) +
                                "', expected '" + streamAttributeTypes.get(j) + "' : " + e.getMessage());
                    }
                    break;
                case DOUBLE:
                    try {
                        eventData[j] = Double.parseDouble(String.valueOf(dataList[j]));
                    } catch (NumberFormatException e) {
                        throw new EventSimulationException("Incorrect value types for the attribute '" +
                                streamAttributeNames.get(j) +
                                "', expected '" + streamAttributeTypes.get(j) + "' : " + e.getMessage());
                    }
                    break;
                case STRING:
                    eventData[j] = dataList[j];
                    break;
                case BOOLEAN:
                    if (String.valueOf(dataList[j]).equalsIgnoreCase("true") || String.valueOf(dataList[j]).equalsIgnoreCase("false")) {
                        eventData[j] = Boolean.parseBoolean(String.valueOf(dataList[j]));
                    } else {
                        throw new EventSimulationException(". Attribute : '" + streamAttributeNames.get(j) +
                                "' expects a value of type '" + streamAttributeTypes.get(j) + "' : " +
                                new IllegalArgumentException().getMessage());
                    }
                    break;
            }

        }
        event.setData(eventData);
        return event;
    }
}
