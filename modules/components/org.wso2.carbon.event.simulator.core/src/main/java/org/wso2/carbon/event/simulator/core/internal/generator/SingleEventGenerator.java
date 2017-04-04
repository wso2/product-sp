package org.wso2.carbon.event.simulator.core.internal.generator;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.event.simulator.core.exception.EventGenerationException;
import org.wso2.carbon.event.simulator.core.exception.InsufficientAttributesException;
import org.wso2.carbon.event.simulator.core.exception.InvalidConfigException;
import org.wso2.carbon.event.simulator.core.internal.bean.SingleEventSimulationDTO;
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
            log.debug("Retrieve stream attribute definitions for stream '" +
                    singleEventConfig.getStreamName() + "' for single event simulation");
        }
        /**
         * check whether the number of attribute values specified is the number of stream attributes
         * if yes, proceed with sending single event
         * else, throw an exception
         * */
        if (singleEventConfig.getAttributeValues().length == streamAttributes.size()) {
            Event event;
            try {
                event = EventConverter.eventConverter(streamAttributes,
                        singleEventConfig.getAttributeValues(),
                        singleEventConfig.getTimestamp());
                EventSimulatorDataHolder.getInstance().getEventStreamService().pushEvent(
                        singleEventConfig.getExecutionPlanName(),
                        singleEventConfig.getStreamName(), event);
            } catch (EventGenerationException e) {
                log.error("Error occurred during single event simulation of stream '" +
                        singleEventConfig.getStreamName() + "' for configuration '" + singleEventConfig.toString() +
                        "'. " + e.getMessage(), e);
            }
        } else {
            throw new InsufficientAttributesException("Simulation of stream '" + singleEventConfig
                    .getStreamName() + "' requires " + streamAttributes.size() + " attribute(s). Single" +
                    "event configuration only contains values for " + singleEventConfig.getAttributeValues()
                    .length + " attribute(s)");
        }
    }

    /**
     * validateSingleEvent() is used to validate single event simulation provided and create a
     * SingleEventSimulationDTO object containing simulation configuration
     *
     * @param singleEventConfiguration SingleEventSimulationDTO containing single event simulation configuration
     * @return SingleEventSimulationDTO if required configuration is provided
     */
    private static SingleEventSimulationDTO validateSingleEvent(String singleEventConfiguration)
            throws InvalidConfigException {
        JSONObject singleEventConfig = new JSONObject(singleEventConfiguration);
        try {
            if (!checkAvailability(singleEventConfig, EventSimulatorConstants.STREAM_NAME)) {
                throw new InvalidConfigException("Stream name is required for single event simulation. Invalid " +
                        "configuration provided : " + singleEventConfig.toString());
            }
            if (!checkAvailability(singleEventConfig, EventSimulatorConstants.EXECUTION_PLAN_NAME)) {
                throw new InvalidConfigException("Execution plan name is required for single event simulation of " +
                        "stream '" + singleEventConfig.getString(EventSimulatorConstants.STREAM_NAME) + ". " +
                        "Invalid configuration provided : " + singleEventConfig.toString());
            }
            /*
             * if timestamp is set to null, take current system time as the timestamp of the event
             * */
            long timestamp;
            if (singleEventConfig.has(EventSimulatorConstants.SINGLE_EVENT_TIMESTAMP)) {
                if (singleEventConfig.isNull(EventSimulatorConstants.SINGLE_EVENT_TIMESTAMP)) {
                    timestamp = System.currentTimeMillis();
                } else if (singleEventConfig.getLong(EventSimulatorConstants.SINGLE_EVENT_TIMESTAMP) > 0) {
                    timestamp = singleEventConfig.getLong(EventSimulatorConstants.SINGLE_EVENT_TIMESTAMP);
                } else {
                    throw new InvalidConfigException("Timestamp must be a positive value for single event simulation " +
                            "of stream '" + singleEventConfig.getString(EventSimulatorConstants.STREAM_NAME) +
                            "'. Invalid configuration provided : " + singleEventConfig.toString());
                }
            } else {
                throw new InvalidConfigException("Timestamp value is required for single event simulation of stream '"
                        + singleEventConfig.getString(EventSimulatorConstants.STREAM_NAME) + "'. Invalid " +
                        "configuration provided : " + singleEventConfig.toString());
            }
            Object[] attributeValues;
            if (checkAvailabilityOfArray(singleEventConfig, EventSimulatorConstants.SINGLE_EVENT_DATA)) {
                attributeValues = (new Gson().fromJson(singleEventConfig.getJSONArray(EventSimulatorConstants
                        .SINGLE_EVENT_DATA).toString(), ArrayList.class)).toArray();
            } else {
                throw new InvalidConfigException("Single event simulation requires a attribute value for " +
                        "stream '" + singleEventConfig.getString(EventSimulatorConstants.STREAM_NAME) + "'. Invalid " +
                        "configuration provided : " + singleEventConfig.toString());
            }
            SingleEventSimulationDTO singleEventSimulationDTO = new SingleEventSimulationDTO();
            singleEventSimulationDTO.setStreamName(singleEventConfig.getString(EventSimulatorConstants.STREAM_NAME));
            singleEventSimulationDTO.setExecutionPlanName(singleEventConfig
                    .getString(EventSimulatorConstants.EXECUTION_PLAN_NAME));
            singleEventSimulationDTO.setTimestamp(timestamp);
            singleEventSimulationDTO.setAttributeValues(attributeValues);
            return singleEventSimulationDTO;
        } catch (JSONException e) {
            log.error("Error occurred when accessing stream configuration. ", e);
            throw new InvalidConfigException("Error occurred when accessing stream configuration. ", e);
        }
    }
}
