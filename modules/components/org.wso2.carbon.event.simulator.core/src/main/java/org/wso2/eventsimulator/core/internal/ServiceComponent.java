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

package org.wso2.eventsimulator.core.internal;


import com.google.gson.Gson;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.eventsimulator.core.simulator.bean.FeedSimulationDto;
import org.wso2.eventsimulator.core.simulator.bean.FeedSimulationStreamConfiguration;
import org.wso2.eventsimulator.core.simulator.csvFeedSimulation.core.FileUploader;
import org.wso2.eventsimulator.core.simulator.exception.EventSimulationException;
import org.wso2.eventsimulator.core.simulator.exception.ValidationFailedException;
import org.wso2.eventsimulator.core.simulator.singleventsimulator.SingleEventDto;
import org.wso2.eventsimulator.core.util.EventSimulatorParser;
import org.wso2.eventsimulator.core.util.EventSimulatorPoolExecutor;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.streamprocessor.core.EventStreamService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


@Component(
        name = "event-simulator-core-services",
        service = Microservice.class,
        immediate = true
)
@Path("/eventSimulation")
public class ServiceComponent implements Microservice {
    public static final Logger log = LoggerFactory.getLogger(ServiceComponent.class);


    /**
     * Event simulator service executor for event simulator REST service.
     */
    private static Map<String, EventSimulatorPoolExecutor> executorMap = new ConcurrentHashMap<>();

    /**
     * Send single event for simulation
     *
     * @param simulationString jsonString to be converted to SingleEventDto object from the request Json body.
     *                         <p>
     *                         http://localhost:9090/eventSimulation/singleEventSimulation
     *                         <pre>
     *                         curl -X POST -d'{"streamName":"stream2","executionPlanName" : "planSingle","attributeValues":["WSO2","345","45"]}' http://localhost:9090/eventSimulation/singleEventSimulation
     *                        </pre>
     *                         <p>
     *                         Eg :simulationString: {
     *                         "streamName":"cseEventStream",
     *                         "executionPlanName" : "planSingle",
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
     * http://localhost:9090/eventSimulation/fileUpload
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
     * http://localhost:9090/eventSimulation/deleteFile
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
     *     curl -X POST -d '{"orderByTimeStamp" : "false","streamConfiguration":[{"simulationType" : "RANDOM_DATA_SIMULATION",
     *     "streamName":"stream4","executionPlanName" : "planCustom","events": "5","delay": "1000","attributeConfiguration":
     *     [{"type":"CUSTOMDATA","list": "WSO2,AAA,DDD,IBM"}]}]}' ​ http://localhost:9090/eventSimulation/feedSimulation
     * </pre>
     * <pre>
     *     curl -X POST -d '{"orderByTimeStamp" : "false","streamConfiguration":[{"simulationType" : "RANDOM_DATA_SIMULATION",
     *     "streamName":"stream5","executionPlanName" : "planPrimitive","events": "5","delay": "1000","attributeConfiguration":
     *     [{"type": "PRIMITIVEBASED","min": "2","max": "200","length":"2"}]}]}' ​ http://localhost:9090/eventSimulation/feedSimulation
     * </pre>
     * <pre>
     *     curl -X POST -d '{"orderByTimeStamp" : "false","streamConfiguration":[{"simulationType" : "RANDOM_DATA_SIMULATION",
     *     "streamName":"stream5","executionPlanName" : "planRegx","events": "5","delay": "1000","attributeConfiguration":[
     *     {"type": "REGEXBASED","pattern":"[+]?[0-9]*\\.?[0-9]+"}]}]}' ​ http://localhost:9090/eventSimulation/feedSimulation
     * </pre>
     * <p>
     * http://localhost:9090/eventSimulation/feedSimulation
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
            executorMap.put(uuid, EventSimulatorPoolExecutor.newEventSimulatorPool(feedSimulationConfig, feedSimulationConfig.getStreamConfigurationList().size()));
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
     * http://localhost:9090/eventSimulation/feedSimulation/stop/{uuid}
     */
    @POST
    @Path("/feedSimulation/stop/{uuid}")
    public Response stop(@PathParam("uuid") String uuid) throws InterruptedException {
        String jsonString;
        //stop feed simulation
        try {
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
     *                              http://localhost:9090/eventSimulation/feedSimulation/pause/{uuid}
     */
    @POST
    @Path("/feedSimulation/pause/{uuid}")
    public Response pause(@PathParam("uuid") String uuid) throws InterruptedException {
        String jsonString;
        //pause feed simulation
        try {
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
     *                              http://localhost:9090/eventSimulation/feedSimulation/resume
     */
    @POST
    @Path("/feedSimulation/resume/{uuid}")
    public Response resume(@PathParam("uuid") String uuid) throws InterruptedException {
        String jsonString;
        //pause feed simulation
        try {
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


    /**
     * This is the activation method of ServiceComponent. This will be called when it's references are fulfilled
     *
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start() throws Exception {
        log.info("Event Simulator service component is activated");
    }

    /**
     * This is the deactivation method of ServiceComponent. This will be called when this component
     * is being stopped or references are satisfied during runtime.
     *
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() throws Exception {
        log.info("Simulator service component is deactivated");
    }

    /**
     * This bind method will be called when EventStreamService method of stream processor is called
     */
    @Reference(
            name = "event.stream.service",
            service = EventStreamService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "stopEventStreamService"
    )
    protected void eventStreamService(EventStreamService eventStreamService) {
        log.info("@Reference(bind) EventStreamService");
        EventSimulatorDataHolder.getInstance().setEventStreamService(eventStreamService);
    }

    /**
     * This is the unbind method which gets called at the un-registration of eventStream OSGi service.
     */
    protected void stopEventStreamService(EventStreamService eventStreamService) {
        log.info("@Reference(unbind) EventStreamService");
        EventSimulatorDataHolder.getInstance().setEventStreamService(null);
    }
}