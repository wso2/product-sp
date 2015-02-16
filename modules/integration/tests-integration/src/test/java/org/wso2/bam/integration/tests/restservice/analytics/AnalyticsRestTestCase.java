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

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.wso2.bam.analytics.rest.beans.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
public class AnalyticsRestTestCase extends BAMIntegrationTest {

    private static final Log log = LogFactory.getLog(AnalyticsRestTestCase.class);
    private static final String TABLE_NAME = "testtable";
    private static final String TABLE_NAME2 = "doesntExists";
    private static final String INDICES = "indices";
    private static final long ONE_HOUR_MILLISECOND = 3600000;
    private static final Gson gson = new Gson();
    private Map<String, String> indices;
    private Map<String, String> headers;
    private Map<String, Object> valueSet1;
    private Map<String, Object> valueSet2;
    private Map<String, Object> updateValueSet1;
    private Map<String, Object> updateValueSet2;
    private RecordBean record1;
    private RecordBean record2;
    private RecordBean record3;
    private RecordBean record4;
    private TableBean tableBean;
    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        super.init();
        headers = new HashMap<String, String>(1);
        headers.put("Content-Type", TestConstants.CONTENT_TYPE_JSON);
        headers.put("Accept", TestConstants.CONTENT_TYPE_JSON);
        
        indices = new HashMap<String, String>();
        indices.put("key1@", "STRING");
		indices.put("key2@", "STRING");
		indices.put("key3@", "STRING");
		indices.put("key4@", "STRING");
		indices.put("key5@", "STRING");
		
		valueSet1 = new LinkedHashMap<String, Object>();
		valueSet1.put("key1@", "@value1");
		valueSet1.put("key2@", "@value2");
		valueSet1.put("key3@", "@value3");
		valueSet1.put("key4@", "@value4");
		valueSet1.put("key5@", "@value5");

		valueSet2 = new LinkedHashMap<String, Object>();
		valueSet2.put("key7@", "@value1");
		valueSet2.put("key6@", "@value2");
		valueSet2.put("key9@", "@value3");
		valueSet2.put("key0@", "@value4");
		valueSet2.put("key4@", "@value5");
		
		updateValueSet1 = new LinkedHashMap<String, Object>();
		updateValueSet1.put("updatedkey7@", "updated@value1");
		updateValueSet1.put("updatedkey6@", "updated@value2");
		updateValueSet1.put("updatedkey9@", "updated@value3");
		updateValueSet1.put("updatedkey0@", "updated@value4");
		updateValueSet1.put("updatedkey4@", "updated@value5");
		
		updateValueSet2 = new LinkedHashMap<String, Object>();
		updateValueSet2.put("key1@", "@value1");
		updateValueSet2.put("key2@", "@value2");
		updateValueSet2.put("key3@", "@value3");
		updateValueSet2.put("key4@", "@value4");
		updateValueSet2.put("key5@", "@value5");
		
		record1 = new RecordBean();
		record1.setTableName(TABLE_NAME);
		record1.setValues(valueSet1);
		
		record2 = new RecordBean();
		record2.setTableName(TABLE_NAME);
		record2.setValues(valueSet2);
		
		record3 = new RecordBean();
		record3.setTableName(TABLE_NAME);
		record3.setValues(valueSet1);

