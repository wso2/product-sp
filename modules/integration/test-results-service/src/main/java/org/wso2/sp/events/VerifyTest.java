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

package org.wso2.sp.events;

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
import org.wso2.sp.events.exception.TestNotFoundException;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
    private Map<String, Event> testResults = new HashMap<>();

    public VerifyTest() {

    }

    @GET
    @Path("/{testCaseName}")
    @Produces({"application/json", "text/xml"})
    @ApiOperation(
            value = "Return event details corresponding to the testcase name",
            notes = "Returns HTTP 404 if the testcase is not found")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Valid stock item found"),
            @ApiResponse(code = 404, message = "Stock item not found")})
    public Response getTestResults(@ApiParam(value = "testCaseName", required = true)
                                   @PathParam("testCaseName") String testCaseName) throws TestNotFoundException {

        //@CookieParam("testCaseName") String testCaseName
        log.info("Getting Test results using PathParam...");
        Event result;
        if (testResults.containsKey(testCaseName)) {
            result = testResults.get(testCaseName);
            if (result == null) {
                log.warn("No events found for : " + testCaseName);
                return Response.status(404).build();
            }
            return Response.ok().entity(result).build();
        }
        log.warn("Not found testcase : " + testCaseName);
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
        //, @Context Request request
        log.info("POST invoked");
        request.getHeaders().getAll().forEach(entry -> log.info(entry.getName() + "=" + entry.getValue()));

       /* if (testResults.containsKey(testCaseName)) {
            log.info("events exist for the test, not adding new.");
           //TODO: if events are already exist for the test
        }*/
        testResults.put(className, event.event);
        log.info("ClassName: " + className);
        log.info("event: " + testResults.get(className));

    }

    @POST
    @Path("/clear")
    public void clearMap() {
        log.info("event map clearing...");
        testResults.clear();
    }
}
