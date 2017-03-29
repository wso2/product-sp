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

package org.wso2.carbon.event.simulator.core.service;

import com.google.gson.Gson;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.event.simulator.core.exception.FileAlreadyExistsException;
import org.wso2.carbon.event.simulator.core.exception.FileOperationsException;
import org.wso2.carbon.event.simulator.core.exception.InsufficientAttributesException;
import org.wso2.carbon.event.simulator.core.exception.InvalidConfigException;
import org.wso2.carbon.event.simulator.core.exception.ValidationFailedException;
import org.wso2.carbon.event.simulator.core.internal.bean.SingleEventSimulationDTO;
import org.wso2.carbon.event.simulator.core.internal.generator.SingleEventGenerator;
import org.wso2.carbon.event.simulator.core.internal.generator.csv.util.FileUploader;
import org.wso2.carbon.stream.processor.core.EventStreamService;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.formparam.FormDataParam;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;



/**
 * Service component implements Microservices and provides services used for event simulation
 */
@Component(
        name = "event-simulator-core-services",
        service = Microservice.class,
        immediate = true
)
@Path("/simulation")
public class ServiceComponent implements Microservice {
    public static final Map<String, EventSimulator> SIMULATOR_MAP = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(ServiceComponent.class);
    private static final ExecutorService executorServices = Executors.newFixedThreadPool(10);
    private Gson gson = new Gson();

    /**
     * Send single event for simulation
     * <p>
     * http://localhost:9090/eventSimulation/singleEventSimulation
     * <pre>
     * curl -X POST -d'{"streamName":"FooStream",
     *                 "executionPlanName" : "TestExecutionPlan",
     *                 "timestamp" : "1488615136958"
     *                 "attributeValues":["WSO2","345", "45"]}'
     *  http://localhost:9090/eventSimulation/singleEventSimulation
     * </pre>
     * <p>
     * Eg :simulationString: {
     * "streamName":"cseEventStream",
     * "executionPlanName" : "planSingle",
     * "attributeValues":attributeValue
     * };
     *
     * @param singleEventConfiguration jsonString to be converted to SingleEventSimulationDTO object.
     * @return response
     * @throws InvalidConfigException          if the simulation configuration contains invalid data
     * @throws InsufficientAttributesException if the number of attributes specified for the event is not equal to
     *                                         the number of stream attributes
     */
//    @POST
//    @Path("/singleEventSimulation")
//    public Response singleEventSimulation(String singleEventConfiguration)
//            throws InvalidConfigException, InsufficientAttributesException {
//        if (log.isDebugEnabled()) {
//            log.debug("Single Event Simulation");
//        }
//        String jsonString;
//        SingleEventGenerator.sendEvent(singleEventConfiguration);
//        jsonString = gson.toJson("Single Event simulation completed successfully");
//
//        return Response.ok().entity(jsonString).build();
//    }


    @POST
    @Path("/single")
    @Consumes("application/json")
    public Response singleEvent(SingleEventSimulationDTO singleEventConfiguration)
            throws InvalidConfigException, InsufficientAttributesException  {
        if (log.isDebugEnabled()) {
            log.debug("Single Event Simulation started successfully");
        }
        SingleEventGenerator.sendEvent(singleEventConfiguration);
        return Response.ok().entity("success").build();
    }


    /**
     * This method produces service for feed simulation
     * <p>
     * http://localhost:9090/eventSimulation/feedSimulation
     *
     * @param simulationConfigDetails jsonString to be converted to EventSimulationDto object from the request
     *                                Json body.
     * @return Response
     * @throws InvalidConfigException          if the simulation configuration contains invalid data
     * @throws ValidationFailedException       if the regex has incorrect syntax
     * @throws InsufficientAttributesException if the number of attributes specified for the event is not equal to
     *                                         the number of stream attributes
     */
    @POST
    @Path("/feed")
    public Response feedSimulation(String simulationConfigDetails)
            throws InvalidConfigException, ValidationFailedException, InsufficientAttributesException {
        EventSimulator simulator = new EventSimulator(simulationConfigDetails);
        SIMULATOR_MAP.put(simulator.getUuid(), simulator);
        executorServices.execute(simulator);
        String jsonString = gson.toJson("Event simulation submitted successfully | uuid : " + simulator.getUuid
                ());
//        todo response structure

        return Response.ok().entity(jsonString).build();
    }

  /*  @POST
    @Path("/feed")
    @Consumes("application/json")
    public Response feed(SimulationConfigurationDTO simulationConfigDetails)
            throws InvalidConfigException, ValidationFailedException, InsufficientAttributesException {
        String jsonString = gson.toJson("Event simulation submitted successfully");
//        todo response structure

        return Response.ok().entity(jsonString).build();
    }*/

