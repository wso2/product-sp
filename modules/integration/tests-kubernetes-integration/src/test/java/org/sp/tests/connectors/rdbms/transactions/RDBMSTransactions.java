/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.sp.tests.connectors.rdbms.transactions;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sp.tests.base.SPBaseTest;
import org.sp.tests.util.Constants;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.testng.Assert.assertEquals;

/**
 * Tests to cover RDBMS transactions
 */
public class RDBMSTransactions extends SPBaseTest {

    private static final Log log = LogFactory.getLog(RDBMSTransactions.class);
    Connection conn = null;
    Statement stmt = null;
    Statement stmt2 = null;

    @BeforeClass(alwaysRun = true) public void initializeTables() {
        String createPersonLKTable = "CREATE TABLE personsinlanka (\n" + "    personid int NOT NULL,\n"
                + "    lastname varchar(255) NOT NULL,\n" + "    firstname varchar(255),\n"
                + "    address varchar(255),\n" + "    city varchar(255),\n" + "    PRIMARY KEY (personid)\n" + ");";
        String createPersonsUSTable = "CREATE TABLE personsinus (\n" + "    personid int NOT NULL,\n"
                + "    lastname varchar(255) NOT NULL,\n" + "    firstname varchar(255),\n"
                + "    address varchar(255),\n" + "    city varchar(255),\n" + "    PRIMARY KEY (personid)\n" + ");";

        try {
            conn = DriverManager.getConnection(
                    "jdbc:mysql://" + mysqlURL + "?" + "user=" + Constants.MYSQL_USERNAME + "&password="
                            + Constants.MYSQL_PASSWORD);

            stmt = conn.createStatement();
            stmt.executeUpdate(createPersonLKTable);
            stmt.executeUpdate(createPersonsUSTable);

        } catch (SQLException ex) {
            log.error("SQLException: " + ex.getMessage());
            log.error("SQLState: " + ex.getSQLState());
            log.error("VendorError: " + ex.getErrorCode());
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException sqlEx) {
                }
            }
        }
    }

    @Test public static void insertIntoTable() {

        String serviceURL = spURL + "/persons/insert/success";
        String payload = "{  \n" + "   \"id\":1,\n" + "   \"firstname\":\"danuja\",\n" + "   \"lastname\":\"perera\",\n"
                + "   \"address\":\"260, Mahawatta Rd, Colombo 14\",\n" + "   \"city\":\"kentaky\"\n" + "}";
        try {
            StringRequestEntity requestEntity = new StringRequestEntity(payload, "application/json", "UTF-8");

            HttpClient client = new HttpClient();
            PostMethod post = new PostMethod(serviceURL);

            post.setRequestEntity(requestEntity);

            int statuscode = client.executeMethod(post);

            // Asserting the Status code. Expected 200 OK
            assertEquals(statuscode, HttpStatus.SC_OK);

            byte[] response = post.getResponseBody();

            // Asserting the Response Message.
            assertEquals(new String(response), "Data Insertion Successful");

            //TODO : Assert the actual database table values

        } catch (IOException e) {
            log.error("Error while calling the BE server : " + e.getMessage(), e);
        }
    }

    @Test public void abortONSQLError() {
        String serviceURL = spURL + "/persons/insert/sqlError";
        String payload = "{  \n" + "   \"id\":1,\n" + "   \"firstname\":\"danuja\",\n" + "   \"lastname\":\"perera\",\n"
                + "   \"address\":\"260, Mahawatta Rd, Colombo 14\",\n" + "   \"city\":\"kentaky\"\n" + "}";
        try {
            StringRequestEntity requestEntity = new StringRequestEntity(payload, "application/json", "UTF-8");

            HttpClient client = new HttpClient();
            PostMethod post = new PostMethod(serviceURL);

            post.setRequestEntity(requestEntity);

            int statuscode = client.executeMethod(post);

            // Asserting the Status code. Expected 200 OK
            assertEquals(statuscode, HttpStatus.SC_OK);

            byte[] response = post.getResponseBody();

            // Asserting the Response Message.
            assertEquals(new String(response), "Data Insertion Failed");

            //TODO : Assert the actual database table values

        } catch (IOException e) {
            log.error("Error while calling the BE server : " + e.getMessage(), e);
        }
    }

    @Test public void forceAbort() {
        String serviceURL = spURL + "/persons/insert/sqlError";
        String payload = "{  \n" + "   \"id\":1,\n" + "   \"firstname\":\"danuja\",\n" + "   \"lastname\":\"perera\",\n"
                + "   \"address\":\"260, Mahawatta Rd, Colombo 14\",\n" + "   \"city\":\"kentaky\"\n" + "}";
        try {
            StringRequestEntity requestEntity = new StringRequestEntity(payload, "application/json", "UTF-8");

            HttpClient client = new HttpClient();
            PostMethod post = new PostMethod(serviceURL);

            post.setRequestEntity(requestEntity);

            int statuscode = client.executeMethod(post);

            // Asserting the Status code. Expected 200 OK
            assertEquals(statuscode, HttpStatus.SC_OK);

            byte[] response = post.getResponseBody();

            // Asserting the Response Message.
            assertEquals(new String(response), "Data Insertion Failed");

        } catch (IOException e) {
            log.error("Error while calling the BE server : " + e.getMessage(), e);
        }
    }
}
