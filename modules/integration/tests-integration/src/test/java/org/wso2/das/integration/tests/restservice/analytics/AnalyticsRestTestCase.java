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

package org.wso2.das.integration.tests.restservice.analytics;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.das.analytics.rest.beans.AnalyticsSchemaBean;
import org.wso2.das.analytics.rest.beans.ColumnDefinitionBean;
import org.wso2.das.analytics.rest.beans.ColumnTypeBean;
import org.wso2.das.analytics.rest.beans.QueryBean;
import org.wso2.das.analytics.rest.beans.RecordBean;
import org.wso2.das.analytics.rest.beans.TableBean;
import org.wso2.das.integration.common.utils.BAMIntegrationTest;
import org.wso2.das.integration.common.utils.TestConstants;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
public class AnalyticsRestTestCase extends BAMIntegrationTest {

    private static final Log log = LogFactory.getLog(AnalyticsRestTestCase.class);
    private static final String TABLE_NAME = "testtable";
    private static final String TABLE_NAME2 = "doesntExists";
    private static final String INDICES = "indexData";
    private static final long ONE_HOUR_MILLISECOND = 3600000;
    private static final Gson gson = new Gson();
    private Map<String, ColumnDefinitionBean> indices;
    private AnalyticsSchemaBean schemaBean;
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
        headers = new HashMap<>();
        headers.put("Content-Type", TestConstants.CONTENT_TYPE_JSON);
        headers.put("Accept", TestConstants.CONTENT_TYPE_JSON);
        headers.put("Authorization", TestConstants.BASE64_ADMIN_ADMIN);

        indices = new HashMap<>();
        indices.put("key1@", new ColumnDefinitionBean(ColumnTypeBean.STRING, true, false));
		indices.put("key2@", new ColumnDefinitionBean(ColumnTypeBean.STRING, true, false));
		indices.put("key3", new ColumnDefinitionBean(ColumnTypeBean.STRING, true, false));
		indices.put("key4@", new ColumnDefinitionBean(ColumnTypeBean.STRING, true, false));
		indices.put("key5@", new ColumnDefinitionBean(ColumnTypeBean.STRING, true, false));
		indices.put("IndexedKey", new ColumnDefinitionBean(ColumnTypeBean.STRING, true, false));

        schemaBean = new AnalyticsSchemaBean(indices, null);

		valueSet1 = new LinkedHashMap<String, Object>();
		valueSet1.put("key1@", "@value1");
		valueSet1.put("key2@", "@value2");
		valueSet1.put("key3", "value3");
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
		updateValueSet1.put("IndexedKey", "IndexedValue");
		updateValueSet1.put("updatedkey0@", "updated@value4");
		updateValueSet1.put("updatedkey4@", "updated@value5");
		
		updateValueSet2 = new LinkedHashMap<String, Object>();
		updateValueSet2.put("key1@", "@value1");
		updateValueSet2.put("key2@", "@value2");
		updateValueSet2.put("key3", "value3");
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

    @Test(groups = "wso2.das", description = "Create table")
    public void createTable() throws Exception {

        log.info("Executing create table test case ...");
        URL restUrl = new URL(TestConstants.ANALYTICS_TABLES_ENDPOINT_URL);
        tableBean = new TableBean();
        tableBean.setTableName(TABLE_NAME);
        HttpResponse response = HttpRequestUtil.doPost(restUrl,gson.toJson(tableBean), headers);
        log.info("Response: " + response.getData());
        Assert.assertEquals(response.getResponseCode(), 201, "Status code is different");
        Assert.assertTrue(response.getData().
                contains("Successfully created table: " + TABLE_NAME));
    }
    
    @Test(groups = "wso2.das", description = "Checks if table exists", dependsOnMethods = "createTable")
    public void tableExists() throws Exception {

        log.info("Executing Table Exist test case ...");
        StringBuilder restUrl = new StringBuilder();
        restUrl.append(TestConstants.ANALYTICS_ENDPOINT_URL);
        restUrl.append(TestConstants.TABLE_EXISTS);
        restUrl.append(TABLE_NAME);
        HttpResponse response = HttpRequestUtil.doGet(restUrl.toString(), headers);
        
        log.info("Response: " + response.getData());
        Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
    }
    
