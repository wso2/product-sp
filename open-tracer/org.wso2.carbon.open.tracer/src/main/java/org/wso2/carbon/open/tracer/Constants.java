/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/
package org.wso2.carbon.open.tracer;

/**
 * This class holds the constants that are used in the analytics tracing component.
 */
class Constants {
    private Constants() {
    }

    static final String ANALYTICS_SPAN_STREAM_ID = "SpanStream:1.0.0";
    static final String USERNAME_CONFIG = "username";
    static final String PASSWORD_CONFIG = "password";
    static final String URL_CONFIG = "url";
    static final String AUTH_URL_CONFIG = "authUrl";
    static final String PUBLISHER_TYPE_CONFIG = "publisher.type";
    static final String COMPONENT_NAME_CONFIG = "component.name";
    static final String TRACER_NAME = "wso2-analytics";

    static final String DEFAULT_USERNAME = "admin";
    static final String DEFAULT_PASSWORD = "admin";
    static final String DEFAULT_URL = "tcp://localhost:7611";
    static final String DEFAULT_AUTH_URL = "ssl://localhost:7711";
    static final String DEFAULT_PUBLISHER_TYPE = "Thrift";
    static final String DEFAULT_COMPONENT_NAME = "WSO2 MSF4J";
}
