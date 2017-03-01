/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.eventsimulator.core.endpoint;


import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.wso2.eventsimulator.core.simulator.bean.FeedSimulationDto;
import org.wso2.eventsimulator.core.simulator.bean.FeedSimulationStreamConfiguration;
import org.wso2.eventsimulator.core.simulator.csvFeedSimulation.core.FileUploader;
import org.wso2.eventsimulator.core.simulator.exception.EventSimulationException;
import org.wso2.eventsimulator.core.simulator.exception.ValidationFailedException;
import org.wso2.eventsimulator.core.simulator.singleventsimulator.SingleEventDto;
import org.wso2.eventsimulator.core.util.EventSimulatorParser;
import org.wso2.eventsimulator.core.util.EventSimulatorPoolExecutor;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.formparam.FormDataParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Simulator REST service is databaseFeedSimulation micro-service built on top of WSO2 msf4j.
 * The REST service provides the capability of simulating events.
 */

@Path("/EventSimulation")
public class EventSimulatorRestService {
    private static final Logger log = Logger.getLogger(EventSimulatorRestService.class);

    /**
     * Event simulator service executor for event simulator REST service.
     */
    private Map<String, EventSimulatorPoolExecutor> executorMap;

    /**
     * Initializes the service classes for resources.
     */
    public EventSimulatorRestService() {
        executorMap = new ConcurrentHashMap<>();
    }

    /**
     * Send single event for simulation
     *
     * @param simulationString jsonString to be converted to SingleEventDto object from the request Json body.
     *                         <p>
     *                         http://localhost:8080/EventSimulation/singleEventSimulation
     *                         <pre>
     *                         curl  -X POST -d '{"streamName":"cseEventStream","attributeValues":["WSO2","345","56"]}' http://localhost:8080/EventSimulation/singleEventSimulation
     *                        </pre>
     *                         <p>
     *                         Eg :simulationString: {
     *                         "streamName":"cseEventStream",
     *                         "attributeValues":attributeValue
     *                         };
     */
    @POST
    @Path("/singleEventSimulation")
    public Response singleEventSimulation(String simulationString) {
        if (log.isDebugEnabled()) {
            log.debug("Single Event Simulation");
        }
        String jsonString;

        try {
            //parse json string to SingleEventDto object
            SingleEventDto singleEventSimulationConfiguration = EventSimulatorParser.singleEventSimulatorParser(simulationString);
            FeedSimulationDto feedSimulationDto = new FeedSimulationDto();
            feedSimulationDto.setStreamConfigurationList(new ArrayList<FeedSimulationStreamConfiguration>() {{
                add(singleEventSimulationConfiguration);
            }});

            //start single event simulation
            // TODO: 2/4/17 is there a better way????
            EventSimulatorPoolExecutor.newEventSimulatorPool(feedSimulationDto, 1);

            jsonString = new Gson().toJson("Event is send successfully");
        } catch (EventSimulationException e) {
            throw new EventSimulationException("Single Event simulation failed : " + e.getMessage());
        }
        return Response.ok().entity(jsonString).build();
    }

