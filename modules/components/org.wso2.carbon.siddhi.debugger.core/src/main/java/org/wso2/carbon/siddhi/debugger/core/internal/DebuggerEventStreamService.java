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

package org.wso2.carbon.siddhi.debugger.core.internal;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.stream.processor.common.EventStreamService;
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
public class DebuggerEventStreamService implements EventStreamService {
    private static Logger log = LoggerFactory.getLogger(DebuggerEventStreamService.class);

    @Override
    public List<String> getStreamNames(String runtimeId) {
        Map<String, ExecutionPlanRuntime> executionPlanRunTimeMap =
                DebuggerDataHolder.getDebugProcessorService().getExecutionPlanRunTimeMap();
        ExecutionPlanRuntime runtime = executionPlanRunTimeMap.get(runtimeId);
        if (runtime != null) {
            if (runtime.getStreamDefinitionMap().size() != 0) {
                return new ArrayList<>(runtime.getStreamDefinitionMap().keySet());
            }
        } else {
            log.error("Execution Plan Runtime with id : " + runtimeId + " is not available");
        }
        return null;
    }

    @Override
    public List<Attribute> getStreamAttributes(String runtimeId, String streamName) {
        Map<String, ExecutionPlanRuntime> executionPlanRunTimeMap =
                DebuggerDataHolder.getDebugProcessorService().getExecutionPlanRunTimeMap();
        ExecutionPlanRuntime runtime = executionPlanRunTimeMap.get(runtimeId);
        if (runtime != null) {
            if (runtime.getStreamDefinitionMap().size() != 0) {
                AbstractDefinition streamDefinition = runtime.getStreamDefinitionMap().get(streamName);
                return streamDefinition.getAttributeList();
            }
        } else {
            log.error("Execution Plan Runtime with id : " + runtimeId + " is not available");
        }
        return null;
    }

    @Override
    public void pushEvent(String runtimeId, String streamName, Event event) {
        Map<String, Map<String, InputHandler>> handlerMap = DebuggerDataHolder.
                getDebugProcessorService().getRuntimeSpecificInputHandlerMap();
        if (handlerMap != null) {
            Map<String, InputHandler> inputHandlerMap = handlerMap.get(runtimeId);
            if (inputHandlerMap != null) {
                InputHandler inputHandler = inputHandlerMap.get(streamName);
                try {
                    inputHandler.send(event);
                } catch (InterruptedException e) {
                    log.error("Error when pushing events to siddhi debugger engine ", e);
                }
            }
        }
        log.info(runtimeId);
        log.info(streamName);
        log.info(event.toString());
    }
}
