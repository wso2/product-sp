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


import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.stream.processor.common.EventStreamService;
import org.wso2.msf4j.Microservice;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.debugger.SiddhiDebugger;

import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;


@Component(
        name = "siddhi-debugger-core-services",
        service = Microservice.class,
        immediate = true
)
@Path("/debugger")
public class ServiceComponent implements Microservice {
    public static final Logger log = LoggerFactory.getLogger(ServiceComponent.class);
    private ServiceRegistration serviceRegistration;

    @POST
    @Produces("application/json")
    @Path("/debug")
    public Response debug(String executionPlan) {
        String runtimeId = DebuggerDataHolder.getDebugProcessorService().deployAndDebug(executionPlan);
        Set<String> streams = DebuggerDataHolder.getDebugProcessorService().getRuntimeSpecificStreamsMap().get(runtimeId);
        return Response.ok().entity("{id:'" + runtimeId + "', streams:" + streams + "}").build();
    }

    public Response acquireBreakPoint(String runtimeId, String queryName, SiddhiDebugger.QueryTerminal queryTerminal) {
        return Response.ok().entity("some-value").build();
    }

    public Response releaseBreakPoint(String runtimeId, String queryName, SiddhiDebugger.QueryTerminal queryTerminal) {
        return null;
    }

    @GET
    @Produces("application/json")
    @Path("/release/{runtimeId}")
    public Response releaseAllBreakPoints(@PathParam("runtimeId") String runtimeId) {
        DebuggerDataHolder.getDebugProcessorService().getSiddhiDebuggerMap().get(runtimeId).releaseAllBreakPoints();
        return Response.status(Response.Status.OK).entity("{'status':'ok'}").build();
    }

    @GET
    @Produces("application/json")
    @Path("/next/{runtimeId}")
    public Response next(@PathParam("runtimeId") String runtimeId) {
        DebuggerDataHolder.getDebugProcessorService().getSiddhiDebuggerMap().get(runtimeId).next();
        return Response.status(Response.Status.OK).entity("{'status':'ok'}").build();
    }

    @GET
    @Produces("application/json")
    @Path("/play/{runtimeId}")
    public Response play(@PathParam("runtimeId") String runtimeId) {
        DebuggerDataHolder.getDebugProcessorService().getSiddhiDebuggerMap().get(runtimeId).play();
        return Response.status(Response.Status.OK).entity("{'status':'ok'}").build();
    }

    @GET
    @Produces("application/json")
    @Path("/state/{runtimeId}/{queryName}")
    public Response getQueryState(@PathParam("runtimeId") String runtimeId, @PathParam("queryName") String queryName) {
        return Response.status(Response.Status.OK).entity(DebuggerDataHolder.getDebugProcessorService()
                .getSiddhiDebuggerMap().get(runtimeId).getQueryState(queryName)).build();
    }

    /**
     * This is the activation method of ServiceComponent. This will be called when its references are
     * satisfied.
     *
     * @param bundleContext the bundle context instance of this bundle.
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start(BundleContext bundleContext) throws Exception {
        log.info("Service Component is activated");

        // Create Stream Processor Service
        DebuggerDataHolder.setDebugProcessorService(new DebugProcessorService());
        DebuggerDataHolder.setSiddhiManager(new SiddhiManager());

        serviceRegistration = bundleContext.registerService(EventStreamService.class.getName(),
                new DebuggerEventStreamService(), null);
    }

    /**
     * This is the deactivation method of ServiceComponent. This will be called when this component
     * is being stopped or references are satisfied during runtime.
     *
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() throws Exception {
        log.info("Service Component is deactivated");

        Map<String, ExecutionPlanRuntime> executionPlanRunTimeMap = DebuggerDataHolder.
                getDebugProcessorService().getExecutionPlanRunTimeMap();
        for (ExecutionPlanRuntime runtime : executionPlanRunTimeMap.values()) {
            runtime.shutdown();
        }

        serviceRegistration.unregister();
    }
}