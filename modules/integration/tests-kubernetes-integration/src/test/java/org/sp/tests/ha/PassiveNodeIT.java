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
import org.sp.tests.util.TestUtil;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;

import static org.sp.tests.ha.util.SiddhiAppUtil.deployPassThroughSiddhiApp;
import static org.sp.tests.ha.util.SiddhiAppUtil.sendEvent;
import static org.sp.tests.ha.util.SiddhiAppUtil.sendEvents;
import static org.sp.tests.ha.util.TestCaseUtil.getExpectedEventsCountMessage;
import static org.sp.tests.ha.util.TestCaseUtil.getTestListener;
import static org.sp.tests.util.Constants.DEFAULT_PASSWORD;
import static org.sp.tests.util.Constants.DEFAULT_USER_NAME;
import static org.sp.tests.util.Constants.HEADER_CONTTP_JSON;
import static org.sp.tests.util.Constants.HEADER_CONTTP_TEXT;
import static org.sp.tests.util.Constants.HTTP_GET;
import static org.sp.tests.util.Constants.HTTP_POST;
import static org.sp.tests.util.Constants.HTTP_RESP_200;
import static org.sp.tests.util.Constants.HTTP_RESP_201;
import static org.sp.tests.util.Constants.HTTP_RESP_204;
import static org.sp.tests.util.Constants.SINGLE_EVENT_SIMULATION_STARTED_SUCCESSFULLY;
import static org.sp.tests.util.TestUtil.waitThread;

