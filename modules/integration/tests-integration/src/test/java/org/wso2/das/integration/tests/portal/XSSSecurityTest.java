/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.das.integration.tests.portal;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;
import org.wso2.das.integration.common.utils.DASIntegrationTest;
import org.wso2.das.integration.common.utils.TestConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.testng.Assert.assertTrue;

/**
 * Test the XSS Security vulnerabilities of portal app
 */
public class XSSSecurityTest extends DASIntegrationTest {
    /**
     * Initialize the test case
     *
     * @throws Exception
     */
    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
    }

    @Test(groups = "wso2.bam", description = "Test the login page for possible XSS")
    public void testXSSOnLoginPage() throws AutomationFrameworkException, IOException {
        String line;
        String hiddenValue = "";
        String destination = "destination=" +
                "%2Fportal%2F%22%3e%3c%73%43%72%49%70%54%3e%61%6c%65%72%74%28%38%35%32%38%31%" +
                "29%3c%2f%73%43%72%49%70%54%3e";
        String expectedDestinationValue = "/portal/sCrIpTalert85281/sCrIpT";
        String loginURL = TestConstants.ANALYTICS_PORTAL_ENDPOINT + "/login?";
        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(loginURL + destination);
        HttpResponse response = client.execute(get);
        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        while ((line = reader.readLine()) != null) {
            if (line.contains("name=\"destination\"")) {
                hiddenValue = line;
                break;
            }
        }
        assertTrue(hiddenValue.contains(expectedDestinationValue), "Login page is vulnerable to XSS");
    }
}