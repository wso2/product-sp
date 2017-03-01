package org.wso2.streamprocessor.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.siddhi.core.event.Event;

/**
 * Created by ruwini on 2/14/17.
 */
public class EventReceiverServiceImpl implements EventReceiverService {
    private static Logger logger = LoggerFactory.getLogger(EventReceiverService.class);


    @Override
    public void eventReceiverService(String executionPlanName, String streamName, Event event) {
        logger.info(executionPlanName);
        logger.info(streamName);
        logger.info(event.toString());
    }
}
