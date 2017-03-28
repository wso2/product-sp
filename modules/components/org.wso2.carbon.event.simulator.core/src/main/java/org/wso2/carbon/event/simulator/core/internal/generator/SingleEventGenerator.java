package org.wso2.carbon.event.simulator.core.internal.generator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.event.simulator.core.exception.EventGenerationException;
import org.wso2.carbon.event.simulator.core.exception.InsufficientAttributesException;
import org.wso2.carbon.event.simulator.core.exception.InvalidConfigException;
import org.wso2.carbon.event.simulator.core.internal.bean.SingleEventSimulationDTO;
import org.wso2.carbon.event.simulator.core.internal.util.EventConverter;
import org.wso2.carbon.event.simulator.core.service.EventSimulatorDataHolder;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.query.api.definition.Attribute;

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
    public static void sendEvent(SingleEventSimulationDTO singleEventConfiguration)
            throws InvalidConfigException, InsufficientAttributesException {
        if (validateSingleEvent(singleEventConfiguration)) {
            List<Attribute> streamAttributes = EventSimulatorDataHolder.getInstance().getEventStreamService()
                    .getStreamAttributes(singleEventConfiguration.getExecutionPlanName(),
                            singleEventConfiguration.getStreamName());

            if (streamAttributes == null) {
                throw new EventGenerationException("Execution plan '" + singleEventConfiguration.getExecutionPlanName()
                        + "' has not been deployed");
            }

            if (log.isDebugEnabled()) {
                log.debug("Retrieve stream attribute definitions for stream '" +
                        singleEventConfiguration.getStreamName() + "' for single event simulation");
            }

            /*
            * check whether the number of attribute values specified is the number of stream attributes
            * if yes, proceed with sending single event
            * else, throw an exception
            * */
            if (singleEventConfiguration.getAttributeValues().length == streamAttributes.size()) {
                Event event;
                try {
                     event = EventConverter.eventConverter(streamAttributes,
                            singleEventConfiguration.getAttributeValues(),
                            singleEventConfiguration.getTimestamp());
                } catch (EventGenerationException e) {
                    log.error(e.getMessage(), e);
                    throw new EventGenerationException("Error occurred during single event simulation of stream '" +
                            singleEventConfiguration.getStreamName() + "'. " + e.getMessage(), e);
                }
                EventSimulatorDataHolder.getInstance().getEventStreamService().pushEvent(
                        singleEventConfiguration.getExecutionPlanName(),
                        singleEventConfiguration.getStreamName(), event);
            } else {
                throw new InsufficientAttributesException("Simulation of stream '" + singleEventConfiguration
                        .getStreamName() + "' requires " + streamAttributes.size() + " attribute(s). Single" +
                        "event configuration only contains values for " + singleEventConfiguration.getAttributeValues()
                        .length + " attribute(s)");
            }
        }
    }

    /**
     * validateSingleEvent() is used to check whether the  SingleEventSimulationDTO contains all required data
     *
     * @param singleEventConfiguration SingleEventSimulationDTO containing single event simulation configuration
     * @return true if all required configuration is provided
     */
    private static boolean validateSingleEvent(SingleEventSimulationDTO singleEventConfiguration)
            throws InvalidConfigException {

        if (singleEventConfiguration.getStreamName() == null || singleEventConfiguration.getStreamName().isEmpty()) {
            throw new InvalidConfigException("Stream name is required for single event simulation. Invalid " +
                    "configuration provided for single event simulation : " + singleEventConfiguration.toString());
        }
        if (singleEventConfiguration.getExecutionPlanName() == null
                 || singleEventConfiguration.getExecutionPlanName().isEmpty()) {
            throw new InvalidConfigException("Execution plan name is required for single event simulation of " +
                    "stream '" + singleEventConfiguration.getStreamName() + "' Invalid configuration provided for " +
                    "single event simulation : " + singleEventConfiguration.toString());
        }
        if (singleEventConfiguration.getTimestamp() == -1) {
            throw new InvalidConfigException("Timestamp value is required for single event simulation of " +
                    "stream '" + singleEventConfiguration.getStreamName() + "'. Invalid configuration provided for " +
                    "single event simulation : " + singleEventConfiguration.toString());
        }
        if (singleEventConfiguration.getAttributeValues() == null
                || singleEventConfiguration.getAttributeValues().length == 0) {
            throw new InvalidConfigException("Attribute values are required for single event simulation of " +
                    "stream '" + singleEventConfiguration.getStreamName() + "'. Invalid " +
                    "configuration provided for single event simulation : " + singleEventConfiguration.toString());
        }
        return true;
    }
}
