package org.wso2.eventsimulator.core;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.eventsimulator.core.simulator.bean.FeedSimulationDto;
import org.wso2.eventsimulator.core.simulator.bean.FeedSimulationStreamConfiguration;
import org.wso2.eventsimulator.core.simulator.exception.EventSimulationException;
import org.wso2.eventsimulator.core.simulator.singleventsimulator.SingleEventDto;
import org.wso2.eventsimulator.core.util.EventSimulatorParser;
import org.wso2.eventsimulator.core.util.EventSimulatorPoolExecutor;
import scala.util.parsing.json.JSON;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ruwini on 2/18/17.
 */
public class EventSimulatorStart {
    private static final Logger log = LoggerFactory.getLogger(EventSimulatorStart.class);
//    private static EventSimulatorStart instance = new EventSimulatorStart();

    Map<String,EventSimulatorPoolExecutor> executorMap;


    public void EventSimulatorStart() {
        executorMap = new ConcurrentHashMap<String, EventSimulatorPoolExecutor>();
    }

    public void EventSimulatorStart(String config) {

        try {
            JSONObject jsonObject = new JSONObject(config);
            log.info(jsonObject.getString("Type"));
            log.info(jsonObject.getString("Config"));

            switch (jsonObject.getString("Type")) {
                case ("single") : {
                    log.info("single simulation");
                    singleEventSimulation(jsonObject.getString("Config"));
                    break;
                }
                case ("csv"): {
                    break;
                }
                case ("feed") : {
                    feedsimulation(jsonObject.getString("Config"));
                    break;
                }default: {
                    log.info("no type match");
                }
            }
        } catch (Exception e) {
            log.error("EventSimulatorStar Exception : " + e.getMessage());
        }



    }


//    public static EventSimulatorStart getInstance() {return instance;}

    private void singleEventSimulation (String simulationString) {
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

                jsonString = "Event is send successfully";
            } catch (EventSimulationException e) {
                throw new EventSimulationException("Single Event simulation failed : " + e.getMessage());
            }
            log.info(jsonString);

    }

    private void feedsimulation (String simulationConfig) {
        String jsonString;
            try {
                //parse json string to FeedSimulationDto object
                FeedSimulationDto feedSimulationConfig = EventSimulatorParser.feedSimulationParser(simulationConfig);
                //start feed simulation
                String uuid = UUID.randomUUID().toString();
                // TODO: 2/4/17 Cannot put if there's already a config, or does it really matters? since UUID??
                executorMap.put(uuid, EventSimulatorPoolExecutor.newEventSimulatorPool(feedSimulationConfig, feedSimulationConfig.getNoOfParallelSimulationSources()));
                jsonString = "Feed simulation starts successfully | uuid : " + uuid;
            } catch (EventSimulationException e) {
                throw new EventSimulationException(e.getMessage());
            }
            log.info(jsonString);

    }

    private void stop(UUID uuid) {
        String jsonString;
            //stop feed simulation
            try {
                // TODO: 2/4/17 Double check whether this really works as expected
                // TODO: check whether uuid exists in executorMap, and return response accordingly
                if (executorMap.containsKey(uuid)) {
                    executorMap.get(uuid).stop();
                    executorMap.remove(uuid);
                    jsonString ="Feed simulation is stopped | uuid : " + uuid;
                } else {
                    jsonString="No feed simulation available under uuid : " + uuid;
                }
            } catch (EventSimulationException e) {
                throw new EventSimulationException(e.getMessage());
            }
            log.info(jsonString);
    }

    private void pause (UUID uuid) {
        String jsonString;
            //pause feed simulation
            try {
                // TODO: 2/4/17 Double check whether this really works as expected
                // TODO: check whether uuid exists in executorMap, and return response accordingly
                if (executorMap.containsKey(uuid)) {
                    executorMap.get(uuid).pause();
                    jsonString = "Feed simulation is paused | uuid : " + uuid;
                } else {
                    jsonString ="No feed simulation available under uuid : " + uuid;
                }
            } catch (EventSimulationException e) {
                throw new EventSimulationException(e.getMessage());
            }
            log.info(jsonString);

    }

    private void resume (UUID uuid) {
        String jsonString;
            //pause feed simulation
            try {
                // TODO: 2/4/17 Double check whether this really works as expected
                // TODO: check whether uuid exists in executorMap, and return response accordingly
                if (executorMap.containsKey(uuid)) {
                    executorMap.get(uuid).resume();
                    jsonString = "Feed simulation resumed | uuid : " + uuid;
                } else {
                    jsonString = "No feed simulation available under uuid : " + uuid;
                }
            } catch (EventSimulationException e) {
                throw new EventSimulationException(e.getMessage());
            }
            log.info(jsonString);
    }

