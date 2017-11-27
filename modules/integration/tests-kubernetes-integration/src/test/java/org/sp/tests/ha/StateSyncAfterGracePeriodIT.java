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

import static org.sp.tests.ha.util.SiddhiAppUtil.deployAggregateSiddhiApp;
import static org.sp.tests.ha.util.SiddhiAppUtil.sendEvent;
import static org.sp.tests.ha.util.TestCaseUtil.getExpectedEventsMessage;
import static org.sp.tests.ha.util.TestCaseUtil.getTestListener;
import static org.sp.tests.util.Constants.DEFAULT_PASSWORD;
import static org.sp.tests.util.Constants.DEFAULT_USER_NAME;
import static org.sp.tests.util.Constants.HEADER_CONTTP_TEXT;
import static org.sp.tests.util.Constants.HTTP_GET;
import static org.sp.tests.util.Constants.HTTP_POST;
import static org.sp.tests.util.Constants.HTTP_RESP_201;
import static org.sp.tests.util.Constants.HTTP_RESP_204;
import static org.sp.tests.util.TestUtil.waitThread;

public class StateSyncAfterGracePeriodIT extends SPBaseTest {
    private static final Logger log = Logger.getLogger(StateSyncIT.class);
    private static final String SIDDHI_APP_NAME = "TestMinimumHA";
    private static final String TEST_NAME = "cclassName:com.sp.test.StateSync";

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

        HTTPResponse httpResponseNodeTwo = deployAggregateSiddhiApp(nodeTwoURI, SIDDHI_APP_NAME);
        Assert.assertEquals(httpResponseNodeTwo.getResponseCode(), HTTP_RESP_201, httpResponseNodeTwo.getMessage());

        waitThread(4000); //Wait for siddhi application to deploy in active before deploying in passive

        HTTPResponse httpResponseNodeOne = deployAggregateSiddhiApp(nodeOneURI, SIDDHI_APP_NAME);
        Assert.assertEquals(httpResponseNodeOne.getResponseCode(), HTTP_RESP_201, httpResponseNodeOne.getMessage());

        waitThread(2000); //Wait for siddhi application to deploy

        sendEvent(nodeOneURI, TEST_NAME, SIDDHI_APP_NAME, "FooStream", "message", 100f);
        sendEvent(nodeOneURI, TEST_NAME, SIDDHI_APP_NAME, "FooStream", "message", 150f);
        sendEvent(nodeOneURI, TEST_NAME, SIDDHI_APP_NAME, "FooStream", "message", 90f);

        waitThread(65000); //Wait for passive node to sync with active node after 1 minute grace period

        super.runBashScript("ha-scripts", "shutdown-node-1-server.sh");

        sendEvent(nodeTwoURI, TEST_NAME, SIDDHI_APP_NAME, "FooStream", "message", 100f);

        getTestListener(getExpectedEventsMessage(TEST_NAME, "First", 20), msf4jBaseURI,
                "/testresults/com.sp.test.StateSyn/?eventIndex=" + 3).waitForResults(10, 1000);
        HTTPResponse msf4jResponse = TestUtil.sendHRequest("", msf4jBaseURI, "/testresults/" +
                        "com.sp.test.StateSyn/" + "?eventIndex=" + 3, HEADER_CONTTP_TEXT, HTTP_GET,
                DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(msf4jResponse.getMessage(), getExpectedEventsMessage(TEST_NAME, "message", 150));

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
