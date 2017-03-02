/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
