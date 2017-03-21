/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.wso2.carbon.stream.processor.core.internal;


import org.wso2.carbon.stream.processor.core.internal.util.EventProcessorConstants;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.query.api.ExecutionPlan;
import org.wso2.siddhi.query.api.util.AnnotationHelper;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Class which manage execution plans
 */
public class StreamProcessorService {

    private Map<String, ExecutionPlanRuntime> executionPlanRunTimeMap = new ConcurrentHashMap<>();
    private Map<String, Map<String, InputHandler>> executionPlanSpecificInputHandlerMap = new ConcurrentHashMap<>();
    private Map<String, ExecutionPlanConfiguration> executionPlanConfigurationMap = new ConcurrentHashMap<>();

    public boolean deployExecutionPlan(String executionPlan) {
        ExecutionPlan parsedExecutionPlan = SiddhiCompiler.parse(executionPlan);
        String executionPlanName = AnnotationHelper.getAnnotationElement(EventProcessorConstants.ANNOTATION_NAME_NAME,
                                                                         null, parsedExecutionPlan.
                        getAnnotations()).getValue();
        if (!executionPlanRunTimeMap.containsKey(executionPlan)) {

            SiddhiManager siddhiManager = StreamProcessorDataHolder.getSiddhiManager();
            //Check this and have a separate config
            ExecutionPlanConfiguration executionPlanConfiguration = new ExecutionPlanConfiguration();

            executionPlanConfiguration.setName(executionPlanName);
            executionPlanConfiguration.setExecutionPlan(executionPlan);
            executionPlanConfigurationMap.put(executionPlanName, executionPlanConfiguration);

            ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

            if (executionPlanRuntime != null) {
                Set<String> streamNames = executionPlanRuntime.getStreamDefinitionMap().keySet();
                Map<String, InputHandler> inputHandlerMap =
                        new ConcurrentHashMap<String, InputHandler>(streamNames.size());
                for (String streamName : streamNames) {
                    inputHandlerMap.put(streamName, executionPlanRuntime.getInputHandler(streamName));
                }

                executionPlanSpecificInputHandlerMap.put(executionPlanName, inputHandlerMap);

                executionPlanRunTimeMap.put(executionPlan, executionPlanRuntime);
                executionPlanRuntime.start();

                return true;
            }
        }
        return false;

    }

    public boolean undeployExecutionPlan(String executionPlanName) {
        if (executionPlanRunTimeMap.containsKey(executionPlanName)) {
            executionPlanRunTimeMap.remove(executionPlanName);
            executionPlanConfigurationMap.remove(executionPlanName);
            executionPlanSpecificInputHandlerMap.remove(executionPlanName);

            return true;
        }
        return false;
    }

    public Map<String, ExecutionPlanRuntime> getExecutionPlanRunTimeMap() {
        return executionPlanRunTimeMap;
    }

    public Map<String, Map<String, InputHandler>> getExecutionPlanSpecificInputHandlerMap() {
        return executionPlanSpecificInputHandlerMap;
    }
}
