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

package org.wso2.carbon.stream.processor.core.api;

import io.swagger.annotations.ApiParam;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.stream.processor.core.factories.SiddhiApiServiceFactory;
import org.wso2.carbon.stream.processor.core.model.Artifact;
import org.wso2.carbon.stream.processor.core.model.Success;
import org.wso2.msf4j.Microservice;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * Auto generated class from Swagger to MSF4J.
 */

@Component(
        name = "siddhi-core-services",
        service = Microservice.class,
        immediate = true
)
@Path("/siddhi")
@io.swagger.annotations.Api(description = "The siddhi API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-03-15T08:56:59.657Z")
public class SiddhiApi implements Microservice {
    private final SiddhiApiService delegate = SiddhiApiServiceFactory.getSiddhiApi();

    @POST
    @Path("/artifact/deploy")
    @Consumes({"text/plain"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "", notes = "Deploys the execution plan. Request **executionPlan** " +
                                                             "explains the Siddhi Query ",
            response = Success.class, tags = {"artifact"})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Successful response", response = Success.class),
            @io.swagger.annotations.ApiResponse(code = 200, message = "Unexpected error", response = Success.class)})
    public Response siddhiArtifactDeployPost(@ApiParam(value = "Siddhi Execution Plan", required = true) String body)
            throws NotFoundException {
        return delegate.siddhiArtifactDeployPost(body);
    }

    @POST
    @Path("/artifact/undeploy/{executionPlanName}")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "", notes = "Undeploys the execution plan as given by " +
                                                             "`executionPlanName`. Path param of " +
                                                             "**executionPlanName** " +
                                                             "determines name of the execution plan ",
            response = Success.class, tags = {"artifact"})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Successful response", response = Success.class),
            @io.swagger.annotations.ApiResponse(code = 200, message = "Unexpected error", response = Success.class)})
    public Response siddhiArtifactUndeployExecutionPlanPost(
            @ApiParam(value = "Execution Plan Name", required = true) @PathParam("executionPlanName")
                    String executionPlanName)
            throws NotFoundException {
        return delegate.siddhiArtifactUndeployExecutionPlanPost(executionPlanName);
    }

    @GET
    @Path("/artifact/list")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Lists Siddhi execution plans", notes = "Provides list of execution " +
                                                                                         "plans that active.",
            response = Artifact.class,
            tags = {"artifact"})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation",
                    response = Artifact.class),

            @io.swagger.annotations.ApiResponse(code = 200, message = "Unexpected error", response = Artifact.class)})
    public Response siddhiArtifactListGet()
            throws NotFoundException {
        return delegate.siddhiArtifactListGet();
    }

    @POST
    @Path("/state/snapshot/{executionPlanName}")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "", notes = "Persist the State for the provided execution Plan ",
            response = Success.class, tags = {"state"})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Successful response", response = Success.class),

            @io.swagger.annotations.ApiResponse(code = 200, message = "Unexpected error", response = Success.class)})
    public Response siddhiStateSnapshotExecutionPlanNamePost(
            @ApiParam(value = "Execution Plan Name", required = true) @PathParam("executionPlanName")
                    String executionPlanName
    )
            throws NotFoundException {
        return delegate.siddhiStateSnapshotExecutionPlanNamePost(executionPlanName);
    }

    @POST
    @Path("/state/restore/{executionPlanName}")

    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "", notes = "Restore the State for the provided execution Plan ",
            response = Success.class, tags = {"state"})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Successful response", response = Success.class),

            @io.swagger.annotations.ApiResponse(code = 200, message = "Unexpected error", response = Success.class)})
    public Response siddhiStateRestoreExecutionPlanNamePost(@ApiParam(value = "Execution Plan Name", required = true)
                                                            @PathParam("executionPlanName") String executionPlanName
    )
            throws NotFoundException {
        return delegate.siddhiStateRestoreExecutionPlanNamePost(executionPlanName);
    }
}
