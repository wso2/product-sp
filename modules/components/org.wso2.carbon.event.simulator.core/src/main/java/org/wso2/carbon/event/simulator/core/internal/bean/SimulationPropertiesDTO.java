package org.wso2.carbon.event.simulator.core.internal.bean;


/**
 * SimulationPropertiesDTO class is used to create simulation configuration objects.
 */
public class SimulationPropertiesDTO {

    private String simulationName;
    private long timeInterval;
    private int noOfEventsRequired;
    private long timestampStartTime;
    private long timestampEndTime;

    public String getSimulationName() {
        return simulationName;
    }

    public void setSimulationName(String simulationName) {
        this.simulationName = simulationName;
    }

    public long getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(long timeInterval) {
        this.timeInterval = timeInterval;
    }

    public int getNoOfEventsRequired() {
        return noOfEventsRequired;
    }

    public void setNoOfEventsRequired(int noOfEventsRequired) {
        this.noOfEventsRequired = noOfEventsRequired;
    }

    public long getTimestampStartTime() {
        return timestampStartTime;
    }

    public void setTimestampStartTime(long timestampStartTime) {
        this.timestampStartTime = timestampStartTime;
    }

    public long getTimestampEndTime() {
        return timestampEndTime;
    }

    public void setTimestampEndTime(long timestampEndTime) {
        this.timestampEndTime = timestampEndTime;
    }
}
