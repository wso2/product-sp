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

package org.wso2.eventsimulator.core.eventGenerator.databaseEventGeneration.util;

import org.apache.log4j.Logger;
import org.wso2.eventsimulator.core.eventGenerator.bean.DBSimulationDto;
import org.wso2.eventsimulator.core.eventGenerator.util.exceptions.EventGenerationException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
    private String username;
    private String password;
    private String tableName;
    private List<String> columnNames;
    private String timestampAttribute;
    private String query;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;


    public DatabaseConnection(DBSimulationDto databaseConfiguration) {
        this.dataSourceLocation = this.URL + databaseConfiguration.getDatabaseName();
        this.username = databaseConfiguration.getUsername();
        this.password = databaseConfiguration.getPassword();
        this.tableName = databaseConfiguration.getTableName();
        this.columnNames = databaseConfiguration.getColumnNames();
        this.timestampAttribute = databaseConfiguration.getTimestampAttribute();
    }

    /**
     * getDatabaseEvenItems method is used to obtain data from a database
     *
     * @param timestampStartTime least possible timestamp
     * @param timestampEndTime   maximum possible timestamp
     * @return resultset containing data needed for event simulation
     */
    public ResultSet getDatabaseEventItems(Long timestampStartTime, Long timestampEndTime) {

        /*
        * check whether,
        * 1. database connection is established
        * 2. table exists
        * 3. column names are valid
        *
        * if successful, create an sql query and retrieve data for event generation
        * else, resultset will remain as null
        * */
        try {
            if (dbConnection != null && !dbConnection.isClosed()) {
                if (checkTableExists() && validateColumns()) {
                    if (timestampEndTime != null) {
                        query = prepareSQLstatement(timestampStartTime, timestampEndTime);
                    } else {
                        query = prepareSQLstatement(timestampStartTime);
                    }
                    this.preparedStatement = dbConnection.prepareStatement(query);
                    this.resultSet = preparedStatement.executeQuery();
                }
            } else {
                throw new EventGenerationException("No database connection available for source '" + dataSourceLocation
                        + "'");
            }
        } catch (SQLException e) {
            log.error("Error occurred when retrieving resultset from  table '" + tableName + "' in data" +
                    " source '" + dataSourceLocation + "'. ", e);
        }
        return resultSet;
    }

    /**
     * This method loads the JDBC driver and creates a database connection
     */
    public void connectToDatabase() {
        try {
            Class.forName(driver).newInstance();
            dbConnection = DriverManager.getConnection(dataSourceLocation, username, password);
        } catch (SQLException e) {
            log.error(" Error occurred while connecting to database : ", e);
        } catch (ClassNotFoundException e) {
            log.error(" Error occurred when loading driver : ", e);
        } catch (InstantiationException e) {
            log.error(" Error occurred when instantiating driver class : ", e);
        } catch (IllegalAccessException e) {
            log.error(" Error occurred when accessing the driver : ", e);
        }

        if (log.isDebugEnabled()) {
            log.debug("Create a database connection for " + dataSourceLocation);
        }
    }

    /**
     * checkTableExists methods checks whether the table specified exists in the specified database
     *
     * @return true if table exists in the database
     */
    private Boolean checkTableExists() {
        boolean tableExists = false;
        try {
            DatabaseMetaData metaData = dbConnection.getMetaData();
            /*
            retrieve a resultset containing tables with name 'tableName'. if resultset has entries, table exists
            in data source
            */
            ResultSet tableResults = metaData.getTables(null, null, tableName, null);
            if (tableResults.isBeforeFirst()) {
                tableExists = true;
            } else {
                throw new EventGenerationException(" Table '" + tableName + "' does not exist in data source '" +
                        dataSourceLocation + "'");
            }
        } catch (SQLException e) {
            log.error("Error occurred when validating whether table '" + tableName + "' exists in '" +
                    dataSourceLocation + "'");
        }

        if (log.isDebugEnabled()) {
            log.debug("Table '" + tableName + "' exists in data source '" + dataSourceLocation);
        }
        return tableExists;
    }


    /**
     * validateColumns method checks whether the columns specified exists in the specified table in the
     * specified database
     *
     * @return true if columns exists
     */
    private Boolean validateColumns() {
        boolean columnsValid = false;
        try {
            DatabaseMetaData metaData = dbConnection.getMetaData();
            /*
            retrieve a resultset containing column details of table 'tableName'.
            if the resultset has entries, convert the rcolumn names in resultset into a list.
            check whether each column name specified by user exists in this list
            if yes, column names are valid.
            if not, throw exception
            */
            ResultSet columnResults =
                    metaData.getColumns(null, null, tableName, null);

            if (columnResults.isBeforeFirst()) {
                List<String> resulsetColumns = new ArrayList<String>();

                while (columnResults.next()) {
                    resulsetColumns.add(columnResults.getString("COLUMN_NAME"));
                }

                columnNames.forEach(columnName -> {
                    if (!resulsetColumns.contains(columnName)) {
                        throw new EventGenerationException("Column '" + columnName + "' does not exist in table '" +
                                tableName + "' in data source '" + dataSourceLocation + "'");
                    }
                });

                columnsValid = true;

            } else {
                throw new EventGenerationException("Table '" + tableName + "' in data source '" + dataSourceLocation +
                        "' is empty");
            }

        } catch (SQLException e) {
            log.error("Error occurred when validating whether the columns exists in table '" + tableName +
                    "' in the data source '" + dataSourceLocation + "'");
        }
        return columnsValid;
    }

    /**
     * PrepareSQLstatement method creates a string object of a SQL query.
     *
     * @param timestampStartTime least possible value for timestamp
     * @return a string object of a SQL query
     */
    private String prepareSQLstatement(Long timestampStartTime) {

        String columns = String.join(",", columnNames);
        return String.format("SELECT %s,%s FROM %s WHERE %s >= %d ORDER BY ABS(%s);", timestampAttribute, columns,
                tableName, timestampAttribute, timestampStartTime, timestampAttribute);
    }

    /**
     * PrepareSQLstatement method will be overloaded with timestampAttribute name if orderbyTimestamp flag
     * is set to true.
     *
     * @param timestampStartTime least possible value for timestamp
     * @param timestampEndTime   maximum possible value for timestamp
     * @return a string object of a SQL query
     */
    private String prepareSQLstatement(Long timestampStartTime, Long timestampEndTime) {

        String columns = String.join(",", columnNames);
        return String.format("SELECT %s,%s FROM %s WHERE %s >= %d && %s <= %d ORDER BY ABS(%s);", timestampAttribute,
                columns, tableName, timestampAttribute, timestampStartTime, timestampAttribute, timestampEndTime,
                timestampAttribute);
    }

    /**
     * closeConnection method releases the database sources acquired.
     * <p>
     * It performs the following tasks
     * 1. Close resultset obtained by querying the database
     * 2. Close prepared statement used to query the database
     * 3. Close the database connection established
     */
    public void closeConnection() {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (dbConnection != null && !dbConnection.isClosed()) {
                dbConnection.close();
            }
        } catch (SQLException e) {
            log.error("Error occurred when terminating database connection : ", e);
        }

        if (log.isDebugEnabled()) {
            log.debug("Close resources used for database simulation");
        }
    }

}
