package org.wso2.carbon.event.simulator.core.internal.bean;


/**
 * SimulationPropertiesDTO class is used to create simulation configuration objects.
 */
public class SimulationPropertiesDTO {

    private String simulationName;
    private Long timeInterval;
    private Integer noOfEventsRequired;
    private Long timestampStartTime;
    private Long timestampEndTime;

    public String getSimulationName() {
        return simulationName;
    }

    public void setSimulationName(String simulationName) {
        this.simulationName = simulationName;
    }

    public Long getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(Long timeInterval) {
        this.timeInterval = timeInterval;
    }

    public Integer getNoOfEventsRequired() {
        return noOfEventsRequired;
    }

    public void setNoOfEventsRequired(Integer noOfEventsRequired) {
        this.noOfEventsRequired = noOfEventsRequired;
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

}