    /**
     * Stop the simulation process of simulation configuration related to the provided UUID
     * <p>
     * http://localhost:9090/eventSimulation/feedSimulation/stop/{uuid}
     *
     * @param uuid uuid of simulation that needs to be stopped
     * @return Response
     * @throws InterruptedException Interrupted Exception
     */
    @POST
    @Path("/feedSimulation/stop/{uuid}")
    public Response stop(@PathParam("uuid") String uuid) throws InterruptedException {
        String jsonString;
        //stop event simulation
        if (SIMULATOR_MAP.containsKey(uuid)) {
            SIMULATOR_MAP.get(uuid).stop();
            SIMULATOR_MAP.remove(uuid);
            jsonString = gson.toJson("Event simulation is stopped | uuid : " + uuid);
        } else {
            jsonString = gson.toJson("No event simulation available under uuid : " + uuid);
        }
        return Response.ok().entity(jsonString).build();
    }

    /**
     * pause the simulation process of simulation configuration related to the provided UUID
     * <p>
     * http://localhost:9090/eventSimulation/feedSimulation/pause/{uuid}
     *
     * @param uuid uuid of simulation that needs to be paused
     * @return Response
     * @throws InterruptedException Interrupted Exception
     */
    @POST
    @Path("/feedSimulation/pause/{uuid}")
    public Response pause(@PathParam("uuid") String uuid) throws InterruptedException {
        String jsonString;
        //pause event simulation
        if (SIMULATOR_MAP.containsKey(uuid)) {
            SIMULATOR_MAP.get(uuid).pause();
            jsonString = gson.toJson("Event simulation is paused | uuid : " + uuid);
        } else {
            jsonString = gson.toJson("No event simulation available under uuid : " + uuid);
        }
        return Response.ok().entity(jsonString).build();
    }

    /**
     * resume the simulation of simulation configuration related to the provided UUID
     * <p>
     * http://localhost:9090/eventSimulation/feedSimulation/resume
     *
     * @param uuid uuid of simulation that needs to be resumed
     * @return Response
     * @throws InterruptedException Interrupted Exception
     */
//    todo uuid/resume
    @POST
    @Path("/feedSimulation/resume/{uuid}")
    public Response resume(@PathParam("uuid") String uuid) throws InterruptedException {
        String jsonString;
        //pause event simulation
        if (SIMULATOR_MAP.containsKey(uuid)) {
            SIMULATOR_MAP.get(uuid).resume();
            jsonString = gson.toJson("Event simulation resumed | uuid : " + uuid);
        } else {
            jsonString = gson.toJson("No event simulation available under uuid : " + uuid);
        }
        return Response.ok().entity(jsonString).build();
    }

    /**
     * service to upload csv files
     * <p>
     * http://localhost:9090/eventSimulation/fileUpload
     * This function use FormDataParam annotation. WSO@2 MSF4J supports this annotation and multipart/form-data
     * content type.
     *
     * @param fileInfo        FileInfo bean to hold the filename and the content type attributes of the particular
     *                        InputStream
     * @param fileInputStream InputStream of the file
     * @return Response
     * @throws ValidationFailedException  throw exception if csv file validation failure
     * @throws FileAlreadyExistsException if the file exists in 'temp/eventSimulator' directory
     * @throws FileOperationsException    if an IOException occurs while copying uploaded stream to
     *                                    'temp/eventSimulator' directory
     */
    @POST
    @Path("/fileUpload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)

    public Response uploadFile(@FormDataParam("file") FileInfo fileInfo,
                               @FormDataParam("file") InputStream fileInputStream)
            throws FileAlreadyExistsException, ValidationFailedException, FileOperationsException {
        String jsonString;
        /*
        Get singleton instance of FileUploader
         */
        FileUploader fileUploader = FileUploader.getFileUploaderInstance();
        fileUploader.uploadFile(fileInfo, fileInputStream);
        jsonString = gson.toJson("Successfully uploaded file '" + fileInfo.getFileName() + "'");
        return Response.ok().entity(jsonString).build();
    }


    /**
     * Delete the file
     * <p>
     * http://localhost:9090/eventSimulation/deleteFile
     * <p>
     * This function use FormDataParam annotation. WSO@2 MSF4J supports this annotation and multipart/form-data
     * content type.
     * <p>
     *
     * @param fileName File Name
     * @return Response
     * @throws FileOperationsException if an IOException occurs while deleting file
     */
    @POST
    @Path("/deleteFile")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response deleteFile(@FormDataParam("fileName") String fileName) throws FileOperationsException {
        String jsonString;
        /*
         * Get singleton instance of FileUploader
         */
        FileUploader fileUploader = FileUploader.getFileUploaderInstance();
        fileUploader.deleteFile(fileName);
        jsonString = gson.toJson("Successfully deleted file '" + fileName + "'");
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
        EventSimulatorDataHolder.getInstance().setEventStreamService(eventStreamService);
        if (log.isDebugEnabled()) {
            log.info("@Reference(bind) EventStreamService");
        }

    }

    /**
     * This is the unbind method which gets called at the un-registration of eventStream OSGi service.
     */
    protected void stopEventStreamService(EventStreamService eventStreamService) {
        EventSimulatorDataHolder.getInstance().setEventStreamService(null);

        if (log.isDebugEnabled()) {
            log.info("@Reference(unbind) EventStreamService");
        }

    }
}
