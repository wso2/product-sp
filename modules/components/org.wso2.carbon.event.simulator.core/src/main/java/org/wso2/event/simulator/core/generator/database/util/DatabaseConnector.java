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

package org.wso2.event.simulator.core.generator.database.util;

import org.apache.log4j.Logger;
import org.wso2.event.simulator.core.bean.DBSimulationDto;
import org.wso2.event.simulator.core.exception.EventGenerationException;
import org.wso2.event.simulator.core.exception.SimulatorInitializationException;

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

    private static final String driver = "com.mysql.jdbc.Driver";
    private String url = "jdbc:mysql://localhost:3306/";
    private Connection dbConnection;
    private String dataSourceLocation;
    private String username;
    private String password;
    private String tableName;
    private List<String> columnNames;
    private String timestampAttribute;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;


    public DatabaseConnector(DBSimulationDto databaseConfiguration) {
        this.dataSourceLocation = this.url + databaseConfiguration.getDatabaseName();
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
                    prepareSQLstatement(timestampStartTime, timestampEndTime);
                    this.resultSet = preparedStatement.executeQuery();
                }
            } else {
                throw new EventGenerationException("Unable to connect to source '" + dataSourceLocation + "'");
            }
        } catch (SQLException e) {
            throw new EventGenerationException("Error occurred when retrieving resultset from  table '" + tableName +
                    "' in data source '" + dataSourceLocation + "'. ", e);
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
            throw new SimulatorInitializationException(" Error occurred while connecting to database : ", e);
        } catch (ClassNotFoundException e) {
            throw new SimulatorInitializationException(" Error occurred when loading driver : ", e);
        } catch (InstantiationException e) {
            throw new SimulatorInitializationException(" Error occurred when instantiating driver class : ", e);
        } catch (IllegalAccessException e) {
            throw new SimulatorInitializationException(" Error occurred when accessing the driver : ", e);
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
        try {
            DatabaseMetaData metaData = dbConnection.getMetaData();
            /*
            retrieve a resultset containing tables with name 'tableName'. if resultset has entries, table exists
            in data source
            */
            ResultSet tableResults = metaData.getTables(null, null, tableName, null);
            if (tableResults.isBeforeFirst()) {

                if (log.isDebugEnabled()) {
                    log.debug("Table '" + tableName + "' exists in data source '" + dataSourceLocation);
                }
                return true;
            } else {
                throw new EventGenerationException(" Table '" + tableName + "' does not exist in data source '" +
                        dataSourceLocation + "'");
            }
        } catch (SQLException e) {
            throw new EventGenerationException("Error occurred when validating whether table '" + tableName +
                    "' exists in '" + dataSourceLocation + "'");
        }
    }


    /**
     * validateColumns method checks whether the columns specified exists in the specified table in the
     * specified database
     *
     * @return true if columns exists
     */
    private Boolean validateColumns() {
        try {
            DatabaseMetaData metaData = dbConnection.getMetaData();
            /*
            retrieve a resultset containing column details of table 'tableName'.
            if the resultset has entries, convert the column names in resultset into a list.
            check whether each column name specified by user exists in this list
            if yes, column names are valid.
            if not, throw exception
            */
            ResultSet columnResults =
                    metaData.getColumns(null, null, tableName, null);

            if (columnResults.isBeforeFirst()) {
                List<String> resulsetColumns = new ArrayList<>();

                while (columnResults.next()) {
                    resulsetColumns.add(columnResults.getString("COLUMN_NAME"));
                }

                columnNames.forEach(columnName -> {
                    if (!resulsetColumns.contains(columnName)) {
                        throw new EventGenerationException("Column '" + columnName + "' does not exist in table '" +
                                tableName + "' in data source '" + dataSourceLocation + "'");
                    }
                });

            } else {
                throw new EventGenerationException("Table '" + tableName + "' in data source '" + dataSourceLocation +
                        "' is empty");
            }
        } catch (SQLException e) {
            throw new EventGenerationException("Error occurred when validating whether the columns exists in table '"
                    + tableName + "' in the data source '" + dataSourceLocation + "'", e);
        }
        return true;
    }

    /**
     * PrepareSQLstatement() method creates the prepared statement needed to retrieve resultset
     *
     * @param timestampStartTime least possible value for timestamp
     * @param timestampEndTime   maximum possible value for timestamp
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    private void prepareSQLstatement(Long timestampStartTime, Long timestampEndTime) {

        String columns = String.join(",", columnNames);
        try {
            if (timestampEndTime != null) {
                this.preparedStatement = dbConnection.prepareStatement(String.format(("SELECT %s,%s FROM %s WHERE %s " +
                                ">= %d AND %s <= %d ORDER BY ABS(%s);"), timestampAttribute, columns, tableName,
                        timestampAttribute, timestampStartTime, timestampAttribute, timestampEndTime,
                        timestampAttribute));
            } else {
                this.preparedStatement = dbConnection.prepareStatement(String.format(("SELECT %s,%s FROM %s WHERE %s " +
                                ">= %d ORDER BY ABS(%s);"), timestampAttribute, columns, tableName, timestampAttribute,
                        timestampStartTime, timestampAttribute));

            }

        } catch (SQLException e) {
            throw new EventGenerationException("Error occurred when forming prepared statement : ", e);
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
            throw new EventGenerationException("Error occurred when terminating database connection : ", e);
        }

        if (log.isDebugEnabled()) {
            log.debug("Close resources used for database simulation");
        }
    }

}
