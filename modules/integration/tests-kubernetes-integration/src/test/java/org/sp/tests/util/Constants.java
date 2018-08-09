/**
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
package org.sp.tests.util;
/**
 * Constants class
 * */
public class Constants {
    private Constants() {

    }

    public static final String SIDDHI_APP_API = "http://localhost:9090";
    public static final String MSF4J_TEST_API = "http://localhost:8080";
    public static final Integer SP_PORT = 9090;
    public static final String HTTP_POST = "POST";
    public static final String HTTP_GET = "GET";
    public static final String HTTP_DELETE = "DELETE";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_CONTTP_JSON = "application/json";
    public static final String HEADER_CONTTP_XML = "application/xml";
    public static final String HEADER_CONTTP_TEXT = "text/plain";
    public static final String DEFAULT_USER_NAME = "admin";
    public static final String DEFAULT_PASSWORD = "admin";
    public static final int HTTP_RESP_200 = 200;
    public static final int HTTP_RESP_201 = 201;
    public static final int HTTP_RESP_204 = 204;
    public static final int HTTP_RESP_401 = 401;
    public static final int HTTP_RESP_400 = 400;
    public static final String MYSQL_USERNAME = "spintegrationtest";
    public static final String MYSQL_PASSWORD = "spintegrationtest";
    public static final String SINGLE_EVENT_SIMULATION_STARTED_SUCCESSFULLY =
            "{\"status\":\"OK\",\"message\":\"Single Event simulation started successfully\"}";

}
