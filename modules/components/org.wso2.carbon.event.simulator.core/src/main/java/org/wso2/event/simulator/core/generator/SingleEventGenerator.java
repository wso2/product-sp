package org.wso2.event.simulator.core.generator;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.event.simulator.core.bean.SingleEventSimulationDto;
import org.wso2.event.simulator.core.exception.EventGenerationException;
import org.wso2.event.simulator.core.exception.InsufficientAttributesException;
import org.wso2.event.simulator.core.service.EventSimulatorDataHolder;
import org.wso2.event.simulator.core.util.CommonOperations;
import org.wso2.event.simulator.core.util.EventConverter;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.List;


/**
 * SingleEventGenerator class is responsible for single event simulation
 */
public class SingleEventGenerator {
    private static final Logger log = LoggerFactory.getLogger(SingleEventGenerator.class);
    private SingleEventSimulationDto singleEventConfig;
    private List<Attribute> streamAttributes;

    public SingleEventGenerator() {
    }

    /**
     * sendEvent() is used to send a single event based on the configuration provided by the SingleEventSimulationDto
     * object
     *
     * @param singleEventConfiguration configuration of the single event
     * @throws InsufficientAttributesException if the number of attributes specified for the event is not equal to
     *                                         the number of stream attributes
     */
    public void sendEvent(SingleEventSimulationDto singleEventConfiguration) throws InsufficientAttributesException {

        singleEventConfig = singleEventConfiguration;
        streamAttributes = EventSimulatorDataHolder.getInstance().getEventStreamService()
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
}
