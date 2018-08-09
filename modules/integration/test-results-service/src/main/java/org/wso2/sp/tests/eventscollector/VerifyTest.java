/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.wso2.sp.tests.eventscollector;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.Request;
import org.wso2.sp.tests.eventscollector.exception.TestNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;


/**
 * VerifyTest class.
 */

@Api(value = "testresults")
@SwaggerDefinition(
        info = @Info(
                title = "SP Test Events Swagger Definition", version = "1.0",
                description = "Stream Processor Test Events in-memory persisting service",
                license = @License(name = "Apache 2.0", url = "http://www.apache.org/licenses/LICENSE-2.0"),
                contact = @Contact(
                        name = "WSO2 Pvt Ltd",
                        email = "analytics-team@wso2.com",
                        url = "http://wso2.com"
                ))
)

@Path("/testresults")
public class VerifyTest {

    private static final Logger log = LoggerFactory.getLogger(VerifyTest.class);
    private Map<String, ArrayList<Event>> testResults = new HashMap<>();

    @GET
    @Path("/{testCaseName}")
    @Produces({"application/json", "text/xml"})
    @ApiOperation(
            value = "Return event corresponding to the testcase name or testcase name + index",
            notes = "Returns HTTP 404 if the testcase is not found")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Valid event found"),
            @ApiResponse(code = 404, message = "Test case not found")})
    public Response getSingleEvent(@ApiParam(value = "testCaseName", required = true)
                                   @PathParam("testCaseName") String testCaseName,
                                   @ApiParam(value = "Event index", required = true)
                                   @QueryParam("eventIndex") String index) throws TestNotFoundException {
        if (testResults.containsKey(testCaseName)) {
            if (index == null) {
                log.info("Retrieving siddhi events by testcase: " + testCaseName);
                ArrayList<Event> events = testResults.get(testCaseName);
                return Response.ok().entity(events).build();
            } else if (index.isEmpty()) {
                log.warn("Index is invalid : " + testCaseName);
                return Response.status(400).build();
            } else {
                try {
                    int eventIndex = Integer.parseInt(index);
                    if (testResults.get(testCaseName).size() > eventIndex) {
                        log.info("Retrieving siddhi events by testcase: " + testCaseName + " and index: " + eventIndex);
                        Event result = testResults.get(testCaseName).get(eventIndex);
                        return Response.ok().entity(result).build();
                    } else {
                        log.warn("No events found for index : " + eventIndex);
                        return Response.status(404).
                                entity("{\"message\":\"No events found for the index: " + eventIndex + "\"}").build();
                    }
                } catch (NumberFormatException nfe) {
                    log.warn("Index is invalid : " + testCaseName);
                    return Response.status(400).build();
                }
            }
        }
        log.warn("Couldn't find test case : " + testCaseName);
        return Response.status(404).entity("{\"message\":\"Couldn't find test case: " + testCaseName + "\"}").build();
    }

    @GET
    @Path("/{testCaseName}/count")
    @Produces({"application/json", "text/xml"})
    @ApiOperation(
            value = "Return event count corresponding to the testcase name",
            notes = "Returns HTTP 404 if the testcase is not found")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Valid event/s found"),
            @ApiResponse(code = 404, message = "Events not found")})
    public Response getEventsCount(@ApiParam(value = "testCaseName", required = true)
                                   @PathParam("testCaseName") String testCaseName) throws TestNotFoundException {
        log.info("Retrieving siddhi event count by testcase: " + testCaseName);

        if (testResults.containsKey(testCaseName)) {
            Integer count = testResults.get(testCaseName).size();
            return Response.ok().entity("{\"testCase\":\"" + testCaseName + "\",\"eventCount\":" + count + "}").build();
        }
        log.warn("Not Found : " + testCaseName);
        return Response.status(404).build();
    }

    @POST
    @Path("/")
    @Consumes("application/json")
    @ApiOperation(
            value = "Add a test result",
            notes = "Add a valid method name and res")
    public void addResult(@ApiParam(value = "Events object", required = true) EventWrapper event,
                          @ApiParam(value = "className string", required = true)
                          @HeaderParam("className") String className, @Context Request request) {
        log.info("POST invoked");
        request.getHeaders().getAll().forEach(entry -> log.info(entry.getName() + "=" + entry.getValue()));

        if (testResults.containsKey(className)) {
            log.info("adding event under existing test case.");
            ArrayList<Event> eventArrayList = testResults.get(className);
            eventArrayList.add(event.event);
            testResults.put(className, eventArrayList);
        } else {
            log.info("adding event under new test case.");
            ArrayList<Event> eventList = new ArrayList<>();
            eventList.add(event.event);
            testResults.put(className, eventList);
        }
        log.info("TestCaseName: " + className);

    }

    @POST
    @Path("/clear")
    public void clearMap() {
        log.info("event map clearing...");
        testResults.clear();
    }
}
