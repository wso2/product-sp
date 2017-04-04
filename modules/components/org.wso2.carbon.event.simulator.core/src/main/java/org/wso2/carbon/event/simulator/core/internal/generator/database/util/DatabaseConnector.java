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

package org.wso2.carbon.event.simulator.core.internal.generator.database.util;

import org.apache.log4j.Logger;
import org.wso2.carbon.event.simulator.core.exception.EventGenerationException;
import org.wso2.carbon.event.simulator.core.exception.SimulatorInitializationException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 * DatabaseConnector is a utility class performs the following tasks
 * 1. Load the driver
 * 2. Connect to the database
 * 3. Create and execute a SELECT query
 * 4. Return a result set containing data required for database event simulation
 * 5. Close database connection
 */
public class DatabaseConnector {

    private static final Logger log = Logger.getLogger(DatabaseConnector.class);
    private static final String query_attribute_OnlyStartTime = "SELECT %s,%s FROM %s WHERE %s >= %d ORDER BY ABS(%s);";
    private static final String query_attribute_WithBothLimits = "SELECT %s,%s FROM %s WHERE %s >= %d AND %s <= %d " +
            "ORDER BY ABS(%s);";
    private static final String query_interval = "SELECT %s FROM %s;";
    private Connection dbConnection;
    private String dataSourceLocation;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;


    public DatabaseConnector() {
    }

    /**
     * getDatabaseEvenItems method is used to obtain data from a database
     *
     * @param tableName          table from which data must be retrieved
     * @param columnNames        list of columns to be retrieved
     * @param timestampAttribute column containing timestamp
     * @param timestampStartTime least possible timestamp
     * @param timestampEndTime   maximum possible timestamp
     * @return resultset containing data needed for event simulation
     */
    public ResultSet getDatabaseEventItems(String tableName, List<String> columnNames, String timestampAttribute,
                                           long timestampStartTime, long timestampEndTime) {
        /**
         * check whether,
         * 1. database connection is established
         * 2. table exists
         * 3. column names are valid
         *
         * if successful, create an sql query and retrieve data for event generation
         * else throw an exception
         * */
        try {
            if (dbConnection != null && !dbConnection.isClosed()) {
                if (checkTableExists(tableName) && validateColumns(tableName, columnNames)) {
                    prepareSQLstatement(tableName, columnNames, timestampAttribute, timestampStartTime,
                            timestampEndTime);
                    this.resultSet = preparedStatement.executeQuery();
                }
            } else {
                throw new EventGenerationException("Unable to connect to source '" + dataSourceLocation + "' to " +
                        "retrieve data for the configuration, table name : '" + tableName + "', column names : '" +
                        columnNames + "', timestamp attribute : '" + timestampAttribute + "', timestamp start time : " +
                        "'" + timestampStartTime + "' and timestamp end time : '" + timestampEndTime + "'.");
            }
        } catch (SQLException e) {
            log.error("Error occurred when retrieving resultset from source '" + dataSourceLocation + "' " +
                    "to retrieve data for the configuration table name : '" + tableName + "'," +
                    " column names : '" + columnNames + "', timestamp attribute : '" + timestampAttribute + "', " +
                    "timestamp start time : '" + timestampStartTime + "' and timestamp end time : '" +
                    timestampEndTime + "'. ", e);
            closeConnection();
            throw new EventGenerationException("Error occurred when retrieving resultset from source '" +
                    dataSourceLocation + "' to retrieve data for the configuration, table name : '" + tableName + "'," +
                    " column names : '" + columnNames + "', timestamp attribute : '" + timestampAttribute + "', " +
                    "timestamp start time : '" + timestampStartTime + "' and timestamp end time : '" +
                    timestampEndTime + "'. ", e);
        }
        return resultSet;
    }

    /**
     * This method loads the JDBC driver and creates a database connection
     *
     * @param dataSourceLocation location of database to be used
     * @param username           username
     * @param password           password
     */
    public void connectToDatabase(String driver, String dataSourceLocation, String username, String password) {
        try {
            this.dataSourceLocation = dataSourceLocation;
            Class.forName(driver).newInstance();
            dbConnection = DriverManager.getConnection(dataSourceLocation, username, password);
        } catch (SQLException e) {
            log.error("Error occurred while connecting to database for the configuration : driver : '"
                    + driver + "', data source location : '" + dataSourceLocation + "', username : '" + username + "'" +
                    " and password : '" + password + "'. ", e);
            closeConnection();
            throw new SimulatorInitializationException(" Error occurred while connecting to database for the" +
                    " configuration : driver : '" + driver + "', data source location : '" + dataSourceLocation + "'," +
                    " username : '" + username + "' and password : '" + password + "'.  ", e);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            log.error(" Error occurred when loading driver for the configuration driver : '" + driver + "', " +
                    "data source location : '" + dataSourceLocation + "', username : '" + username + "' and password " +
                    ": '" + password + "'. ", e);
            closeConnection();
            throw new SimulatorInitializationException(" Error occurred when loading driver for the" +
                    " configuration driver : '" + driver + "', data source location : '" + dataSourceLocation + "', " +
                    "username : '" + username + "' and password : '" + password + "'. ", e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Create a database connection for for the configuration driver : '" + driver + "', data source " +
                    "location : '" + dataSourceLocation + "', username : '" + username + "' and password : '" +
                    password + "'. ");
        }
    }

