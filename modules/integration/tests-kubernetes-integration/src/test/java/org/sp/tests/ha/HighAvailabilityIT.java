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

package org.sp.tests.ha;

import org.apache.log4j.Logger;
import org.sp.tests.base.SPBaseTest;
import org.sp.tests.util.HTTPResponse;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.sp.tests.ha.util.SiddhiAppUtil.deployPassThroughSiddhiApp;
import static org.sp.tests.ha.util.SiddhiAppUtil.sendEvent;
import static org.sp.tests.ha.util.SiddhiAppUtil.sendEvents;
import static org.sp.tests.util.Constants.DEFAULT_PASSWORD;
import static org.sp.tests.util.Constants.DEFAULT_USER_NAME;
import static org.sp.tests.util.Constants.HEADER_CONTTP_JSON;
import static org.sp.tests.util.Constants.HEADER_CONTTP_TEXT;
import static org.sp.tests.util.Constants.HTTP_GET;
import static org.sp.tests.util.Constants.HTTP_RESP_200;
import static org.sp.tests.util.Constants.HTTP_RESP_201;
import static org.sp.tests.util.Constants.SINGLE_EVENT_SIMULATION_STARTED_SUCCESSFULLY;
import static org.sp.tests.util.TestUtil.sendHRequest;
import static org.sp.tests.util.TestUtil.waitThread;

public class HighAvailabilityIT extends SPBaseTest {
    private static final Logger log = Logger.getLogger(HighAvailabilityIT.class);
    private static final String SIDDHI_APP_NAME = "TestMinimumHA";
    private static final String TEST_NAME = "cclassName:com.sp.test.HighAvailabilityIT";

    private URI nodeOneURI;
    private URI nodeTwoURI;
    private URI msf4jBaseURI;

    @BeforeTest
    public void testInit() {
        log.info("Starting test " + this.getClass().getCanonicalName());
        nodeOneURI = URI.create(haNodeOneURL);
        nodeTwoURI = URI.create(haNodeTwoURL);
        msf4jBaseURI = URI.create(haNodeTwoMsf4jURL);
    }

    @Test
    public void haFailOverTest() throws IOException {

        HTTPResponse httpResponseNodeOne = deployPassThroughSiddhiApp(nodeOneURI, SIDDHI_APP_NAME);
        Assert.assertEquals(httpResponseNodeOne.getResponseCode(), HTTP_RESP_201, httpResponseNodeOne.getMessage());

        HTTPResponse httpResponseNodeTwo = deployPassThroughSiddhiApp(nodeTwoURI, SIDDHI_APP_NAME);
        Assert.assertEquals(httpResponseNodeTwo.getResponseCode(), HTTP_RESP_201, httpResponseNodeTwo.getMessage());

        waitThread(1000); //Wait for Siddhi applications to deploy

        // Checking if both Siddhi Applications are receiving events
        HTTPResponse sendEventResponseNodeOne = sendEvent(nodeOneURI, TEST_NAME, SIDDHI_APP_NAME, "FooStream",
                "First", "1");

        Assert.assertEquals(sendEventResponseNodeOne.getResponseCode(), HTTP_RESP_200,
                sendEventResponseNodeOne.getMessage());
        Assert.assertEquals(sendEventResponseNodeOne.getContentType(), HEADER_CONTTP_JSON);
        Assert.assertEquals(sendEventResponseNodeOne.getMessage(), SINGLE_EVENT_SIMULATION_STARTED_SUCCESSFULLY);
        waitThread(2000); //Wait for event to publish

        HTTPResponse sendEventResponseNodeTwo = sendEvent(nodeTwoURI, TEST_NAME, SIDDHI_APP_NAME, "FooStream",
                "First", "1");

        Assert.assertEquals(sendEventResponseNodeTwo.getResponseCode(), HTTP_RESP_200,
                sendEventResponseNodeTwo.getMessage());
        Assert.assertEquals(sendEventResponseNodeTwo.getContentType(), HEADER_CONTTP_JSON);
        Assert.assertEquals(sendEventResponseNodeTwo.getMessage(),
                SINGLE_EVENT_SIMULATION_STARTED_SUCCESSFULLY);
        waitThread(2000); //Wait for event to publish

        EventSender eventSenderOne = new EventSender(nodeOneURI, TEST_NAME, SIDDHI_APP_NAME);
        EventSender eventSenderTwo = new EventSender(nodeTwoURI, TEST_NAME, SIDDHI_APP_NAME);
        eventSenderOne.start();
        eventSenderTwo.start();

        waitThread(40000); //Wait before shutting down node 1

        super.runBashScript("ha-scripts", "shutdown-node-1-server.sh");
        while (eventSenderTwo.isAlive()) {
            waitThread(5000);
        }

        log.info("Waiting for events to finish publishing");
        waitThread(5000);
        HTTPResponse msf4jResponse = sendHRequest("", msf4jBaseURI,
                "/testresults/com.sp.test.HighAvailabilityI/count",
                HEADER_CONTTP_TEXT, HTTP_GET, DEFAULT_USER_NAME, DEFAULT_PASSWORD);

        Pattern p = Pattern.compile("-?\\d+");
        Matcher m = p.matcher(msf4jResponse.getMessage());
        if (m.find()) {
            Integer eventCount = Integer.valueOf(m.group());
            Assert.assertTrue(eventCount >= 10000);
        } else {
            Assert.fail("Error in getting event count");
        }

    }
}

class EventSender extends Thread {

    private final URI nodeURI;
    private final String testName;
    private final String siddhiAppName;

    EventSender(URI nodeURI, String testName, String siddhiAppName) {
        this.nodeURI = nodeURI;
        this.testName = testName;
        this.siddhiAppName = siddhiAppName;
    }

    @Override
    public void run() {
        try {
            sendEvents(10000, nodeURI, testName, siddhiAppName, "FooStream", true);
        } catch (IOException e) {
            this.stop();
        }
    }
}

