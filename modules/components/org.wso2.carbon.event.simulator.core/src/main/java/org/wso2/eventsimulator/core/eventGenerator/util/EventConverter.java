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

import org.wso2.eventsimulator.core.eventGenerator.util.exceptions.EventGenerationException;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.query.api.definition.Attribute;

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
     * @param streamAttributes List containing stream attribute names and types
     * @param dataList         list of attribute values to be converted to as event data
     * @param timestamp        timestamp to be assigned to the event
     * @return created Event
     */
    public static Event eventConverter(List<Attribute> streamAttributes, Object[] dataList, Long timestamp) {

        Event event = new Event();
        Object[] eventData = new Object[streamAttributes.size()];

        //Convert attribute values according to attribute type in stream definition
        for (int i = 0; i < dataList.length; i++) {

            try {
                switch (streamAttributes.get(i).getType()) {
                    case INT:
                        eventData[i] = Integer.parseInt(String.valueOf(dataList[i]));
                        break;
                    case LONG:
                        eventData[i] = Long.parseLong(String.valueOf(dataList[i]));
                        break;
                    case FLOAT:
                        eventData[i] = Float.parseFloat(String.valueOf(dataList[i]));
                        break;
                    case DOUBLE:
                        eventData[i] = Double.parseDouble(String.valueOf(dataList[i]));
                        break;
                    case STRING:
                        eventData[i] = String.valueOf(dataList[i]);
                        break;
                    case BOOL:
                        eventData[i] = Boolean.parseBoolean(String.valueOf(dataList[i]));
                        break;
                }
            } catch (NumberFormatException e) {
                throw new EventGenerationException("Error occurred when setting event data. Attribute '" +
                        streamAttributes.get(i).getName() + "' expects a value of type '" +
                        streamAttributes.get(i).getType() + "' : ", e);
            }
        }
        event.setTimestamp(timestamp);
        event.setData(eventData);
        return event;
    }
}
