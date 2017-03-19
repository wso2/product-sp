package org.wso2.eventsimulator.core.eventGenerator.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.eventsimulator.core.eventGenerator.bean.SingleEventSimulationDto;
import org.wso2.eventsimulator.core.eventGenerator.util.exceptions.ConfigurationParserException;
import org.wso2.eventsimulator.core.eventGenerator.util.exceptions.EventGenerationException;
import org.wso2.eventsimulator.core.eventGenerator.util.exceptions.ValidationFailedException;
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

    public void sendEvent(SingleEventSimulationDto singleEventConfiguration) {

        try {
            singleEventConfig = singleEventConfiguration;
            streamAttributes = EventSimulatorDataHolder.getInstance().getEventStreamService()
                    .getStreamAttributes(singleEventConfig.getExecutionPlanName(),
                            singleEventConfig.getStreamName());

            if (streamAttributes == null) {
                throw new EventGenerationException("Execution plan '" + singleEventConfig.getExecutionPlanName()
                        + "' has not been deployed");
            }

            if (validateAttributeData(singleEventConfig.getAttributeValues())) {
                Event event = EventConverter.eventConverter(streamAttributes,
                        singleEventConfig.getAttributeValues(),
                        singleEventConfig.getTimestamp());
                EventSimulatorDataHolder.getInstance().getEventStreamService().pushEvent(
                        singleEventConfig.getExecutionPlanName(),
                        singleEventConfig.getStreamName(), event);
            }
        } catch (EventGenerationException e) {
            log.error("Error occurred when generating an event : ", e);
        } catch (ConfigurationParserException e) {
            log.error("Error occurred when parsing single event simulation configuration : ", e);
        } catch (ValidationFailedException e) {
            log.error("Error occurred when validating Single event simulation attribute data : ", e);
        }
    }


    /**
     * validateAttributeData() checks whether the number attribute values in single event configuration is equal to
     * the number of attributes in the stream being simulated
     *
     * @param attributeValues a string array of attribute values
     * @return true if number of values is equal, else false
     */
    private Boolean validateAttributeData(String[] attributeValues) throws ValidationFailedException {

        if (attributeValues.length == streamAttributes.size()) {
            return true;
        } else {
            throw new ValidationFailedException("Stream '" + singleEventConfig.getStreamName() + "' has " +
                    streamAttributes.size() + " attributes. Single event configuration only contains values for " +
                    singleEventConfig.getAttributeValues().length + " attributes");
        }

    }
}
