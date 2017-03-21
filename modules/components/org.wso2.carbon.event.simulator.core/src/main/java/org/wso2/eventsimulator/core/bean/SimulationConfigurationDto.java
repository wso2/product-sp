package org.wso2.eventsimulator.core.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * SimulationConfigurationDto class is used to create simulation configuration objects.
 */
public class SimulationConfigurationDto {

    private Long delay;
    private Long timestampStartTime;
    private Long timestampEndTime;
    private List<StreamConfigurationDto> streamConfigurations = new ArrayList<StreamConfigurationDto>();


    public Long getDelay() {
        return delay;
    }

    public void setDelay(Long delay) {
        this.delay = delay;
    }

    public Long getTimestampStartTime() {
        return timestampStartTime;
    }

    public void setTimestampStartTime(Long timestampStartTime) {
        this.timestampStartTime = timestampStartTime;
    }

    public Long getTimestampEndTime() {
        return timestampEndTime;
    }

    public void setTimestampEndTime(Long timestampEndTime) {
        this.timestampEndTime = timestampEndTime;
    }

    public List<StreamConfigurationDto> getStreamConfigurations() {
        return streamConfigurations;
    }

    public void setStreamConfigurations(List<StreamConfigurationDto> streamConfigurations) {
        this.streamConfigurations = streamConfigurations;
    }

    public void addStreamConfiguration(StreamConfigurationDto config) {
        streamConfigurations.add(config);
    }
}
