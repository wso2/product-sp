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

package org.wso2.streamprocessor.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.siddhi.core.event.Event;

/**
 * The EventReceiverServiceImpl class implements EventReceiverService class.
 * This is the service used to send events from event simulator to stream processor
 * */

public class EventReceiverServiceImpl implements EventReceiverService {
    private static Logger logger = LoggerFactory.getLogger(EventReceiverService.class);

    /**
     * The eventReceiverService method is used to send events from event simulator to the stream processor
     *
     * @param executionPlanName : the name of the execution plan being simulated
     * @param streamName        : the name of the stream to which the event should be sent
     * @param event             : the event created
     * */
    @Override
    public void eventReceiverService(String executionPlanName, String streamName, Event event) {
        logger.info(executionPlanName);
        logger.info(streamName);
        logger.info(event.toString());
    }
}
