/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sp.tests.ha.util;

import org.apache.log4j.Logger;
import org.sp.tests.util.HTTPResponse;
import org.sp.tests.util.TestUtil;

import java.io.IOException;
import java.net.URI;
import java.util.Date;

import static org.sp.tests.util.Constants.DEFAULT_PASSWORD;
import static org.sp.tests.util.Constants.DEFAULT_USER_NAME;
import static org.sp.tests.util.Constants.HEADER_CONTTP_TEXT;
import static org.sp.tests.util.Constants.HTTP_POST;
import static org.sp.tests.util.TestUtil.waitThread;

public class SiddhiAppUtil {

    private static final Logger log = Logger.getLogger(SiddhiAppUtil.class);

    public static HTTPResponse deployPassThroughSiddhiApp(URI nodeURI, String siddhiAppName) throws IOException {

        String publisherURL = "http://sp-ha-node-2:8080/testresults";
        String appbody = "@App:name('" + siddhiAppName + "')\n" +
                "@source(type='inMemory', topic='symbol', @map(type='xml'))\n" +
                "define stream FooStream (symbol string, price float, class string);\n" +
                "@sink(type='http', publisher.url='" + publisherURL + "', method='{{method}}', " +
                "headers='{{headers}}',\n" +
                "@map(type='json'))\n" +
                "define stream BarStream (message string, value float, method string, headers string);\n" +

                "from FooStream#log()\n" +
                "select symbol as message, price as value, 'POST' as method, class as headers\n" +
                "insert into BarStream;";

        log.info("Deploying Siddhi Application on " + nodeURI);
        HTTPResponse httpResponse = TestUtil.sendHRequest(appbody, nodeURI, "/siddhi-apps", HEADER_CONTTP_TEXT,
                HTTP_POST, DEFAULT_USER_NAME, DEFAULT_PASSWORD);

        waitThread(2000);
        return httpResponse;
    }

    public static HTTPResponse deployAggregateSiddhiApp(URI nodeURI, String siddhiAppName) throws IOException {

        String publisherURL = "http://sp-ha-node-2:8080/testresults";
        String appbody = "@App:name('" + siddhiAppName + "')\n" +
                "@source(type='inMemory', topic='symbol', @map(type='xml'))\n" +
                "define stream FooStream (symbol string, price float, class string);\n" +
                "@sink(type='http', publisher.url='" + publisherURL + "', method='{{method}}', " +
                "headers='{{headers}}',\n" +
                "@map(type='json'))\n" +
                "define stream BarStream (message string, value float, method string, headers string);\n" +

                "from FooStream#window.length(5)\n" +
                "select symbol as message, max(price) as value, 'POST' as method, class as headers\n" +
                "insert into BarStream;\n" +

                "from BarStream#log(\"Events: \")\n" +
                "insert into OutputStream;";

        log.info("Deploying Siddhi Application on " + nodeURI);
        HTTPResponse httpResponse = TestUtil.sendHRequest(appbody, nodeURI, "/siddhi-apps", HEADER_CONTTP_TEXT,
                HTTP_POST, DEFAULT_USER_NAME, DEFAULT_PASSWORD);

        waitThread(2000);
        return httpResponse;
    }

    public static HTTPResponse sendEvent(URI nodeURI, String testName, String siddhiAppName, String streamName,
                                         String message, String timestamp) throws IOException {

        String queryBody = "{\n" +
                "  \"streamName\": \"" + streamName + "\",\n" +
                "  \"siddhiAppName\": \"" + siddhiAppName + "\",\n" +
                "  \"timestamp\": \"" + timestamp + "\",\n" +
                "  \"data\": [\n" +
                message + ",\n" +
                "   0.0f,\n" +
                "   \"" + testName + "\"\n" +
                "  ]\n" +
                "}";

        return TestUtil.sendHRequest(queryBody, nodeURI, "/simulation/single",
                HEADER_CONTTP_TEXT, HTTP_POST, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
    }

    public static HTTPResponse sendEvent(URI nodeURI, String testName, String siddhiAppName, String streamName,
                                         String message, float price) throws IOException {

        String queryBody = "{\n" +
                "  \"streamName\": \"" + streamName + "\",\n" +
                "  \"siddhiAppName\": \"" + siddhiAppName + "\",\n" +
                "  \"timestamp\": null,\n" +
                "  \"data\": [\n" +
                message + ",\n" +
                price + ",\n" +
                "   \"" + testName + "\"\n" +
                "  ]\n" +
                "}";

        return TestUtil.sendHRequest(queryBody, nodeURI, "/simulation/single",
                HEADER_CONTTP_TEXT, HTTP_POST, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
    }

    public static void sendEvents(int count, URI uri, String testName, String siddhiAppName, String streamName,
                                  boolean withTimestamp)
            throws IOException {
        log.info("Sending " + count + " event(s) to " + uri);
        for (int i = 1; i <= count; i++) {
            if (i % 1000 == 0 && i != 0) {
                log.info(new Date() + " Number of events sent: " + i);
            }
            if (withTimestamp) {
                sendEvent(uri, testName, siddhiAppName, streamName, String.valueOf(i), String.valueOf(i));
            } else {
                sendEvent(uri, testName, siddhiAppName, streamName, String.valueOf(i), 0f);
            }
        }
    }
}
