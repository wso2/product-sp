package org.wso2.streamprocessor.core;

import org.wso2.siddhi.core.event.Event;

/**
 * Created by ruwini on 2/14/17.
 */
public interface EventReceiverService {

    void eventReceiverService(String executionPlanName, String streamName, Event event);
}