    /**
     * Deploy CSV file return Response.ok().entity("File uploaded").build();
     * <p>
     * This function use FormDataParam annotation. WSO@2 MSF4J supports this annotation and multipart/form-data content type.
     * <p>
     * </p>
     * The FormDataParam annotation supports complex types and collections (such as List, Set and SortedSet),
     * with the multipart/form-data content type and supports files along with form field submissions.
     * It supports directly to get the file objects in databaseFeedSimulation file upload by using the @FormDataParam  annotation.
     * This annotation can be used with all FormParam supported data types plus file and bean types as well as InputStreams.
     * </p>
     *
     * @param fileInfo        FileInfo bean to hold the filename and the content type attributes of the particular InputStream
     * @param fileInputStream InputStream of the file
     * @return Response of completion of process
     * <p>
     * http://localhost:8080/EventSimulation/fileUpload
     */
    @POST
    @Path("/fileUpload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)

    public Response uploadFile(@FormDataParam("file") FileInfo fileInfo,
                               @FormDataParam("file") InputStream fileInputStream) {
        String jsonString;
        /*
        Get singleton instance of FileUploader
         */

        FileUploader fileUploader = FileUploader.getFileUploaderInstance();
        try {
            fileUploader.uploadFile(fileInfo, fileInputStream);
            jsonString = new Gson().toJson("File is uploaded");
        } catch (ValidationFailedException | EventSimulationException e) {
            throw new EventSimulationException("Failed file upload : " + e.getMessage());
        }
        return Response.ok().entity(jsonString).build();
    }

    /**
     * Delete the file
     * <p>
     * This function use FormDataParam annotation. WSO@2 MSF4J supports this annotation and multipart/form-data content type.
     * <p>
     *
     * @param fileName File Name
     * @return Response of completion of process
     * <p>
     * http://localhost:8080/EventSimulation/deleteFile
     */
    @POST
    @Path("/deleteFile")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response deleteFile(@FormDataParam("fileName") String fileName) {
        String jsonString;
        /*
         * Get singleton instance of FileUploader
         */
        FileUploader fileUploader = FileUploader.getFileUploaderInstance();
        try {
            fileUploader.deleteFile(fileName);
            jsonString = new Gson().toJson("File is deleted");
        } catch (EventSimulationException e) {
            throw new EventSimulationException("Failed file delete : " + e.getMessage());
        }
        return Response.ok().entity(jsonString).build();
    }


    /**
     * This method produces service for feed simulation
     * <p>
     * For an execution plan It may have one or more input streams.
     * this method provides the capability to simulate each input streams in different cases.
     * such as simulate using CSV File, simulate using Random Data and simulate using
     * database resource.
     * </p>
     *
     * @param feedSimulationConfigDetails jsonString to be converted to FeedSimulationDto object from the request Json body.
     * @return Response of completion of process
     * <p>
     * <pre>
     *     curl  -X POST -d '{"orderByTimeStamp" : "false","streamConfiguration"
     *     :[{"simulationType" : "RandomDataSimulation","streamName": "cseEventStream2",
     *     "events": "20","delay": "1000","attributeConfiguration":[{"type": "CUSTOMDATA",
     *     "list": "WSO2,IBM"},{"type": "REGEXBASED","pattern": "[+]?[0-9]*\\.?[0-9]+"},
     *     {"type": "PRIMITIVEBASED","min": "2","max": "200","length": "2",}]},
     *     {"simulationType" : "FileFeedSimulation","streamName" : "cseEventStream","fileName"   : "cseteststream.csv",
     *     "delimiter"  : ",","delay": "1000"}]}' http://localhost:8080/EventSimulation/feedSimulation
     * </pre>
     * <p>
     * http://localhost:8080/EventSimulation/feedSimulation
     */
    @POST
    @Path("/feedSimulation")
    public Response feedSimulation(String feedSimulationConfigDetails) {
        String jsonString;
        try {
            //parse json string to FeedSimulationDto object
            FeedSimulationDto feedSimulationConfig = EventSimulatorParser.feedSimulationParser(feedSimulationConfigDetails);
            //start feed simulation
            String uuid = UUID.randomUUID().toString();
            // TODO: 2/4/17 Cannot put if there's already a config, or does it really matters? since UUID??
            executorMap.put(uuid, EventSimulatorPoolExecutor.newEventSimulatorPool(feedSimulationConfig, feedSimulationConfig.getNoOfParallelSimulationSources()));
            jsonString = new Gson().toJson("Feed simulation starts successfully | uuid : " + uuid);
        } catch (EventSimulationException e) {
            throw new EventSimulationException(e.getMessage());
        }
        return Response.ok().entity(jsonString).build();
    }

    /**
     * Stop the simulation process
     *
     * @return Response of completion of process
     * <p>
     * http://localhost:8080/EventSimulation/feedSimulation/stop/{uuid}
     */
    @POST
    @Path("/feedSimulation/stop/{uuid}")
    public Response stop(@PathParam("uuid") String uuid) throws InterruptedException {
        String jsonString;
        //stop feed simulation
        try {
            // TODO: 2/4/17 Double check whether this really works as expected
            // TODO: check whether uuid exists in executorMap, and return response accordingly
            if (executorMap.containsKey(uuid)) {
                executorMap.get(uuid).stop();
                executorMap.remove(uuid);
                jsonString = new Gson().toJson("Feed simulation is stopped | uuid : " + uuid);
            } else {
                jsonString=new Gson().toJson("No feed simulation available under uuid : " + uuid);
            }
        } catch (EventSimulationException e) {
            throw new EventSimulationException(e.getMessage());
        }
        return Response.ok().entity(jsonString).build();
    }

    /**
     * pause the simulation process
     *
     * @return Response of completion of process
     * @throws InterruptedException Interrupted Exception
     *                              <p>
     *                              http://localhost:8080/EventSimulation/feedSimulation/pause/{uuid}
     */
    @POST
    @Path("/feedSimulation/pause/{uuid}")
    public Response pause(@PathParam("uuid") String uuid) throws InterruptedException {
        String jsonString;
        //pause feed simulation
        try {
            // TODO: 2/4/17 Double check whether this really works as expected
            // TODO: check whether uuid exists in executorMap, and return response accordingly
            if (executorMap.containsKey(uuid)) {
            executorMap.get(uuid).pause();
            jsonString = new Gson().toJson("Feed simulation is paused | uuid : " + uuid);
            } else {
                jsonString = new Gson().toJson("No feed simulation available under uuid : " + uuid);
            }
        } catch (EventSimulationException e) {
            throw new EventSimulationException(e.getMessage());
        }
        return Response.ok().entity(jsonString).build();
    }

    /**
     * resume the simulation
     *
     * @return Response of completion of process
     * @throws InterruptedException Interrupted Exception
     *                              <p>
     *                              http://localhost:8080/EventSimulation/feedSimulation/resume
     */
    @POST
    @Path("/feedSimulation/resume/{uuid}")
    public Response resume(@PathParam("uuid") String uuid) throws InterruptedException {
        String jsonString;
        //pause feed simulation
        try {
            // TODO: 2/4/17 Double check whether this really works as expected
            // TODO: check whether uuid exists in executorMap, and return response accordingly
            if (executorMap.containsKey(uuid)) {
                executorMap.get(uuid).resume();
                jsonString = new Gson().toJson("Feed simulation resumed | uuid : " + uuid);
            } else {
                jsonString = new Gson().toJson("No feed simulation available under uuid : " + uuid);
            }
        } catch (EventSimulationException e) {
            throw new EventSimulationException(e.getMessage());
        }
        return Response.ok().entity(jsonString).build();
    }
}
