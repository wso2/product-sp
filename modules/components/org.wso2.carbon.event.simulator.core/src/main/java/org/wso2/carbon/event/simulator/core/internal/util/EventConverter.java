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

import org.wso2.carbon.event.simulator.core.exception.EventGenerationException;
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
     * @param dataArray        list of attribute values to be converted to as event data
     * @param timestamp        timestamp to be assigned to the event
     * @return created Event
     */
    public static Event eventConverter(List<Attribute> streamAttributes, Object[] dataArray, Long timestamp) {
        Object[] eventData = new Object[streamAttributes.size()];
        /*
         * Convert attribute values according to attribute type in stream definition
         * iterate the data array.
         * for each data item in data array, check the respective attribute type in stream attributes list and parse
         * the data item accordingly.
         * if the data item cant be parsed, the NumberFormatException will be wrapped as an EventGenerationException
         * */
        for (int i = 0; i < dataArray.length; i++) {
            try {
                switch (streamAttributes.get(i).getType()) {
                    case INT:
                        eventData[i] = Integer.parseInt(String.valueOf(dataArray[i]));
                        break;
                    case LONG:
                        eventData[i] = Long.parseLong(String.valueOf(dataArray[i]));
                        break;
                    case FLOAT:
                        eventData[i] = Float.parseFloat(String.valueOf(dataArray[i]));
                        break;
                    case DOUBLE:
                        eventData[i] = Double.parseDouble(String.valueOf(dataArray[i]));
                        break;
                    case STRING:
                        eventData[i] = String.valueOf(dataArray[i]);
                        break;
                    case BOOL:
                        eventData[i] = Boolean.parseBoolean(String.valueOf(dataArray[i]));
                        break;
                    default:
//                        this statement is never reached since attribute type is an enum
                }
            } catch (NumberFormatException e) {
                throw new EventGenerationException("Error occurred when parsing event data. Attribute value " +
                        "is incompatible with stream attribute. Attribute '" + streamAttributes.get(i).getName() + "'" +
                        " expects a value of type '" + streamAttributes.get(i).getType() + "' : ", e);
            }
        }
        Event event = new Event();
        event.setTimestamp(timestamp);
        event.setData(eventData);
        return event;
    }
}
