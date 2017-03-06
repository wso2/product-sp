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
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.core.event.Event;

import java.util.List;


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
     * @param streamAttributes  LinkedHashMap containing stream attribute names and types
     * @param dataList          list of attribute values to be converted to as event data
     * @return created Event
     */
    public static Event eventConverter(List<Attribute> streamAttributes, Object[] dataList) {

        Event event = new Event();
        Object[] eventData = new Object[streamAttributes.size()];

        //Convert attribute values according to attribute type in stream definition
        for (int j = 0; j < dataList.length; j++) {

            switch (streamAttributes.get(j).getType()) {
                case INT:
                    try {
                        eventData[j] = Integer.parseInt(String.valueOf(dataList[j]));
                    } catch (NumberFormatException e) {
                        throw new EventSimulationException("Incorrect value types for the attribute '" +
                                streamAttributes.get(j).getName() +
                                "', expected '" + streamAttributes.get(j).getType() + "' : " + e.getMessage());
                    }
                    break;
                case LONG:
                    try {
                        eventData[j] = Long.parseLong(String.valueOf(dataList[j]));
                    } catch (NumberFormatException e) {
                        throw new EventSimulationException("Incorrect value types for the attribute '" +
                                streamAttributes.get(j).getName() +
                                "', expected '" + streamAttributes.get(j).getType() + "' : " + e.getMessage());
                    }
                    break;
                case FLOAT:
                    try {
                        eventData[j] = Float.parseFloat(String.valueOf(dataList[j]));
                    } catch (NumberFormatException e) {
                        throw new EventSimulationException("Incorrect value types for the attribute '" +
                                streamAttributes.get(j).getName() +
                                "', expected '" + streamAttributes.get(j).getType() + "' : " + e.getMessage());
                    }
                    break;
                case DOUBLE:
                    try {
                        eventData[j] = Double.parseDouble(String.valueOf(dataList[j]));
                    } catch (NumberFormatException e) {
                        throw new EventSimulationException("Incorrect value types for the attribute '" +
                                streamAttributes.get(j).getName() +
                                "', expected '" + streamAttributes.get(j).getType() + "' : " + e.getMessage());
                    }
                    break;
                case STRING:
                    eventData[j] = dataList[j];
                    break;
                case BOOL:
                    if (String.valueOf(dataList[j]).equalsIgnoreCase("true") || String.valueOf(dataList[j]).equalsIgnoreCase("false")) {
                        eventData[j] = Boolean.parseBoolean(String.valueOf(dataList[j]));
                    } else {
                        throw new EventSimulationException(". Attribute : '" + streamAttributes.get(j).getName() +
                                "' expects a value of type '" + streamAttributes.get(j).getType() + "' : " +
                                new IllegalArgumentException().getMessage());
                    }
                    break;
                case OBJECT:
                    eventData[j] = dataList[j];
                    break;
            }

        }
        event.setData(eventData);
        return event;
    }
}
