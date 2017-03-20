package org.wso2.eventsimulator.core.eventGenerator;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.eventsimulator.core.eventGenerator.bean.SingleEventSimulationDto;
import org.wso2.eventsimulator.core.eventGenerator.util.EventConverter;
import org.wso2.eventsimulator.core.eventGenerator.util.exceptions.EventGenerationException;
import org.wso2.eventsimulator.core.eventGenerator.util.exceptions.InsufficientAttributesException;
import org.wso2.eventsimulator.core.internal.EventSimulatorDataHolder;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.List;


/**
 * SingleEventSender class is responsible for single event simulation
 */
public class SingleEventSender {
    private static final Logger log = LoggerFactory.getLogger(SingleEventSender.class);
    private SingleEventSimulationDto singleEventConfig;
    private List<Attribute> streamAttributes;

    public SingleEventSender() {
    }

    /**
     * sendEvent() is used to send a single event based on the configuration provided by the SingleEventSimulationDto
     * object
     *
     * @param singleEventConfiguration configuration of the single event
     * @throws InsufficientAttributesException if the number of attributes specified for the event is not equal to
     * the number of stream attributes
     * */
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

        if (singleEventConfig.getAttributeValues().length == streamAttributes.size()) {
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


    /**
     * validateAttributeData() checks whether the number attribute values in single event configuration is equal to
     * the number of attributes in the stream being simulated
     *
     * @param attributeValues a string array of attribute values
     * @return true if number of values is equal, else false
     */
    private Boolean validateAttributeData(String[] attributeValues) {

        return attributeValues.length == streamAttributes.size();
    }
}