    @Test(groups = "wso2.das", description = "Checks if table doesnt exist", dependsOnMethods = "createTable")
    public void tableNotExist() throws Exception {

        log.info("Executing TableNotExist test case ...");
        StringBuilder restUrl = new StringBuilder();
        restUrl.append(TestConstants.ANALYTICS_ENDPOINT_URL);
        restUrl.append(TestConstants.TABLE_EXISTS);
        restUrl.append(TABLE_NAME2);
        HttpResponse response = HttpRequestUtil.doGet(restUrl.toString(), headers);
        
        log.info("Response: " + response.getData());
        Assert.assertEquals(response.getResponseCode(), 404, "Status code is different");
    }
    
    @Test(groups = "wso2.das", description = "lists all the tables", dependsOnMethods = "createTable")
    public void getAllTables() throws Exception {

        log.info("Executing getAllTables test case ...");
        StringBuilder restUrl = new StringBuilder();
        restUrl.append(TestConstants.ANALYTICS_TABLES_ENDPOINT_URL);
        HttpResponse response = HttpRequestUtil.doGet(restUrl.toString(), headers);
        
        log.info("Response: " + response.getData());
        Type listType = new TypeToken<List<String>>(){}.getType();
        List< String> tableNames = gson.fromJson(response.getData(), listType);
        Assert.assertTrue(tableNames.contains("testtable".toUpperCase()), "Table : testtable not found");
        Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
        
    }

    @Test(groups = "wso2.das", description = "Create table schema", dependsOnMethods = "createTable")
    public void setTableSchema() throws Exception {

        log.info("Executing createTableSchema test case ...");
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(TestConstants.ANALYTICS_TABLES_ENDPOINT_URL);
        urlBuilder.append(TABLE_NAME);
        urlBuilder.append(TestConstants.SCHEMA);
        URL restUrl = new URL(urlBuilder.toString());
        HttpResponse response = HttpRequestUtil.doPost(restUrl, gson.toJson(schemaBean), headers);
        log.info("Response: " + response.getData());
        Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
    }

    @Test(groups = "wso2.das", description = "Create records without optional paramters", dependsOnMethods = "setTableSchema")
	public void createRecordsWithoutOptionalParams() throws Exception {

		log.info("Executing create records without Optional Parameters test case ...");
		URL restUrl = new URL(TestConstants.ANALYTICS_RECORDS_ENDPOINT_URL);

		List<RecordBean> recordList = new ArrayList<RecordBean>();
		recordList.add(record1);
		recordList.add(record2);

		HttpResponse response = HttpRequestUtil.doPost(restUrl, gson.toJson(recordList), headers);
		log.info("Response: " + response.getData());
		Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
		Assert.assertFalse(response.getData().contains("[]"));
	}
    
