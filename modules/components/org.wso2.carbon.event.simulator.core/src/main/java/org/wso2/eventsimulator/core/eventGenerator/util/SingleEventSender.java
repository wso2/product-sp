package org.wso2.eventsimulator.core.eventGenerator.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.eventsimulator.core.eventGenerator.bean.SingleEventDto;
import org.wso2.eventsimulator.core.eventGenerator.util.exceptions.EventGenerationException;
import org.wso2.eventsimulator.core.internal.EventSimulatorDataHolder;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.List;


/**
 * SingleEventSender class is responsible for single event simulation
 */
public class SingleEventSender {
    private static final Logger log = LoggerFactory.getLogger(SingleEventSender.class);
    private SingleEventDto singleEventDto;
    private List<Attribute> streamAttributes;

    public SingleEventSender() {
    }

    public void sendEvent(String singleEventConfiguration) {

        try {
            singleEventDto = StreamConfigurationParser.singleEventSimulatorParser(singleEventConfiguration);

            streamAttributes = EventSimulatorDataHolder.getInstance().getEventStreamService()
                    .getStreamAttributes(singleEventDto.getExecutionPlanName(), singleEventDto.getStreamName());

            if (validateAttributeData(singleEventDto.getAttributeValues())) {
                Event event = EventConverter.eventConverter(streamAttributes, singleEventDto.getAttributeValues(),
                        singleEventDto.getTimestamp());
                EventSimulatorDataHolder.getInstance().getEventStreamService()
                        .pushEvent(singleEventDto.getExecutionPlanName(), singleEventDto.getStreamName(), event);
            } else {
                throw new EventGenerationException("Stream '" + singleEventDto.getStreamName() + "' has " +
                        streamAttributes.size() + " attributes. Single event configuration only contains values for " +
                        singleEventDto.getAttributeValues().length + " attributes");
            }
        } catch (EventGenerationException e) {
            log.error("Error occurred when generating an event : " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error occurred during single event simulation : " + e.getMessage(), e);

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

        if (attributeValues.length == streamAttributes.size()) {
            return true;
        } else {
            return false;
        }

    }
}
