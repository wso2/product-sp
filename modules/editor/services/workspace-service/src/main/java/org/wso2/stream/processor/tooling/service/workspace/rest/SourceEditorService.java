/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.stream.processor.tooling.service.workspace.rest;

import com.google.gson.Gson;
import org.wso2.stream.processor.tooling.service.workspace.siddhi.editor.commons.request.ValidationRequest;
import org.wso2.stream.processor.tooling.service.workspace.siddhi.editor.commons.response.GeneralResponse;
import org.wso2.stream.processor.tooling.service.workspace.siddhi.editor.commons.response.MetaDataResponse;
import org.wso2.stream.processor.tooling.service.workspace.siddhi.editor.commons.response.Status;
import org.wso2.stream.processor.tooling.service.workspace.siddhi.editor.commons.response.ValidationSuccessResponse;
import org.wso2.stream.processor.tooling.service.workspace.siddhi.editor.util.MetaDataHolder;
import org.wso2.stream.processor.tooling.service.workspace.siddhi.editor.util.SourceEditorUtils;
import org.wso2.siddhi.core.ExecutionPlanRuntime;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * HTTP Responses for siddhi source editor related requests
 */
@Path("/siddhi-editor")
public class SourceEditorService {
    @POST
    @Path("/validator")
    public Response validateExecutionPlan(String validationRequestString) {
        ValidationRequest validationRequest = new Gson().fromJson(validationRequestString, ValidationRequest.class);
        String jsonString;

        try {
            ExecutionPlanRuntime executionPlanRuntime =
                    SourceEditorUtils.validateExecutionPlan(validationRequest.getExecutionPlan());

            // Status SUCCESS to indicate that the execution plan is valid
            ValidationSuccessResponse response = new ValidationSuccessResponse(Status.SUCCESS);

            // Getting requested inner stream definitions
            if (validationRequest.getMissingInnerStreams() != null) {
                response.setInnerStreams(SourceEditorUtils.getInnerStreamDefinitions(
                        executionPlanRuntime, validationRequest.getMissingInnerStreams()
                ));
            }

            // Getting requested stream definitions
            if (validationRequest.getMissingStreams() != null) {
                response.setStreams(SourceEditorUtils.getStreamDefinitions(
                        executionPlanRuntime, validationRequest.getMissingStreams()
                ));
            }

            jsonString = new Gson().toJson(response);
        } catch (Throwable t) {
            jsonString = new Gson().toJson(new GeneralResponse(Status.ERROR, t.getMessage()));
        }

        return Response.ok(jsonString, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", "*")     // TODO : remove this header when ports are decided
                .build();
    }

    @GET
    @Path("/metadata")
    public Response getMetaData() {
        MetaDataResponse response = new MetaDataResponse(Status.SUCCESS);
        response.setInBuilt(MetaDataHolder.getInBuiltProcessorMetaData());
        response.setExtensions(SourceEditorUtils.getExtensionProcessorMetaData());

        String jsonString = new Gson().toJson(response);
        return Response.ok(jsonString, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", "*")     // TODO : remove this header when ports are decided
                .build();
    }
}
