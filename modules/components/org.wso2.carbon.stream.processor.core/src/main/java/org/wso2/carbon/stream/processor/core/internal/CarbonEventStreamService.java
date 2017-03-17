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

package org.wso2.carbon.stream.processor.core.internal;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.stream.processor.core.EventStreamService;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class which provides necessary apis for event stream related operations
 */
public class CarbonEventStreamService implements EventStreamService {

    private static Logger log = LoggerFactory.getLogger(CarbonEventStreamService.class);

    @Override
    public List<String> getStreamNames(String executionPlanName) {

        Map<String, ExecutionPlanRuntime> executionPlanRunTimeMap = StreamProcessorDataHolder.
                getStreamProcessorService().getExecutionPlanRunTimeMap();
        ExecutionPlanRuntime executionPlanRuntime = executionPlanRunTimeMap.get(executionPlanName);
        if (executionPlanRuntime != null) {
            if (executionPlanRuntime.getStreamDefinitionMap().size() != 0) {
                return new ArrayList<>(executionPlanRuntime.getStreamDefinitionMap().keySet());
            }
        } else {
            log.error("Execution Plan with name : " + executionPlanName + " is not available");
        }

        return null;
    }

    @Override
    public List<Attribute> getStreamAttributes(String executionPlanName, String streamName) {

        Map<String, ExecutionPlanRuntime> executionPlanRunTimeMap = StreamProcessorDataHolder.
                getStreamProcessorService().getExecutionPlanRunTimeMap();
        ExecutionPlanRuntime executionPlanRuntime = executionPlanRunTimeMap.get(executionPlanName);
        if (executionPlanRuntime != null) {
            if (executionPlanRuntime.getStreamDefinitionMap().size() != 0) {
                AbstractDefinition streamDefinition = executionPlanRuntime.getStreamDefinitionMap().get(streamName);
                return streamDefinition.getAttributeList();

            }
        }
        return null;
    }

    @Override
    public void pushEvent(String executionPlanName, String streamName, Event event) {

        Map<String, Map<String, InputHandler>> executionPlanSpecificInputHandlerMap = StreamProcessorDataHolder.
                getStreamProcessorService().
                getExecutionPlanSpecificInputHandlerMap();
        if (executionPlanSpecificInputHandlerMap != null) {
            Map<String, InputHandler> inputHandlerMap = executionPlanSpecificInputHandlerMap.get(executionPlanName);
            if (inputHandlerMap != null) {
                InputHandler inputHandler = inputHandlerMap.get(streamName);
                try {
                    inputHandler.send(event);
                } catch (InterruptedException e) {
                    log.error("Error when pushing events to Siddhi engine ", e);
                }
            }
        }


        log.info(executionPlanName);
        log.info(streamName);
        log.info(event.toString());
    }


}