    @Test(groups = "wso2.das", description = "Create records with optional params", dependsOnMethods = "createRecordsWithoutOptionalParams")
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
        Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
        Assert.assertTrue(response.getData().contains("id1"));
        Assert.assertTrue(response.getData().contains("id2"));
    }
    
    @Test(groups = "wso2.das", description = "Get the record count of a table", dependsOnMethods = "createRecordsWithOptionalParams")
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

    @Test(groups = "wso2.das", description = "Get records without pagination", dependsOnMethods = "createRecordsWithoutOptionalParams")
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
    
    @Test(groups = "wso2.das", description = "Get records with pagination", dependsOnMethods = "createRecordsWithOptionalParams")
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
        			"\"key3\":\"value3\",\"key4@\":\"@value4\",\"key5@\":\"@value5\"}"));
        Assert.assertTrue(response.getData().contains("\"values\":{\"key7@\":\"@value1\",\"key6@\":\"@value2\"," + 
    			"\"key9@\":\"@value3\",\"key0@\":\"@value4\",\"key4@\":\"@value5\"}"));
    }


    @Test(groups = "wso2.das", description = "Get all records", dependsOnMethods = "createRecordsWithoutOptionalParams")
    public void getAllRecords() throws Exception {

        log.info("Executing get All records test case ...");
        StringBuilder restUrl = new StringBuilder();
        restUrl.append(TestConstants.ANALYTICS_TABLES_ENDPOINT_URL);
        restUrl.append(TABLE_NAME);
        HttpResponse response = HttpRequestUtil.doGet(restUrl.toString(), headers);  
        Type listType = new TypeToken<List<RecordBean>>(){}.getType();
        List< RecordBean> recordList = gson.fromJson(response.getData(), listType);
        log.info("Response :" + response.getData());
		Assert.assertTrue(recordList.size() == 4,
		                  "Size mismatch!");
		
        Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
    }
    
    @Test(groups = "wso2.das", description = "update existing records", dependsOnMethods = "getRecordCount")
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
        HttpResponse response = HttpRequestUtil.doPost(restUrl, gson.toJson(recordList), headers);
		log.info("Response: " + response.getData());
		Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
		Assert.assertTrue(response.getData().contains("id1"));
		Assert.assertTrue(response.getData().contains("id2"));
    }
    
    @Test(groups = "wso2.das", description = "update existing records in a specific table", dependsOnMethods = "insertRecordsToTable")
    public void updateRecordsInTable() throws Exception {
    	
        log.info("Executing updateRecordsInTable test case ...");
        StringBuilder url = new StringBuilder(TestConstants.ANALYTICS_TABLES_ENDPOINT_URL);
        url.append(TABLE_NAME);
        URL restUrl = new URL(url.toString());
        List<RecordBean> recordList = new ArrayList<RecordBean>();
        updateValueSet1 = new LinkedHashMap<String, Object>();
		updateValueSet1.put("newupdatedkey7@", "newupdated@value1");
		updateValueSet1.put("newupdatedkey6@", "newupdated@value2");
		updateValueSet1.put("newupdatedkey9@", "newupdated@value3");
		updateValueSet1.put("newupdatedkey0@", "newupdated@value4");
		updateValueSet1.put("newupdatedkey4@", "newupdated@value5");
		
		updateValueSet2 = new LinkedHashMap<String, Object>();
		updateValueSet2.put("newkey1@", "new@value1");
		updateValueSet2.put("newkey2@", "new@value2");
		updateValueSet2.put("newkey3@", "new@value3");
		updateValueSet2.put("newkey4@", "new@value4");
		updateValueSet2.put("newkey5@", "new@value5");
		record3 = new RecordBean();
        record3.setId("id1");
        record3.setValues(updateValueSet1);
        record4 = new RecordBean();
        record4.setId("id2");
        record4.setValues(updateValueSet2);
        recordList.add(record3);
        recordList.add(record4);
        HttpResponse response = HttpRequestUtil.doPost(restUrl, gson.toJson(recordList), headers);
		log.info("Response: " + response.getData());
		Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
		Assert.assertTrue(response.getData().contains("id1"));
		Assert.assertTrue(response.getData().contains("id2"));
    }
    
    @Test(groups = "wso2.das", description = "Insert records in a specific table", dependsOnMethods = "updateRecords")
    public void insertRecordsToTable() throws Exception {
    	
        log.info("Executing insertRecordsInTable test case ...");
        StringBuilder url = new StringBuilder(TestConstants.ANALYTICS_TABLES_ENDPOINT_URL);
        url.append(TABLE_NAME);
        URL restUrl = new URL(url.toString());
        List<RecordBean> recordList = new ArrayList<RecordBean>();
        updateValueSet1 = new LinkedHashMap<String, Object>();
		updateValueSet1.put("newKey1", "new Value1");
		updateValueSet1.put("newKey2", "new Value2");
		updateValueSet2 = new LinkedHashMap<String, Object>();
		updateValueSet2.put("newKey3", "new value3");
		updateValueSet2.put("newKey4", "new value4");
		record3 = new RecordBean();
		record3.setId("id3");
        record3.setValues(updateValueSet1);
        record4 = new RecordBean();
        record4.setId("id4");
        record4.setValues(updateValueSet2);
        recordList.add(record3);
        recordList.add(record4);
        HttpResponse response = HttpRequestUtil.doPost(restUrl, gson.toJson(recordList), headers);
		log.info("Response: " + response.getData());
		Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
		Assert.assertTrue(response.getData().contains("id3"));
        Assert.assertTrue(response.getData().contains("id4"));
    }
    
    @Test(groups = "wso2.das", description = "search records in a specific table", dependsOnMethods = "updateRecordsInTable")
    public void search() throws Exception {
    	
        log.info("Executing search test case ...");
        StringBuilder url = new StringBuilder(TestConstants.ANALYTICS_SEARCH_ENDPOINT_URL);
        StringBuilder waitForIndexing = new StringBuilder(TestConstants.ANALYTICS_WAITFOR_INDEXING_ENDPOINT_URL);
        HttpResponse response = HttpRequestUtil.doGet(waitForIndexing.toString(), headers); //wait till indexing finishes
        Assert.assertEquals(response.getResponseCode(), 200, "Waiting till indexing finished - failed");
        URL restUrl = new URL(url.toString());
        QueryBean query = new QueryBean();
        query.setTableName(TABLE_NAME);
        query.setQuery("key3:value3");
        query.setStart(0);
        query.setCount(10);
        response = HttpRequestUtil.doPost(restUrl, gson.toJson(query), headers);
		log.info("Response: " + response.getData());
		Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
		Assert.assertTrue(response.getData().contains("\"key3\":\"value3\""), "Search result not found");
    }
    
    @Test(groups = "wso2.das", description = "get the search record count in a specific table", dependsOnMethods = "search")
    public void searchCount() throws Exception {
    	
        log.info("Executing searchCount test case ...");
        StringBuilder url = new StringBuilder(TestConstants.ANALYTICS_SEARCH_COUNT_ENDPOINT_URL);
        URL restUrl = new URL(url.toString());
        QueryBean query = new QueryBean();
        query.setTableName(TABLE_NAME);
        query.setQuery("key3:value3");
        query.setStart(0);
        query.setCount(10);
        HttpResponse response = HttpRequestUtil.doPost(restUrl, gson.toJson(query), headers);
		log.info("Response: " + response.getData());
		Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
		Assert.assertTrue(response.getData().contains("1"), "Search Count mismatch!");
    }

    @Test(groups = "wso2.das", description = "delete records by ids in a specific table", dependsOnMethods = "searchCount")
    public void deleteRecordsByIds() throws Exception {
    	
        log.info("Executing deleteRecordsByIds test case ...");
        StringBuilder url = new StringBuilder(TestConstants.ANALYTICS_TABLES_ENDPOINT_URL);
        url.append(TABLE_NAME);
        List<String> recordList = new ArrayList<String>();
        recordList.add("id3");
        recordList.add("id4");
        HttpClient httpClient = new DefaultHttpClient();
        HttpDeleteWithBody httpDelete = new HttpDeleteWithBody(url.toString());
        httpDelete.setHeader("Content-Type", TestConstants.CONTENT_TYPE_JSON);
        httpDelete.setHeader("Authorization", TestConstants.BASE64_ADMIN_ADMIN);
        HttpEntity entity = new StringEntity(gson.toJson(recordList));
        httpDelete.setEntity(entity);
        org.apache.http.HttpResponse response = httpClient.execute(httpDelete);
        String responseBody = EntityUtils.toString(response.getEntity());
		log.info("Response: " + responseBody);
		Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Status code is different");
		Assert.assertTrue(responseBody.contains("Successfully deleted records"), "Record deletion by IDs failed");
		EntityUtils.consume(response.getEntity()); //ensures the http connection is closed
    }
     
    @Test(groups = "wso2.das", description = "delete records given a time range in a specific table"
    		, dependsOnMethods = "deleteRecordsByIds")
    public void deleteRecordsByTimeRange() throws Exception {
    	
        log.info("Executing deleteRecordsByTimeRange test case ...");
        StringBuilder url = new StringBuilder(TestConstants.ANALYTICS_TABLES_ENDPOINT_URL);
        long currentTime = System.currentTimeMillis();
        url.append(TABLE_NAME);
        url.append("/");
        url.append(currentTime - ONE_HOUR_MILLISECOND);
        url.append("/");
        url.append(currentTime + ONE_HOUR_MILLISECOND);
        HttpClient httpClient = new DefaultHttpClient();
        HttpDeleteWithBody httpDelete = new HttpDeleteWithBody(url.toString());
        httpDelete.setHeader("Authorization", TestConstants.BASE64_ADMIN_ADMIN);
        org.apache.http.HttpResponse response = httpClient.execute(httpDelete);
        String responseBody = EntityUtils.toString(response.getEntity());
		log.info("Response: " + responseBody);
		Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Status code is different");
		Assert.assertTrue(responseBody.contains("Successfully deleted records"), "Record deletion by timeRange failed");
		EntityUtils.consume(response.getEntity()); //ensures the http connection is closed
    }
    
    @Test(groups = "wso2.das", description = "clear indexData in a specific table"
    		, dependsOnMethods = "deleteRecordsByTimeRange")
    public void clearIndices() throws Exception {
    	
        log.info("Executing clearIndices test case ...");
        StringBuilder url = new StringBuilder(TestConstants.ANALYTICS_TABLES_ENDPOINT_URL);
        url.append(TABLE_NAME);
        url.append("/");
        url.append(INDICES);       
        HttpClient httpClient = new DefaultHttpClient();
        HttpDeleteWithBody httpDelete = new HttpDeleteWithBody(url.toString());
        httpDelete.setHeader("Authorization", TestConstants.BASE64_ADMIN_ADMIN);
        org.apache.http.HttpResponse response = httpClient.execute(httpDelete);
        String responseBody = EntityUtils.toString(response.getEntity());
		log.info("Response: " + responseBody);
		Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Status code is different");
		Assert.assertTrue(responseBody.contains("Successfully cleared indices"), "Record deletion by IDs failed");
		EntityUtils.consume(response.getEntity()); //ensures the http connection is closed
    }
    
    @Test(groups = "wso2.das", description = "deletes a specific table"
    		, dependsOnMethods = "deleteRecordsByTimeRange")
    public void deleteTable() throws Exception {
    	
        log.info("Executing deleteTable test case ...");
        StringBuilder url = new StringBuilder(TestConstants.ANALYTICS_TABLES_ENDPOINT_URL);  
        TableBean table = new TableBean();
        table.setTableName(TABLE_NAME);
        HttpClient httpClient = new DefaultHttpClient();
        HttpDeleteWithBody httpDelete = new HttpDeleteWithBody(url.toString());
        httpDelete.setHeader("Content-Type", TestConstants.CONTENT_TYPE_JSON);
        httpDelete.setHeader("Authorization", TestConstants.BASE64_ADMIN_ADMIN);
        httpDelete.setEntity(new StringEntity(gson.toJson(table)));
        org.apache.http.HttpResponse response = httpClient.execute(httpDelete);
        String responseBody = EntityUtils.toString(response.getEntity());
		log.info("Response: " + responseBody);
		Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Status code is different");
		Assert.assertTrue(responseBody.contains("Successfully deleted table"), "Table deletion failed");
		EntityUtils.consume(response.getEntity()); //ensures the http connection is closed
    }    
}

@NotThreadSafe
class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {
    public static final String METHOD_NAME = "DELETE";
    public String getMethod() { return METHOD_NAME; }

    public HttpDeleteWithBody(final String uri) {
        super();
        setURI(URI.create(uri));
    }
    public HttpDeleteWithBody(final URI uri) {
        super();
        setURI(uri);
    }
    public HttpDeleteWithBody() { super(); }
}