//
//
//
//    @Path("/EventSimulation")
//    public class EventSimulatorRestService {
//        private static final Logger log = Logger.getLogger(EventSimulatorRestService.class);
//
//        /**
//         * Event simulator service executor for event simulator REST service.
//         */
//        private Map<String, EventSimulatorPoolExecutor> executorMap;
//
//        /**
//         * Initializes the service classes for resources.
//         */
//        public EventSimulatorRestService() {
//            executorMap = new ConcurrentHashMap<>();
//        }
//        public Response singleEventSimulation(String simulationString) {
//            if (log.isDebugEnabled()) {
//                log.debug("Single Event Simulation");
//            }
//            String jsonString;
//
//            try {
//                //parse json string to SingleEventDto object
//                SingleEventDto singleEventSimulationConfiguration = EventSimulatorParser.singleEventSimulatorParser(simulationString);
//                FeedSimulationDto feedSimulationDto = new FeedSimulationDto();
//                feedSimulationDto.setStreamConfigurationList(new ArrayList<FeedSimulationStreamConfiguration>() {{
//                    add(singleEventSimulationConfiguration);
//                }});
//
//                //start single event simulation
//                // TODO: 2/4/17 is there a better way????
//                EventSimulatorPoolExecutor.newEventSimulatorPool(feedSimulationDto, 1);
//
//                jsonString = new Gson().toJson("Event is send successfully");
//            } catch (EventSimulationException e) {
//                throw new EventSimulationException("Single Event simulation failed : " + e.getMessage());
//            }
//            return Response.ok().entity(jsonString).build();
//        }

//        public Response uploadFile(@FormDataParam("file") FileInfo fileInfo,
//                                   @FormDataParam("file") InputStream fileInputStream) {
//            String jsonString;
//        /*
//        Get singleton instance of FileUploader
//         */
//
//            FileUploader fileUploader = FileUploader.getFileUploaderInstance();
//            try {
//                fileUploader.uploadFile(fileInfo, fileInputStream);
//                jsonString = new Gson().toJson("File is uploaded");
//            } catch (ValidationFailedException | EventSimulationException e) {
//                throw new EventSimulationException("Failed file upload : " + e.getMessage());
//            }
//            return Response.ok().entity(jsonString).build();
//        }
//
//        /**
//         * Delete the file
//         * <p>
//         * This function use FormDataParam annotation. WSO@2 MSF4J supports this annotation and multipart/form-data content type.
//         * <p>
//         *
//         * @param fileName File Name
//         * @return Response of completion of process
//         * <p>
//         * http://localhost:8080/EventSimulation/deleteFile
//         */
//        @POST
//        @Path("/deleteFile")
//        @Consumes(MediaType.MULTIPART_FORM_DATA)
//        public Response deleteFile(@FormDataParam("fileName") String fileName) {
//            String jsonString;
//        /*
//         * Get singleton instance of FileUploader
//         */
//            FileUploader fileUploader = FileUploader.getFileUploaderInstance();
//            try {
//                fileUploader.deleteFile(fileName);
//                jsonString = new Gson().toJson("File is deleted");
//            } catch (EventSimulationException e) {
//                throw new EventSimulationException("Failed file delete : " + e.getMessage());
//            }
//            return Response.ok().entity(jsonString).build();
//        }

}
