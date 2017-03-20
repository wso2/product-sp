/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.stream.processor.core.impl;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.stream.processor.core.api.ApiResponseMessage;
import org.wso2.carbon.stream.processor.core.api.NotFoundException;
import org.wso2.carbon.stream.processor.core.api.SiddhiApiService;
import org.wso2.carbon.stream.processor.core.internal.ExecutionPlanConfiguration;
import org.wso2.carbon.stream.processor.core.internal.StreamProcessorDataHolder;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.ws.rs.core.Response;

/**
 * Siddhi Service Implementataion Class
 */

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-03-15T08:56:59.657Z")
public class SiddhiApiServiceImpl extends SiddhiApiService {

    private Log log = LogFactory.getLog(SiddhiApiServiceImpl.class);
    private SiddhiManager siddhiManager = new SiddhiManager();
    private Map<String, Map<String, InputHandler>> executionPlanSpecificInputHandlerMap = new ConcurrentHashMap<>();
    private Map<String, ExecutionPlanConfiguration> executionPlanConfigurationMap = new ConcurrentHashMap<>();
    private Map<String, ExecutionPlanRuntime> executionPlanRunTimeMap = new ConcurrentHashMap<>();

    @Override
    public Response siddhiArtifactDeployPost(String executionPlan) throws NotFoundException {

        log.info("ExecutionPlan = " + executionPlan);
        String jsonString = new Gson().toString();
        try {
            if (StreamProcessorDataHolder.getStreamProcessorService().deployExecutionPlan(executionPlan)) {
                jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.OK,
                                                                      "Execution Plan is deployed " +
                                                                      "and runtime is created"));
            } else {
                jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.ERROR,
                                                                      "There is a Execution plan already " +
                                                                      "exists with same name"));
            }

        } catch (Exception e) {
            jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage()));
        }

        return Response.ok()
                .entity(jsonString)
                .build();
    }

    @Override
    public Response siddhiArtifactUndeployExecutionPlanGet(String executionPlan) throws NotFoundException {

        String jsonString = new Gson().toString();
        if (executionPlan != null) {
            if (StreamProcessorDataHolder.getStreamProcessorService().undeployExecutionPlan(executionPlan)) {

                jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.OK,
                                                                      "Execution plan removed successfully"));
            } else {
                jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.ERROR,
                                                                      "There is no execution plan exist " +
                                                                      "with provided name : " + executionPlan));
            }
        } else {
            jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.ERROR,
                                                                  "nvalid Request"));

        }
        return Response.ok()
                .entity(jsonString)
                .build();
    }
}
