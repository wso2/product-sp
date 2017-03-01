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

/**
 * Represent the Event Item
 */
public class Event {
    /**
     * Stream Name that the event represents
     */
    private String streamName;

    /**
     * Array of data values of event for databaseFeedSimulation particular input stream
     */
    private Object[] eventData;

    /**
     * Initialize the Event
     */
    public Event() {
    }

    /**
     * get Stream Name
     *
     * @return stream Name
     */
    public String getStreamName() {
        return streamName;
    }

    /**
     * set the value for Stream Name
     *
     * @param streamName Stream Name
     */
    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    /**
     * get array of event data values
     *
     * @return Object array of event data values
     */
    public Object[] getEventData() {
        return eventData;
    }

    /**
     * set Event data values
     *
     * @param eventData Object array of Event data values
     */
    public void setEventData(Object[] eventData) {
        this.eventData = eventData;
    }
}
