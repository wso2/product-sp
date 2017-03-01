package org.wso2.eventsimulator.core.simulator.databaseFeedSimulation;


import org.wso2.eventsimulator.core.simulator.bean.FeedSimulationStreamConfiguration;

import java.util.LinkedHashMap;

/**
 * DatabaseFeedSimulationDto returns configuration for database simulation.
 */
public class DatabaseFeedSimulationDto extends FeedSimulationStreamConfiguration {

    private String databaseConfigName;
    private String databaseName;
    private String username;
    private String password;
    private String tableName;
    private LinkedHashMap<String,String> columnNamesAndTypes;
    private int delay;
    public DatabaseFeedSimulationDto() {  }

    public String getDatabaseConfigName() { return databaseConfigName; }

    public void setDatabaseConfigName(String databaseConfigName) {
        this.databaseConfigName = databaseConfigName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getUsername(){return username;}

    public void setUsername(String username){this.username=username;}

    public String getPassword(){return password;}

    public void setPassword(String password){this.password = password;}

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public LinkedHashMap<String, String> getColumnNamesAndTypes() {
        return columnNamesAndTypes;
    }

    public void setColumnNamesAndTypes(LinkedHashMap<String,String> columnNamesAndTypes) {
        this.columnNamesAndTypes = columnNamesAndTypes;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

}
