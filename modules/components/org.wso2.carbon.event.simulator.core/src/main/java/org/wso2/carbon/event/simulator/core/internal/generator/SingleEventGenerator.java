package org.wso2.carbon.event.simulator.core.internal.generator;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.event.simulator.core.exception.EventGenerationException;
import org.wso2.carbon.event.simulator.core.exception.InsufficientAttributesException;
import org.wso2.carbon.event.simulator.core.exception.InvalidConfigException;
import org.wso2.carbon.event.simulator.core.internal.bean.SingleEventSimulationDTO;
import org.wso2.carbon.event.simulator.core.internal.util.CommonOperations;
import org.wso2.carbon.event.simulator.core.internal.util.EventConverter;
import org.wso2.carbon.event.simulator.core.internal.util.EventSimulatorConstants;
import org.wso2.carbon.event.simulator.core.service.EventSimulatorDataHolder;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.query.api.definition.Attribute;

import static org.wso2.carbon.event.simulator.core.internal.util.CommonOperations.checkAvailability;
import static org.wso2.carbon.event.simulator.core.internal.util.CommonOperations.checkAvailabilityOfArray;

import java.util.ArrayList;
import java.util.List;


/**
 * SingleEventGenerator class is responsible for single event simulation
 */
public class SingleEventGenerator {
    private static final Logger log = LoggerFactory.getLogger(SingleEventGenerator.class);

    public SingleEventGenerator() {
    }

    /**
     * sendEvent() is used to send a single event based on the configuration provided by the SingleEventSimulationDTO
     * object
     *
     * @param singleEventConfiguration configuration of the single event
     * @throws InvalidConfigException          if the single even simulation configuration contains invalid entries
     * @throws InsufficientAttributesException if the number of attributes specified for the event is not equal to
     *                                         the number of stream attributes
     */
    public static void sendEvent(String singleEventConfiguration)
            throws InvalidConfigException, InsufficientAttributesException {
        SingleEventSimulationDTO singleEventConfig = validateSingleEvent(singleEventConfiguration);
        List<Attribute> streamAttributes = EventSimulatorDataHolder.getInstance().getEventStreamService()
                .getStreamAttributes(singleEventConfig.getExecutionPlanName(),
                        singleEventConfig.getStreamName());

        if (streamAttributes == null) {
            throw new EventGenerationException("Execution plan '" + singleEventConfig.getExecutionPlanName()
                    + "' has not been deployed");
        }

        if (log.isDebugEnabled()) {
            log.debug("Retrieve stream attribute definitions for stream '" + singleEventConfig.getStreamName() +
                    "' for single event simulation");
        }

            /*
            * check whether the number of columns specified is the number of stream attributes
            * if yes, proceed with sending single event
            * else, throw an exception
            * */
        if (CommonOperations.checkAttributes(singleEventConfig.getAttributeValues().length,
                streamAttributes.size())) {
            Event event = EventConverter.eventConverter(streamAttributes,
                    singleEventConfig.getAttributeValues(),
                    singleEventConfig.getTimestamp());
            EventSimulatorDataHolder.getInstance().getEventStreamService().pushEvent(
                    singleEventConfig.getExecutionPlanName(),
                    singleEventConfig.getStreamName(), event);
        } else {
            throw new InsufficientAttributesException("Stream '" + singleEventConfig.getStreamName() + "' has " +
                    streamAttributes.size() + " attributes. Single event configuration only contains values for " +
                    singleEventConfig.getAttributeValues().length + " attributes");
        }
    }

//    todo fix api simulation/single

    /**
     * validateSingleEvent() is used to parse the string SingleEventConfiguration into a SingleEventSimulationDTO
     * object and to validate it
     *
     * @param singleEventConfiguration string containing single event simulation configuration
     * @return SingleEventSimulationDTO object containing the single event simulation configuration
     */
    private static SingleEventSimulationDTO validateSingleEvent(String singleEventConfiguration)
            throws InvalidConfigException {

        SingleEventSimulationDTO singleEventSimulationDTO = new SingleEventSimulationDTO();
        JSONObject singleEventConfig = new JSONObject(singleEventConfiguration);

        /*
         * assign properties to SingleDto object
         * perform the following checks prior to assigning properties
         *
         * 1. has
         * 2. isNull
         * 3. isEmpty
         *
         * assign property if all 3 checks are successful
         * else, throw an exception
         * */
        try {
            if (checkAvailability(singleEventConfig, EventSimulatorConstants.STREAM_NAME)) {
                singleEventSimulationDTO.setStreamName(singleEventConfig
                        .getString(EventSimulatorConstants.STREAM_NAME));
            } else {
                throw new InvalidConfigException("Stream name is required for single event simulation");
            }

            if (checkAvailability(singleEventConfig, EventSimulatorConstants.EXECUTION_PLAN_NAME)) {
                singleEventSimulationDTO.setExecutionPlanName(singleEventConfig
                        .getString(EventSimulatorConstants.EXECUTION_PLAN_NAME));
            } else {
                throw new InvalidConfigException("Execution plan name is required for single event simulation");
            }

            if (checkAvailability(singleEventConfig, EventSimulatorConstants.SINGLE_EVENT_TIMESTAMP)) {
                singleEventSimulationDTO.setTimestamp(singleEventConfig.
                        getLong(EventSimulatorConstants.SINGLE_EVENT_TIMESTAMP));
            } else {
                throw new InvalidConfigException("Timestamp value is required for single event simulation");
            }

            if (checkAvailabilityOfArray(singleEventConfig, EventSimulatorConstants.SINGLE_EVENT_DATA)) {
                Object[] attributeValues = getEventData(singleEventConfig
                        .getJSONArray(EventSimulatorConstants.SINGLE_EVENT_DATA));
                singleEventSimulationDTO.setAttributeValues(attributeValues);

                if (log.isDebugEnabled()) {
                    log.debug("Set attribute values for single event simulation");
                }
            } else {
                throw new InvalidConfigException("Single event simulation requires a attribute value for " +
                        "stream '" + singleEventSimulationDTO.getStreamName() + "'.");
            }
        } catch (JSONException e) {
            log.error("Error occurred when accessing single event simulation configuration : ", e);
            throw new InvalidConfigException("Error occurred when accessing single event simulation configuration : ",
                    e);
        }
        return singleEventSimulationDTO;
    }

    /**
     * getEventData() converts the json array of event data into a string array
     *
     * @param eventData json array containing event data
     * @return string array of event data
     * @throws InvalidConfigException if event data json array contains null or empty string
     */
//    todo no need
    private static Object[] getEventData(JSONArray eventData) throws InvalidConfigException {
        /*
        * Since the event data may contain string that contain ',' we cannot use string.split(,).
        * hence, iterate the json array and check whether any of the json objects in the array are null or empty
        * if yes, throw an exception
        * else, return a string array containing event data
        * */
        Gson gson = new Gson();
        ArrayList dataValues = gson.fromJson(eventData.toString(), ArrayList.class);

        if (dataValues.contains(null)) {
            throw new InvalidConfigException("Event data list provided for single event simulation cannot" +
                    " contain null or empty values. Invalid attribute configuration provided : " +
                    eventData.toString());
        } else {
            return (dataValues.toArray(new String[dataValues.size()]));
        }
    }
}
