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
import org.wso2.carbon.stream.processor.core.model.Artifact;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;

import java.util.ArrayList;
import java.util.List;
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

        return Response.ok().entity(jsonString).build();
    }

    @Override
    public Response siddhiArtifactUndeployExecutionPlanGet(String executionPlanName) throws NotFoundException {

        String jsonString = new Gson().toString();
        if (executionPlanName != null) {
            if (StreamProcessorDataHolder.getStreamProcessorService().undeployExecutionPlan(executionPlanName)) {

                jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.OK,
                                                                      "Execution plan removed successfully"));
            } else {
                jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.ERROR,
                                                                      "There is no execution plan exist " +
                                                                      "with provided name : " + executionPlanName));
            }
        } else {
            jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.ERROR,
                                                                  "Invalid Request"));

        }
        return Response.ok().entity(jsonString).build();
    }

    @Override
    public Response siddhiArtifactListGet() throws NotFoundException {

        List<Artifact> artifactList = new ArrayList<>();
        for (ExecutionPlanConfiguration executionPlanConfiguration : executionPlanConfigurationMap.values()) {
            Artifact artifact = new Artifact();
            artifact.setName(executionPlanConfiguration.getName());
            artifact.setQuery(executionPlanConfiguration.getExecutionPlan());
            artifactList.add(artifact);
        }
        return Response.ok().entity(artifactList).build();

    }

    @Override
    public Response siddhiStateSnapshotExecutionPlanNamePost(String executionPlanName) throws NotFoundException {

        String jsonString;
        ExecutionPlanRuntime executionPlanRuntime = StreamProcessorDataHolder.getSiddhiManager().
                getExecutionPlanRuntime(executionPlanName);
        if (executionPlanRuntime != null) {
            executionPlanRuntime.persist();
            jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.ERROR,
                                                                  "State persisted for execution plan :" +
                                                                  executionPlanName));
        } else {
            jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.ERROR,
                                                                  "There is no execution plan exist " +
                                                                  "with provided name : " + executionPlanName));
        }

        return Response.ok().entity(jsonString).build();
    }

    @Override
    public Response siddhiStateRestoreExecutionPlanNamePost(String executionPlanName) throws NotFoundException {

        String jsonString;
        ExecutionPlanRuntime executionPlanRuntime = StreamProcessorDataHolder.getSiddhiManager().
                getExecutionPlanRuntime(executionPlanName);
        if (executionPlanRuntime != null) {
            executionPlanRuntime.restoreLastRevision();
            jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.ERROR,
                                                                  "State restored for execution plan :" +
                                                                  executionPlanName));
        } else {
            jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.ERROR,
                                                                  "There is no execution plan exist " +
                                                                  "with provided name : " + executionPlanName));
        }

        return Response.ok().entity(jsonString).build();
    }
}
