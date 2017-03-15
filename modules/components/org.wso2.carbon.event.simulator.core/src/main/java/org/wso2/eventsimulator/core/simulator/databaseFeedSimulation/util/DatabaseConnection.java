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

package org.wso2.eventsimulator.core.simulator.databaseFeedSimulation.util;

import org.apache.log4j.Logger;
import org.wso2.eventsimulator.core.simulator.databaseFeedSimulation.DatabaseFeedSimulationDto;
import org.wso2.eventsimulator.core.simulator.exception.DatabaseConnectionException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.DatabaseMetaData;
import java.util.List;

/**
 * DatabaseConnection is a utility class performs the following tasks
 * 1. Load the driver
 * 2. Connect to the database
 * 3. Create and execute a SELECT query
 * 4. Return a result set containing data required for database feed simulation
 * 5. Close database connection
 */

public class DatabaseConnection {

    private static final Logger log = Logger.getLogger(DatabaseConnection.class);

    private static String driver = "com.mysql.jdbc.Driver";
    private String URL = "jdbc:mysql://localhost:3306/";
    private Connection dbConnection;
    private String dataSourceLocation;
    private String databaseName;
    private String username;
    private String password;
    private String tableName;
    private List<String> columnNames;
    private String timestampAttribute;
    private String query;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;


    public DatabaseConnection(DatabaseFeedSimulationDto databaseConfiguration) {
        this.databaseName = databaseConfiguration.getDatabaseName();
        this.dataSourceLocation = this.URL+databaseName;
        this.username = databaseConfiguration.getUsername();
        this.password = databaseConfiguration.getPassword();
        this.tableName = databaseConfiguration.getTableName();
        this.columnNames = databaseConfiguration.getColumnNames();
        this.timestampAttribute = databaseConfiguration.getTimestampAttribute();
    }

    /**
     * getDatabaseEvenItems method is used to obtain data from a database
     *
     * @return resultset containing data needed for feed simulation
     * */

    public ResultSet getDatabaseEventItems(Long timestampStartTime, Long timestampEndTime) {

       try {
           if(!dbConnection.isClosed() || dbConnection != null) {
               if(checkTableExists()) {
                   if (timestampEndTime != null) {
                       query = prepareSQLstatement(timestampStartTime, timestampEndTime);
                   } else {
                       query = prepareSQLstatement(timestampStartTime);
                   }
               }
               this.preparedStatement = dbConnection.prepareStatement(query);
               this.resultSet = preparedStatement.executeQuery();
           }
           return resultSet;
       }
       catch (SQLException e) {
           throw new DatabaseConnectionException("Error : " + e.getMessage());
       }
    }

    /**
     *  This method loads the JDBC driver and returns a database connection
     *  */

    public void connectToDatabase() {
        try {
            Class.forName(driver).newInstance();
            dbConnection = DriverManager.getConnection(dataSourceLocation,username,password);
        }
        /*
        When loading the driver either one of the following exceptions may occur
        1. ClassNotFoundException
        2. IllegalAccessException
        3. InstantiationException

        Establishing a database connection may throw an SQLException
        */
        catch (SQLException e) {
            throw new DatabaseConnectionException(" Error occurred while connecting to database : " + e.getMessage());
        }
        catch (Exception e) {
            throw new DatabaseConnectionException(" Error occurred when loading driver : " + e.getMessage());
        }
    }

    /**
     * checkTableExists methods checks whether the table specified exists in the specified database
     *
     * @return true if table exists in the database
     * */
    private boolean checkTableExists() {
        try {
            DatabaseMetaData metaData = dbConnection.getMetaData();
            ResultSet tableResults = metaData.getTables(null, null, tableName, null);
            if(tableResults.isBeforeFirst()) {
                return true;
            } else {
                throw new DatabaseConnectionException(" Table '" + tableName + "' does not exist in database '" + databaseName + "'");
            }
        } catch (SQLException e) {
           throw new DatabaseConnectionException(e.getMessage());
        }
    }

    /**
     * PrepareSQLstatement method creates a string object of a SQL query.
     *
     * @return a string object of a SQL query
     * */

    private String prepareSQLstatement(Long timestampStartTime) {

        String columns = String.join(",",columnNames);
        String query = String.format("SELECT %s FROM %s WHERE %s >= %d ORDER BY %s;",columns,tableName,timestampAttribute,timestampStartTime,timestampAttribute);
        return query;
    }

    /**
     * PrepareSQLstatement method will be overloaded with timestampAttribute name if orderbyTimestamp flag is set to true.
     *
     * @return a string object of a SQL query
     * */

    private String prepareSQLstatement(Long timestampStartTime, Long timestampEndTime) {

        String columns = String.join(",",columnNames);
        String query = String.format("SELECT %s FROM %s WHERE %s >= %d && %s <= %d ORDER BY %s;",columns,tableName,timestampAttribute,timestampStartTime,timestampAttribute,timestampEndTime,timestampAttribute);
        return query;
    }

   /**
    * closeConnection method releases the database sources acquired.
    *
    * It performs the following tasks
    * 1. Close resultset obtained by querying the database
    * 2. Close prepared statement used to query the database
    * 3. Close the database connection established
    * */

    public void closeConnection() {
        try {
            if (this.resultSet != null) {
                this.resultSet.close();
            }
            if (this.preparedStatement != null) {
                this.preparedStatement.close();
            }
            if (this.dbConnection != null || !this.dbConnection.isClosed()) {
                dbConnection.close();
            }
        } catch (SQLException e) {
            log.error("Error occurred when terminating database connection : " + e.getMessage());
        }
    }

}
