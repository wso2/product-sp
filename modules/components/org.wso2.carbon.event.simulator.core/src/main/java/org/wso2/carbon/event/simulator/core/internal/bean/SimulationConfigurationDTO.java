package org.wso2.carbon.event.simulator.core.internal.bean;

import java.util.List;

/**
 * SimulationConfigurationDTO class represents configuration for feed simulation
 */
public class SimulationConfigurationDTO {

    private SimulationPropertiesDTO properties;
    private List<CSVSimulationDTO> csvSources;
    private List<DBSimulationDTO> dbSources;
    private List<RandomSimulationDTO> randomSources;

    public SimulationPropertiesDTO getProperties() {
        return properties;
    }

    public void setProperties(SimulationPropertiesDTO properties) {
        this.properties = properties;
    }

    public List<CSVSimulationDTO> getCsvSources() {
        return csvSources;
    }

    public void setCsvSources(List<CSVSimulationDTO> csvSources) {
        this.csvSources = csvSources;
    }

    public List<DBSimulationDTO> getDbSources() {
        return dbSources;
    }

    public void setDbSources(List<DBSimulationDTO> dbSources) {
        this.dbSources = dbSources;
    }

    public List<RandomSimulationDTO> getRandomSources() {
        return randomSources;
    }

    public void setRandomSources(List<RandomSimulationDTO> randomSources) {
        this.randomSources = randomSources;
    }
}