    /**
     * checkTableExists methods checks whether the table specified exists in the specified database
     *
     * @param tableName name of table from which data must be retrieved
     * @return true if table exists in the database
     */
    private Boolean checkTableExists(String tableName) {
        try {
            DatabaseMetaData metaData = dbConnection.getMetaData();
            /**
             * retrieve a resultset containing tables with name 'tableName'.
             * if resultset has entries, table exists in data source
             * else close resources and throw an exception indicating that the table is not available in the data source
             * if an SQL exception occurs while checking whether the table exists close resources and throw an exception
             * */
//            todo R check about the schema
            ResultSet tableResults = metaData.getTables(null, null, tableName, null);
            if (tableResults.isBeforeFirst()) {
                if (log.isDebugEnabled()) {
                    log.debug("Table '" + tableName + "' exists in data source '" + dataSourceLocation);
                }
                return true;
            } else {
                closeConnection();
                throw new EventGenerationException(" Table '" + tableName + "' does not exist in data source '" +
                        dataSourceLocation + "'.");
            }
        } catch (SQLException e) {
            log.error("Error occurred when validating whether table '" + tableName +
                    "' exists in '" + dataSourceLocation + "'. ", e);
            closeConnection();
            throw new EventGenerationException("Error occurred when validating whether table '" + tableName +
                    "' exists in '" + dataSourceLocation + "'. ", e);
        }
    }


    /**
     * validateColumns method checks whether the columns specified exists in the specified table in the
     * specified database
     *
     * @param tableName   table from which data must be retrieved
     * @param columnNames list of columns to be retrieved
     * @return true if columns exists
     */
    private Boolean validateColumns(String tableName, List<String> columnNames) {
        try {
            DatabaseMetaData metaData = dbConnection.getMetaData();
            /**
             * retrieve a resultset containing column details of table 'tableName'.
             * check whether each column name specified by user exists in this list
             * if yes, column names are valid.
             * if not, close resources used and throw exception
             * if an SQL exception occurs while validating column names, close resources and throw an exception
             * */
//            todo R check about the schema and check whether the getcolumns.isbeforefirst is needed
            ResultSet columnResults =
                    metaData.getColumns(null, null, tableName, null);
            if (columnResults.isBeforeFirst()) {
                List<String> resulsetColumns = new ArrayList<>();
                while (columnResults.next()) {
                    resulsetColumns.add(columnResults.getString("COLUMN_NAME"));
                }
                columnNames.forEach(columnName -> {
                    if (!resulsetColumns.contains(columnName)) {
                        closeConnection();
                        throw new EventGenerationException("Column '" + columnName + "' does not exist in table '" +
                                tableName + "' in data source '" + dataSourceLocation + "'.");
                    }
                });
            }
        } catch (SQLException e) {
            log.error("Error occurred when validating whether the columns ' " +
                    columnNames + "' exists in table '" + tableName + "' in the data source '" +
                    dataSourceLocation + "'. ", e);
            closeConnection();
            throw new EventGenerationException("Error occurred when validating whether the columns ' " +
                    columnNames + "' exists in table '" + tableName + "' in the data source '" +
                    dataSourceLocation + "'. ", e);
        }
        return true;
    }

    /**
     * PrepareSQLstatement() method creates the prepared statement needed to retrieve resultset
     *
     * @param tableName          table from which data must be retrieved
     * @param columnNames        list of columns to be retrieved
     * @param timestampAttribute column containing timestamp
     * @param timestampStartTime least possible value for timestamp
     * @param timestampEndTime   maximum possible value for timestamp
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    private void prepareSQLstatement(String tableName, List<String> columnNames, String timestampAttribute,
                                     long timestampStartTime, long timestampEndTime) {
        /**
         * create a prepared statement based on the timestamp start time and timestamp end time provided
         * if an exception occurs while creating the prepared statement close resources and throw an exception
         * */
        String columns = String.join(",", columnNames);
        try {
            if (timestampAttribute == null) {
                this.preparedStatement = dbConnection.prepareStatement(String.format(query_interval, columns,
                        tableName));
            } else {
                if (timestampEndTime == -1) {
                    this.preparedStatement = dbConnection.prepareStatement(String.format(query_attribute_OnlyStartTime,
                            timestampAttribute, columns, tableName, timestampAttribute, timestampStartTime,
                            timestampAttribute));
                } else {
                    this.preparedStatement = dbConnection.prepareStatement(String.format(query_attribute_WithBothLimits,
                            timestampAttribute, columns, tableName, timestampAttribute, timestampStartTime,
                            timestampAttribute, timestampEndTime, timestampAttribute));
                }
            }
        } catch (SQLException e) {
            log.error("Error occurred when forming prepared statement for the configuration table name : '" +
                    tableName + "', columns : '" + columns + "', timestamp attribute : '" + timestampAttribute + "', " +
                    "timestamp start time : '" + timestampStartTime + "' and timestamp end time : '" +
                    timestampEndTime + "'. ", e);
            closeConnection();
            throw new EventGenerationException("Error occurred when forming prepared statement for the configuration" +
                    "table name : '" + tableName + "', columns : '" + columns + "', timestamp attribute : '" +
                    timestampAttribute + "', timestamp start time : '" + timestampStartTime + "' and timestamp end " +
                    "time : '" + timestampEndTime + "'. ", e);
        }
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
            log.error("Error occurred when terminating database resources used for data source '" +
                    dataSourceLocation + "'. ", e);
            throw new EventGenerationException("Error occurred when terminating database resources used for " +
                    "data source '" + dataSourceLocation + "'. ", e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Close resources used for data source '" + dataSourceLocation + "'");
        }
    }

}
