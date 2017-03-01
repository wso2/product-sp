package org.wso2.eventsimulator.core.internal;

/**
 * Created by ruwini on 2/13/17.
 */

import org.wso2.carbon.kernel.CarbonRuntime;
import org.wso2.streamprocessor.core.EventReceiverService;
import org.wso2.streamprocessor.core.StreamDefinitionService;

/**
 * StreamProcessorDataHolder to hold org.wso2.carbon.kernel.CarbonRuntime instance referenced through
 * org.wso2.carbon.helloworld.internal.ServiceComponent.
 *
 * @since 1.0.0
 */

public class EventSimulatorDataHolder {
    private static EventSimulatorDataHolder instance =  new EventSimulatorDataHolder();
    private CarbonRuntime carbonRuntime;
    private EventReceiverService eventReceiverService;
    private StreamDefinitionService streamDefinitionService;

    private EventSimulatorDataHolder(){}

    /**
     * This returns the EventSimulatorDataHolder instance.
     *
     * @return The EventSimulatorDataHolder instance of this singleton class
     */

    public static EventSimulatorDataHolder getInstance() {
        return instance;
    }

    /**
     * Returns the CarbonRuntime service which gets set through a service component.
     *
     * @return CarbonRuntime Service
     */

    public CarbonRuntime getCarbonRuntime() {
        return carbonRuntime;
    }

    /**
     * This method is for setting the CarbonRuntime service. This method is used by
     * ServiceComponent.
     *
     * @param carbonRuntime The reference being passed through ServiceComponent
     */

    public void setCarbonRuntime(CarbonRuntime carbonRuntime) {
        this.carbonRuntime = carbonRuntime;}

    public EventReceiverService getEventReceiverService() {
        return eventReceiverService;}

    public void setEventReceiverService(EventReceiverService eventReceiverService) {
        this.eventReceiverService = eventReceiverService;
    }

    public StreamDefinitionService getStreamDefinitionService() {
        return streamDefinitionService;
    }

    public void setStreamDefinitionService(StreamDefinitionService streamDefinitionService) {
        this.streamDefinitionService = streamDefinitionService;
    }
}
