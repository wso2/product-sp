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

package org.wso2.bam.integration.tests.restservice.analytics;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.wso2.bam.analytics.rest.beans.*;

import com.google.gson.Gson;
public class AnalyticsRestTestCase extends BAMIntegrationTest {

    private static final Log log = LogFactory.getLog(AnalyticsRestTestCase.class);
    private static final String TABLE_NAME = "testtable";
    private static final long ONE_HOUR_MILLISECOND = 3600000;
    private static final Gson gson = new Gson();
    
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
        log.info("Response: " + response.getData());
        Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
        Assert.assertTrue(response.getData().
                contains("Successfully created table: " + TABLE_NAME + " for tenantId: -1234"));
    }

    @Test(groups = "wso2.bam", description = "Create records without optional paramters", dependsOnMethods = "createTable")
	public void createRecordsWithoutOptionalParams() throws Exception {

		log.info("Executing create records without Optional Parameters test case ...");
		URL restUrl = new URL(TestConstants.ANALYTICS_RECORD_ENDPOINT_URL);

		List<RecordBean> recordList = new ArrayList<RecordBean>();
		Map<String, Object> values = new HashMap<String, Object>();

		RecordBean record1 = new RecordBean();
		record1.setTableName(TABLE_NAME);
		values.put("key1", "value1");
		values.put("key2", "value2");
		values.put("key3", "value3");
		values.put("key4", "value4");
		values.put("key5", "value5");
		record1.setValues(values);

		values = new HashMap<String, Object>();
		RecordBean record2 = new RecordBean();
		record2.setTableName(TABLE_NAME);
		values.put("key7", "value1");
		values.put("key6", "value2");
		values.put("key9", "value3");
		values.put("key0", "value4");
		values.put("key4", "value5");
		record2.setValues(values);

		recordList.add(record1);
		recordList.add(record2);

		HttpResponse response = HttpRequestUtil.doPost(restUrl, gson.toJson(recordList), headers);
		log.info("Response: " + response.getData());
		Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
		Assert.assertTrue(response.getData().contains("Successfully added records"));
	}
    
    @Test(groups = "wso2.bam", description = "Create records with optional params", dependsOnMethods = "createTable")
    public void createRecordsWithOptionalParams() throws Exception {

        log.info("Executing create records test case ...");
        long currentTime = System.currentTimeMillis();
        URL restUrl = new URL(TestConstants.ANALYTICS_RECORD_ENDPOINT_URL);
        
        List<RecordBean> recordList = new ArrayList<RecordBean>();
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        
        RecordBean  record1 = new RecordBean();
        record1.setId("id1");
        record1.setTableName(TABLE_NAME);
        record1.setTimestamp(currentTime);
        values.put("key1", "value1");
        values.put("key2", "value2");
        values.put("key3", "value3");
        values.put("key4", "value4");
        values.put("key5", "value5");
        record1.setValues(values);
        
        values = new LinkedHashMap<String, Object>();
        RecordBean  record2 = new RecordBean();
        record2.setId("id2");
        record2.setTableName(TABLE_NAME);
        record2.setTimestamp(currentTime);
        values.put("key7", "value1");
        values.put("key6", "value2");
        values.put("key9", "value3");
        values.put("key0", "value4");
        values.put("key4", "value5");
        record2.setValues(values);
        
        recordList.add(record1);
        recordList.add(record2);
        
        HttpResponse response = HttpRequestUtil.doPost(restUrl, gson.toJson(recordList), headers);
        log.info("Response: " + response.getData());
        Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
        Assert.assertTrue(response.getData().contains("Successfully added records"));
    }

    @Test(groups = "wso2.bam", description = "Get records", dependsOnMethods = "createRecordsWithOptionalParams")
    public void getRecordsWithoutPagination() throws Exception {

        log.info("Executing get records test case ...");
        long currentTime = System.currentTimeMillis();
        StringBuilder restUrl = new StringBuilder();
        restUrl.append(TestConstants.ANALYTICS_RECORD_ENDPOINT_URL);
        restUrl.append(TABLE_NAME);
        restUrl.append("/");
        restUrl.append(currentTime - ONE_HOUR_MILLISECOND);
        restUrl.append("/");
        restUrl.append(currentTime + ONE_HOUR_MILLISECOND);
        HttpResponse response = HttpRequestUtil.doGet(restUrl.toString(), headers);
        
        log.info("Response: " + response.getData());
        Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
        Assert.assertTrue(response.getData().contains("\"values\":{\"key1\":\"value1\",\"key2\":\"value2\"," + 
        			"\"key3\":\"value3\",\"key4\":\"value4\",\"key5\":\"value5\"}"));
    }
}
