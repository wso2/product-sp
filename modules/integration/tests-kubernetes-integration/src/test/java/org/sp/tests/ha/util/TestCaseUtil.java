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

import org.sp.tests.util.HTTPResponse;
import org.sp.tests.util.TestResults;
import org.sp.tests.util.TestUtil;

import java.io.IOException;
import java.net.URI;

import static org.sp.tests.util.Constants.DEFAULT_PASSWORD;
import static org.sp.tests.util.Constants.DEFAULT_USER_NAME;
import static org.sp.tests.util.Constants.HEADER_CONTTP_TEXT;
import static org.sp.tests.util.Constants.HTTP_GET;

public class TestCaseUtil {

    public static TestResults getTestListener(String expectedResponse, URI msf4jBaseURI, String path) {

        return new TestResults() {

            @Override
            public void waitForResults(int retryCount, long interval) {
                HTTPResponse msf4jResponse = null;
                try {
                    msf4jResponse = TestUtil.sendHRequest("", msf4jBaseURI, path, HEADER_CONTTP_TEXT, HTTP_GET,
                            DEFAULT_USER_NAME, DEFAULT_PASSWORD);
                    super.verifyResult(interval, retryCount, msf4jResponse.getMessage());
                } catch (IOException e) {
                    TestUtil.handleException("Error in getting event count ", e);
                }
            }

            @Override
            public boolean resultsFound(String eventMessage) {
                return expectedResponse.equalsIgnoreCase(eventMessage);
            }
        };
    }

    public static String getExpectedEventsCountMessage(String testCase, int eventCount) {
        return "{\"testCase\":\"" + testCase + "\",\"eventCount\":" + Integer.toString(eventCount) + "}";
    }

    public static String getExpectedEventsMessage(String testCase, String data, float value) {
        return "{\"value\":" + String.valueOf(value) + ",\"message\":\""
                + data + "\",\"method\":\"POST\",\"headers\":\"" + testCase + "\"}";
    }
}