		record4 = new RecordBean();
		record4.setTableName(TABLE_NAME);
		record4.setValues(valueSet2);
    }

    @Test(groups = "wso2.bam", description = "Create table")
    public void createTable() throws Exception {

        log.info("Executing create table test case ...");
        URL restUrl = new URL(TestConstants.ANALYTICS_TABLES_ENDPOINT_URL);
        tableBean = new TableBean();
        tableBean.setTableName(TABLE_NAME);
        HttpResponse response = HttpRequestUtil.doPost(restUrl,gson.toJson(tableBean), headers);
        log.info("Response: " + response.getData());
        Assert.assertEquals(response.getResponseCode(), 201, "Status code is different");
        Assert.assertTrue(response.getData().
                contains("Successfully created table: " + TABLE_NAME + " for tenantId: -1234"));
    }
    
    @Test(groups = "wso2.bam", description = "Checks if table exists", dependsOnMethods = "createTable")
    public void tableExists() throws Exception {

        log.info("Executing Table Exist test case ...");
        StringBuilder restUrl = new StringBuilder();
        restUrl.append(TestConstants.ANALYTICS_TABLES_ENDPOINT_URL);
        restUrl.append(TABLE_NAME);
        HttpResponse response = HttpRequestUtil.doGet(restUrl.toString(), headers);
        
        log.info("Response: " + response.getData());
        Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
    }
    
    @Test(groups = "wso2.bam", description = "Checks if table doesnt exists", dependsOnMethods = "createTable")
    public void tableNotExists() throws Exception {

        log.info("Executing Table Not Exist test case ...");
        StringBuilder restUrl = new StringBuilder();
        restUrl.append(TestConstants.ANALYTICS_TABLES_ENDPOINT_URL);
        restUrl.append(TABLE_NAME2);
        HttpResponse response = HttpRequestUtil.doGet(restUrl.toString(), headers);
        
        log.info("Response: " + response.getData());
        Assert.assertEquals(response.getResponseCode(), 404, "Status code is different");
    }
    
    @Test(groups = "wso2.bam", description = "lists all the tables", dependsOnMethods = "createTable")
    public void getAllTables() throws Exception {

        log.info("Executing getAllTables test case ...");
        StringBuilder restUrl = new StringBuilder();
        restUrl.append(TestConstants.ANALYTICS_TABLES_ENDPOINT_URL);
        HttpResponse response = HttpRequestUtil.doGet(restUrl.toString(), headers);
        
        log.info("Response: " + response.getData());
        Type listType = new TypeToken<List<String>>(){}.getType();
        List< String> tableNames = gson.fromJson(response.getData(), listType);
        Assert.assertEquals(tableNames.size(), 1, "Number of tables is different");
        Assert.assertEquals(tableNames.get(0).toLowerCase(), "testtable");
        Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
        
    }
    
    @Test(groups = "wso2.bam", description = "Create indices for the table", dependsOnMethods = "tableExists")
	public void createIndices() throws Exception {

		log.info("Executing create indices test case ...");
		URL restUrl = new URL(TestConstants.ANALYTICS_TABLES_ENDPOINT_URL + TABLE_NAME + "/" + INDICES);
		HttpResponse response = HttpRequestUtil.doPost(restUrl, gson.toJson(indices), headers);
		log.info("Response: " + response.getData());
		Assert.assertEquals(response.getResponseCode(), 201, "Status code is different");
		Assert.assertTrue(response.getData().contains("created"));
	}
    
    @Test(groups = "wso2.bam", description = "Checks if table exists", dependsOnMethods = "createIndices")
    public void getIndices() throws Exception {

        log.info("Executing Get Indices test case ...");
        StringBuilder restUrl = new StringBuilder();
        restUrl.append(TestConstants.ANALYTICS_TABLES_ENDPOINT_URL + TABLE_NAME + "/" + INDICES );
        HttpResponse response = HttpRequestUtil.doGet(restUrl.toString(), headers);
        Type mapType = new TypeToken<Map<String, String>>(){}.getType();
        Map<String, String> indicesMap = gson.fromJson(response.getData(), mapType);
		log.info("Response: " + indicesMap);
		Assert.assertTrue(indicesMap.entrySet().containsAll(indices.entrySet()),
		                  "Returned set is a subset of reference set");
		Assert.assertTrue(indices.entrySet().containsAll(indicesMap.entrySet()),
                "reference set is a subset of returned set");
        Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
    }

    @Test(groups = "wso2.bam", description = "Create records without optional paramters", dependsOnMethods = "getIndices")
	public void createRecordsWithoutOptionalParams() throws Exception {

		log.info("Executing create records without Optional Parameters test case ...");
		URL restUrl = new URL(TestConstants.ANALYTICS_RECORDS_ENDPOINT_URL);

		List<RecordBean> recordList = new ArrayList<RecordBean>();
		recordList.add(record1);
		recordList.add(record2);

		HttpResponse response = HttpRequestUtil.doPost(restUrl, gson.toJson(recordList), headers);
		log.info("Response: " + response.getData());
		Assert.assertEquals(response.getResponseCode(), 201, "Status code is different");
		Assert.assertTrue(response.getData().contains("Successfully added records"));
	}
    
    @Test(groups = "wso2.bam", description = "Create records with optional params", dependsOnMethods = "createRecordsWithoutOptionalParams")
    public void createRecordsWithOptionalParams() throws Exception {

        log.info("Executing create records test case ...");
        long currentTime = System.currentTimeMillis();
        URL restUrl = new URL(TestConstants.ANALYTICS_RECORDS_ENDPOINT_URL);
        List<RecordBean> recordList = new ArrayList<RecordBean>();
        
        record3.setId("id1");
        record3.setTableName(TABLE_NAME);
        record3.setTimestamp(currentTime);

        record4.setId("id2");
        record4.setTableName(TABLE_NAME);
        record4.setTimestamp(currentTime);

        recordList.add(record3);
        recordList.add(record4);
        
        HttpResponse response = HttpRequestUtil.doPost(restUrl, gson.toJson(recordList), headers);
        log.info("Response: " + response.getData());
        Assert.assertEquals(response.getResponseCode(), 201, "Status code is different");
        Assert.assertTrue(response.getData().contains("Successfully added records"));
    }
    
    @Test(groups = "wso2.bam", description = "Get the record count of a table", dependsOnMethods = "createRecordsWithOptionalParams")
    public void getRecordCount() throws Exception {

        log.info("Executing getRecordCount test case ...");
        StringBuilder restUrl = new StringBuilder();
        restUrl.append(TestConstants.ANALYTICS_TABLES_ENDPOINT_URL);
        restUrl.append(TABLE_NAME);
        restUrl.append("/recordcount");
        HttpResponse response = HttpRequestUtil.doGet(restUrl.toString(), headers);
        
        log.info("Response: " + response.getData());
        Assert.assertEquals(response.getData(), "4", "record count is different");
        Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
        
    }

    @Test(groups = "wso2.bam", description = "Get records without pagination", dependsOnMethods = "createRecordsWithoutOptionalParams")
    public void getRecordsWithoutPagination() throws Exception {

        log.info("Executing get records without pagination test case ...");
        long currentTime = System.currentTimeMillis();
        StringBuilder restUrl = new StringBuilder();
        restUrl.append(TestConstants.ANALYTICS_TABLES_ENDPOINT_URL);
        restUrl.append(TABLE_NAME);
        restUrl.append("/");
        restUrl.append(currentTime - ONE_HOUR_MILLISECOND);
        restUrl.append("/");
        restUrl.append(currentTime + ONE_HOUR_MILLISECOND);
        HttpResponse response = HttpRequestUtil.doGet(restUrl.toString(), headers);
        
        Type listType = new TypeToken<List<RecordBean>>(){}.getType();
        List< RecordBean> recordList = gson.fromJson(response.getData(), listType);
		Assert.assertTrue(recordList.size() == 4,
		                  "Size mismatch!");
		
        Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
    }
    
    @Test(groups = "wso2.bam", description = "Get records with pagination", dependsOnMethods = "createRecordsWithOptionalParams")
    public void getRecordsWithPagination() throws Exception {

        log.info("Executing get records with pagination test case ...");
        long currentTime = System.currentTimeMillis();
        StringBuilder restUrl = new StringBuilder();
        restUrl.append(TestConstants.ANALYTICS_TABLES_ENDPOINT_URL);
        restUrl.append(TABLE_NAME);
        restUrl.append("/");
        restUrl.append(currentTime - ONE_HOUR_MILLISECOND);
        restUrl.append("/");
        restUrl.append(currentTime + ONE_HOUR_MILLISECOND);
        restUrl.append("/");
        restUrl.append("0");
        restUrl.append("/");
        restUrl.append("2");
        HttpResponse response = HttpRequestUtil.doGet(restUrl.toString(), headers);
        
        log.info("Response: " + response.getData());
        Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
        Assert.assertTrue(response.getData().contains("\"values\":{\"key1@\":\"@value1\",\"key2@\":\"@value2\"," + 
        			"\"key3@\":\"@value3\",\"key4@\":\"@value4\",\"key5@\":\"@value5\"}"));
        Assert.assertTrue(response.getData().contains("\"values\":{\"key7@\":\"@value1\",\"key6@\":\"@value2\"," + 
    			"\"key9@\":\"@value3\",\"key0@\":\"@value4\",\"key4@\":\"@value5\"}"));
    }
    
    @Test(groups = "wso2.bam", description = "Get all records", dependsOnMethods = "createRecordsWithoutOptionalParams")
    public void getAllRecords() throws Exception {

        log.info("Executing get All records test case ...");
        StringBuilder restUrl = new StringBuilder();
        restUrl.append(TestConstants.ANALYTICS_TABLES_ENDPOINT_URL);
        restUrl.append(TABLE_NAME);
        HttpResponse response = HttpRequestUtil.doGet(restUrl.toString(), headers);  
        Type listType = new TypeToken<List<RecordBean>>(){}.getType();
        List< RecordBean> recordList = gson.fromJson(response.getData(), listType);
		Assert.assertTrue(recordList.size() == 4,
		                  "Size mismatch!");
		
        Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
    }
    
  //  @Test(groups = "wso2.bam", description = "update existing records", dependsOnMethods = "getRecordCount")
    public void updateRecords() throws Exception {

        log.info("Executing updateRecords test case ...");
        URL restUrl = new URL(TestConstants.ANALYTICS_RECORDS_ENDPOINT_URL);
        List<RecordBean> recordList = new ArrayList<RecordBean>();
        
        record3.setId("id1");
        record3.setTableName(TABLE_NAME);
        record3.setValues(updateValueSet1);
        record4.setId("id2");
        record4.setTableName(TABLE_NAME);
        record4.setValues(updateValueSet2);
        
        recordList.add(record3);
        recordList.add(record4);
        StringReader reader = new StringReader(gson.toJson(recordList));
        StringWriter writer =  new StringWriter();
        HttpRequestUtil.sendPutRequest(reader, restUrl, writer, MediaType.APPLICATION_JSON);
        String response = writer.toString();
        log.info("Response: " + response);
        Assert.assertTrue(response.contains("" + Status.OK.getStatusCode()), "status code is different");
        Assert.assertTrue(response.contains("Successfully updated records"), "status message is different");
    }
}
