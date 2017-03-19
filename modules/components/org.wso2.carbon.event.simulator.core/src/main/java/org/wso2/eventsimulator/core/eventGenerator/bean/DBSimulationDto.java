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

package org.wso2.eventsimulator.core.eventGenerator.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.eventsimulator.core.eventGenerator.util.exceptions.ConfigurationParserException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * DBSimulationDto class contains configuration for database simulation.
 */
public class DBSimulationDto extends StreamConfigurationDto {

    private static final Logger log = LoggerFactory.getLogger(DBSimulationDto.class);

    private String databaseName;
    private String username;
    private String password;
    private String tableName;
    private List<String> columnNames;


    public DBSimulationDto() {
        super();
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(String columnNamesString) {

        /*
        * convert the column names in to an array list
        * check whether the column names contain empty string or null values.
        * if yes, throw an exception
        * else, set to the columnNames list
        * */
        List<String> columns = new ArrayList<String>(Arrays.asList(columnNamesString.split("\\s*,\\s*")));

        columns.forEach(column -> {
            if (column.isEmpty()) {
                throw new ConfigurationParserException("Column name cannot contain empty values");
            }
        });

        if (log.isDebugEnabled()) {
            log.debug("Set column names of table '" + tableName + "' in database '" + databaseName + "'");
        }

        this.columnNames = columns;
    }

}