public class PassiveNodeIT extends SPBaseTest {
    private static final Logger log = Logger.getLogger(PassiveNodeIT.class);
    private static final String SIDDHI_APP_NAME = "TestMinimumHA";
    private static final String TEST_NAME = "cclassName:com.sp.test.TwoNodeHa";
    public static final String TEST_RESULTS_COUNT_PATH = "/testresults/com.sp.test.TwoNodeH/count";

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
    public void testHaInit() throws IOException {

        HTTPResponse httpResponseNodeOne = deployPassThroughSiddhiApp(nodeOneURI, SIDDHI_APP_NAME);
        Assert.assertEquals(httpResponseNodeOne.getResponseCode(), HTTP_RESP_201, httpResponseNodeOne.getMessage());

        HTTPResponse httpResponseNodeTwo = deployPassThroughSiddhiApp(nodeTwoURI, SIDDHI_APP_NAME);
        Assert.assertEquals(httpResponseNodeTwo.getResponseCode(), HTTP_RESP_201, httpResponseNodeTwo.getMessage());

        waitThread(1000); //Wait for Siddhi applications to deploy

        // Checking if both Siddhi Applications are receiving events
        HTTPResponse nodeOneResponse = sendEvent(nodeOneURI, TEST_NAME, SIDDHI_APP_NAME, "FooStream", "First", "1");
        Assert.assertEquals(nodeOneResponse.getResponseCode(), HTTP_RESP_200, nodeOneResponse.getMessage());
        Assert.assertEquals(nodeOneResponse.getContentType(), HEADER_CONTTP_JSON);
        Assert.assertEquals(nodeOneResponse.getMessage(), SINGLE_EVENT_SIMULATION_STARTED_SUCCESSFULLY);
        waitThread(2000); //Wait for event to publish

        HTTPResponse nodeTwoResponse = sendEvent(nodeTwoURI, TEST_NAME, SIDDHI_APP_NAME, "FooStream", "First", "1");
        Assert.assertEquals(nodeTwoResponse.getResponseCode(), HTTP_RESP_200, nodeTwoResponse.getMessage());
        Assert.assertEquals(nodeTwoResponse.getContentType(), HEADER_CONTTP_JSON);
        Assert.assertEquals(nodeTwoResponse.getMessage(), SINGLE_EVENT_SIMULATION_STARTED_SUCCESSFULLY);
        waitThread(2000); //Wait for event to publish


        String expectedResult = "{\"value\":0.0,\"message\":\"First\",\"method\":\"POST\"," +
                "\"headers\":\"cclassName:com.sp.test.TwoNodeHa\"}";

        // Checking if the Msf4j Service is working properly by checking if test case is registered
        getTestListener(expectedResult, msf4jBaseURI, "/testresults/com.sp.test.TwoNodeH/?eventIndex=" + 0).
                waitForResults(10, 1000);
        HTTPResponse msf4jResponse = TestUtil.sendHRequest("", msf4jBaseURI, "/testresults/" +
                        "com.sp.test.TwoNodeH/" + "?eventIndex=" + 0, HEADER_CONTTP_TEXT, HTTP_GET,
                DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(msf4jResponse.getMessage(), expectedResult);

        // Resetting event count
        TestUtil.sendHRequest("", msf4jBaseURI, "/testresults/clear",
                HEADER_CONTTP_TEXT, HTTP_POST, DEFAULT_USER_NAME, DEFAULT_PASSWORD);

        // Begin Tests
        // Sending 10 events each to both nodes
        sendEvents(10, nodeOneURI, TEST_NAME, SIDDHI_APP_NAME, "FooStream", false);
        sendEvents(10, nodeTwoURI, TEST_NAME, SIDDHI_APP_NAME, "FooStream", false);

        getTestListener(getExpectedEventsCountMessage("com.sp.test.TwoNodeH", 10), msf4jBaseURI,
                TEST_RESULTS_COUNT_PATH).waitForResults(10, 5000);
        msf4jResponse = TestUtil.sendHRequest("", msf4jBaseURI, TEST_RESULTS_COUNT_PATH,
                HEADER_CONTTP_TEXT, HTTP_GET, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(msf4jResponse.getMessage(), getExpectedEventsCountMessage("com.sp.test.TwoNodeH", 10));

        // Sending 5 events to Active Node
        sendEvents(5, nodeOneURI, TEST_NAME, SIDDHI_APP_NAME, "FooStream", false);

        getTestListener(getExpectedEventsCountMessage("com.sp.test.TwoNodeH", 15), msf4jBaseURI,
                TEST_RESULTS_COUNT_PATH).waitForResults(10, 5000);
        msf4jResponse = TestUtil.sendHRequest("", msf4jBaseURI, TEST_RESULTS_COUNT_PATH,
                HEADER_CONTTP_TEXT, HTTP_GET, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(msf4jResponse.getMessage(), getExpectedEventsCountMessage("com.sp.test.TwoNodeH", 15));

        // Sending 5 events to Passive Node
        sendEvents(5, nodeTwoURI, TEST_NAME, SIDDHI_APP_NAME, "FooStream", false);

        waitThread(20000); // Waiting for Passive Node to Sync with Active Node
        getTestListener(getExpectedEventsCountMessage("com.sp.test.TwoNodeH", 15), msf4jBaseURI,
                TEST_RESULTS_COUNT_PATH).waitForResults(10, 5000);
        msf4jResponse = TestUtil.sendHRequest("", msf4jBaseURI, TEST_RESULTS_COUNT_PATH,
                HEADER_CONTTP_TEXT, HTTP_GET, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(msf4jResponse.getMessage(), getExpectedEventsCountMessage("com.sp.test.TwoNodeH", 15));

        // Shutting down Active Node. Passive Node will now change states
        super.runBashScript("ha-scripts", "undeploy-node-1.sh");
        waitThread(60000); // Waiting for node to go down

        getTestListener(getExpectedEventsCountMessage("com.sp.test.TwoNodeH", 20), msf4jBaseURI,
                TEST_RESULTS_COUNT_PATH).waitForResults(10, 5000);
        msf4jResponse = TestUtil.sendHRequest("", msf4jBaseURI, TEST_RESULTS_COUNT_PATH,
                HEADER_CONTTP_TEXT, HTTP_GET, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(msf4jResponse.getMessage(), getExpectedEventsCountMessage("com.sp.test.TwoNodeH", 20));
        if (msf4jResponse.getMessage().equals(getExpectedEventsCountMessage("com.sp.test.TwoNodeH", 15))) {
            log.error("Error");
        }
        // Starting a new Passive Node
        super.runBashScript("ha-scripts", "deploy-node-1.sh");
        waitThread(20000); //Waiting for node to be deployed

        httpResponseNodeOne = deployPassThroughSiddhiApp(nodeOneURI, SIDDHI_APP_NAME);
        Assert.assertEquals(httpResponseNodeOne.getResponseCode(), HTTP_RESP_201, httpResponseNodeOne.getMessage());
        waitThread(1000); //Wait for Siddhi applications to deploy

        sendEvents(10, nodeOneURI, TEST_NAME, SIDDHI_APP_NAME, "FooStream", false);
        sendEvents(5, nodeTwoURI, TEST_NAME, SIDDHI_APP_NAME, "FooStream", false);
        waitThread(500); //Make sure node one receives events after node two
        sendEvents(5, nodeOneURI, TEST_NAME, SIDDHI_APP_NAME, "FooStream", false);
        waitThread(2000); //Wait for events to publish

        getTestListener(getExpectedEventsCountMessage("com.sp.test.TwoNodeH", 25), msf4jBaseURI,
                TEST_RESULTS_COUNT_PATH).waitForResults(10, 5000);
        msf4jResponse = TestUtil.sendHRequest("", msf4jBaseURI, TEST_RESULTS_COUNT_PATH,
                HEADER_CONTTP_TEXT, HTTP_GET, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(msf4jResponse.getMessage(), getExpectedEventsCountMessage("com.sp.test.TwoNodeH", 25));

        log.info("Waiting for Sync");
        waitThread(20000); // Wait for Sync

        super.runBashScript("ha-scripts", "shutdown-node-2-server.sh");
        log.info("Waiting for node 2 to shutdown");
        waitThread(20000); // Waiting for node to go down

        sendEvents(2, nodeOneURI, TEST_NAME, SIDDHI_APP_NAME, "FooStream", false);

        getTestListener(getExpectedEventsCountMessage("com.sp.test.TwoNodeH", 32), msf4jBaseURI,
                TEST_RESULTS_COUNT_PATH).waitForResults(10, 5000);
        msf4jResponse = TestUtil.sendHRequest("", msf4jBaseURI, TEST_RESULTS_COUNT_PATH,
                HEADER_CONTTP_TEXT, HTTP_GET, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(msf4jResponse.getMessage(), getExpectedEventsCountMessage("com.sp.test.TwoNodeH", 32));

    }

    @AfterMethod
    public void afterMethod(ITestResult result) throws IOException {
        log.info("After Method Running!");
        if (result.getStatus() == ITestResult.FAILURE) {
            log.info("Found Errors. Printing Log");
            runBashScript("ha-scripts", "print-log.sh");
        }
    }

    @AfterTest
    public void tearDown() {
        log.info("Finishing test " + this.getClass().getCanonicalName());
        HTTPResponse msf4jClearResponse = null;
        try {
            msf4jClearResponse = TestUtil.sendHRequest("", msf4jBaseURI, "/testresults/clear",
                    HEADER_CONTTP_TEXT, HTTP_POST, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
            Assert.assertEquals(msf4jClearResponse.getResponseCode(), HTTP_RESP_204);
        } catch (IOException e) {
            TestUtil.handleException("IOException occurred when tearing down", e);
        }
    }
}
