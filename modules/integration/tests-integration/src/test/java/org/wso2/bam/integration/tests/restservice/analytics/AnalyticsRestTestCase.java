package org.wso2.bam.integration.tests.restservice.analytics;

/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.bam.integration.common.utils.BAMIntegrationTest;
import org.wso2.bam.integration.common.utils.TestConstants;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AnalyticsRestTestCase extends BAMIntegrationTest {

    private static final Log log = LogFactory.getLog(AnalyticsRestTestCase.class);
    private static final String TABLE_NAME = "testtable";
    private static final long ONE_HOUR_MILLISECOND = 3600000;

    private Map<String, String> headers;


    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        super.init();
        headers = new HashMap<String, String>(1);
        headers.put("Content-Type", TestConstants.CONTENT_TYPE_JSON);
    }

    @Test(groups = "wso2.bam", description = "Create table")
    public void createTable() throws Exception {

        log.info("Executing create table test case ...");
        URL restUrl = new URL(TestConstants.ANALYTICS_ENDPOINT_URL + TABLE_NAME);

        HttpResponse response = HttpRequestUtil.doPost(restUrl, "", headers);

        Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
        Assert.assertTrue(response.getData().
                contains("Successfully created table: " + TABLE_NAME + " for tenantId: -1234"));

        log.info("Response: " + response.getData());
    }

    @Test(groups = "wso2.bam", description = "Create records", dependsOnMethods = "createTable")
    public void createRecords() throws Exception {

        log.info("Executing create records test case ...");
        URL restUrl = new URL(TestConstants.ANALYTICS_RECORD_ENDPOINT_URL);
        long currentTime = System.currentTimeMillis();

        String payload = "[\n" +
                         "    {\n" +
                         "        \"id\": \"sample1\",\n" +
                         "        \"tableName\": \"" + TABLE_NAME + "\",\n" +
                         "        \"timestamp\": " + currentTime + ",\n" +
                         "        \"values\": \n" +
                         "        {\n" +
                         "            \"timestamp\": \"2015-01-22 12:30:49,264\",\n" +
                         "            \"message\"  : \"File input event adaptor loading listeners \",\n" +
                         "            \"product\"  : \"wso2am\",\n" +
                         "            \"level\"    : \"INFO\",\n" +
                         "            \"component\": \"org.wso2.carbon.event.input.adaptor.file.FileEventAdaptorType\"\n" +
                         "        }\n" +
                         "\n" +
                         "    },\n" +
                         "\n" +
                         "{\n" +
                         "        \"id\": \"sample2\",\n" +
                         "        \"tableName\": \"" + TABLE_NAME + "\",\n" +
                         "        \"timestamp\": " + currentTime + ",\n" +
                         "        \"values\": \n" +
                         "        {\n" +
                         "            \"timestamp\": \"2015-01-22 12:30:49,264\",\n" +
                         "            \"message\"  : \"File input event adaptor loading listeners \",\n" +
                         "            \"product\"  : \"wso2am\",\n" +
                         "            \"level\"    : \"INFO\",\n" +
                         "            \"component\": \"org.wso2.carbon.event.input.adaptor.file.FileEventAdaptorType\"\n" +
                         "        }\n" +
                         "\n" +
                         "    }\n" +
                         "]";

        HttpResponse response = HttpRequestUtil.doPost(restUrl, payload.trim(), headers);

        Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
        Assert.assertTrue(response.getData().contains("Successfully added records"));

        log.info("Response: " + response.getData());
    }

    @Test(groups = "wso2.bam", description = "Get records", dependsOnMethods = "createRecords")
    public void getRecordsWithoutPagination() throws Exception {

        log.info("Executing create records test case ...");
        long currentTime = System.currentTimeMillis();
        StringBuilder restUrl = new StringBuilder();
        restUrl.append(TestConstants.ANALYTICS_RECORD_ENDPOINT_URL);
        restUrl.append(TABLE_NAME);
        restUrl.append("/");
        restUrl.append(currentTime - ONE_HOUR_MILLISECOND);
        restUrl.append("/");
        restUrl.append(currentTime + ONE_HOUR_MILLISECOND);

        HttpResponse response = HttpRequestUtil.doGet(restUrl.toString(), headers);

        Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
        Assert.assertTrue(response.getData().contains("\"values\":{\"product\":\"wso2am\",\"message\":\"File input event adaptor loading listeners \",\"timestamp\":\"2015-01-22 12:30:49,264\",\"level\":\"INFO\",\"component\":\"org.wso2.carbon.event.input.adaptor.file.FileEventAdaptorType\"}"));

        log.info("Response: " + response.getData());
    }
}
