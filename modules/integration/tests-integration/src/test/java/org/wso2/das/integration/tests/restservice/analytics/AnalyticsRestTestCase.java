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
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.analytics.api.AnalyticsDataAPIUtil;
import org.wso2.carbon.analytics.api.CarbonAnalyticsAPI;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.das.analytics.rest.beans.AggregateFieldBean;
import org.wso2.das.analytics.rest.beans.AggregateRequestBean;
import org.wso2.das.analytics.rest.beans.AnalyticsSchemaBean;
import org.wso2.das.analytics.rest.beans.CategoryDrillDownRequestBean;
import org.wso2.das.analytics.rest.beans.ColumnDefinitionBean;
import org.wso2.das.analytics.rest.beans.ColumnTypeBean;
import org.wso2.das.analytics.rest.beans.DrillDownPathBean;
import org.wso2.das.analytics.rest.beans.DrillDownRangeBean;
import org.wso2.das.analytics.rest.beans.DrillDownRequestBean;
import org.wso2.das.analytics.rest.beans.QueryBean;
import org.wso2.das.analytics.rest.beans.RecordBean;
import org.wso2.das.analytics.rest.beans.ResponseBean;
import org.wso2.das.analytics.rest.beans.SortByFieldBean;
import org.wso2.das.analytics.rest.beans.SubCategoriesBean;
import org.wso2.das.integration.common.utils.DASIntegrationTest;
import org.wso2.das.integration.common.utils.TestConstants;
import org.wso2.das.integration.common.utils.Utils;

import java.io.File;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
public class AnalyticsRestTestCase extends DASIntegrationTest {

    private static final Log log = LogFactory.getLog(AnalyticsRestTestCase.class);
    private static final String TABLE_NAME = "testtable";
    private static final String TABLE_NAME2 = "doesntExists";
    private static final String INDICES = "indexData";
    private static final long ONE_HOUR_MILLISECOND = 3600000;
    private static final Gson gson = new Gson();
//    private AnalyticsSchemaBean schemaBean;
    private Map<String, String> headers;
    private Map<String, Object> updateValueSet1, valueSet1;
    private Map<String, Object> updateValueSet2, valueSet2;
    private RecordBean record1;
    private RecordBean record2;
  /*  private RecordBean record3;
    private RecordBean record4;*/
    AnalyticsDataAPI analyticsDataAPI;

    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        super.init();
        headers = new HashMap<>();
        headers.put("Content-Type", TestConstants.CONTENT_TYPE_JSON);
        headers.put("Accept", TestConstants.CONTENT_TYPE_JSON);
        headers.put("Authorization", TestConstants.BASE64_ADMIN_ADMIN);
        /*Map<String, ColumnDefinitionBean> indices = new HashMap<>();
        indices.put("key1@", new ColumnDefinitionBean(ColumnTypeBean.STRING, true, false));
		indices.put("key2@", new ColumnDefinitionBean(ColumnTypeBean.STRING, true, false));
		indices.put("key3", new ColumnDefinitionBean(ColumnTypeBean.STRING, true, false));
		indices.put("key4@", new ColumnDefinitionBean(ColumnTypeBean.STRING, true, false));
		indices.put("key5@", new ColumnDefinitionBean(ColumnTypeBean.STRING, true, false));
		indices.put("IndexedKey", new ColumnDefinitionBean(ColumnTypeBean.STRING, true, false));
        indices.put("facet", new ColumnDefinitionBean(ColumnTypeBean.FACET, true, false));
        schemaBean = new AnalyticsSchemaBean(indices, null);*/
//        Map<String, Object> valueSet1 = new LinkedHashMap<>();
        valueSet1 = new LinkedHashMap<>();
		valueSet1.put("key1@", "@value1");
		valueSet1.put("key2@", "@value2");
		valueSet1.put("key3", "value3");
		valueSet1.put("key4@", "@value4");
		valueSet1.put("key5@", "@value5");
//        Map<String, Object> valueSet2 = new LinkedHashMap<>();
        valueSet2 = new LinkedHashMap<>();
		valueSet2.put("key7@", "@value1");
		valueSet2.put("key6@", "@value2");
		valueSet2.put("key9@", "@value3");
		valueSet2.put("key0@", "@value4");
		valueSet2.put("key4@", "@value5");
		updateValueSet1 = new LinkedHashMap<>();
		updateValueSet1.put("updatedkey7@", "updated@value1");
		updateValueSet1.put("updatedkey6@", "updated@value2");
		updateValueSet1.put("IndexedKey", "IndexedValue");
		updateValueSet1.put("updatedkey0@", "updated@value4");
		updateValueSet1.put("updatedkey4@", "updated@value5");
		updateValueSet2 = new LinkedHashMap<>();
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
	/*	record3 = new RecordBean();
		record3.setTableName(TABLE_NAME);
		record3.setValues(valueSet1);
		record4 = new RecordBean();
		record4.setTableName(TABLE_NAME);
		record4.setValues(valueSet2);*/

        String apiConf =
                new File(this.getClass().getClassLoader().
                        getResource("dasconfig" + File.separator + "api" + File.separator + "analytics-data-config.xml").toURI())
                        .getAbsolutePath();
        analyticsDataAPI = new CarbonAnalyticsAPI(apiConf);
    }

    /*@Test(groups = "wso2.das", description = "Create table")
    public void createTable() throws Exception {
        log.info("Executing create table test case ...");
        URL restUrl = new URL(TestConstants.ANALYTICS_TABLES_ENDPOINT_URL);
        TableBean tableBean = new TableBean();
        tableBean.setTableName(TABLE_NAME);
        HttpResponse response = HttpRequestUtil.doPost(restUrl,gson.toJson(tableBean), headers);
        log.info("Response: " + response.getData());
        Assert.assertEquals(response.getResponseCode(), 201, "Status code is different");
        Assert.assertTrue(response.getData().
                contains("Successfully created table: " + TABLE_NAME));
    }*/

    @Test(groups = "wso2.das", description = "Create table")
    public void createTable() throws Exception {
        log.info("Executing create table test case ...");
        analyticsDataAPI.createTable(MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME);
    }

    @Test(groups = "wso2.das", description = "Checks if table exists", dependsOnMethods = "createTable")
    public void tableExists() throws Exception {
        log.info("Executing Table Exist test case ...");
        HttpResponse response = Utils.doGet(TestConstants.ANALYTICS_ENDPOINT_URL +
                                            TestConstants.TABLE_EXISTS + TABLE_NAME, headers);
        log.info("Response: " + response.getData());
        Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
        ResponseBean responseBean = gson.fromJson(response.getData(), ResponseBean.class);
        Assert.assertEquals(responseBean.getStatus(), "success");
        Assert.assertEquals(responseBean.getMessage(), "Table : testtable exists.");
    }

    @Test(groups = "wso2.das", description = "Checks if table doesnt exist", dependsOnMethods = "tableExists")
    public void tableNotExist() throws Exception {
        log.info("Executing TableNotExist test case ...");
        HttpResponse response = Utils.doGet(TestConstants.ANALYTICS_ENDPOINT_URL +
                                            TestConstants.TABLE_EXISTS + TABLE_NAME2, headers);
        log.info("Response: " + response.getData());
        Assert.assertEquals(response.getResponseCode(), 404, "Status code is different");
        ResponseBean responseBean = gson.fromJson(response.getData(), ResponseBean.class);
        Assert.assertEquals(responseBean.getStatus(), "non-existent");
        Assert.assertEquals(responseBean.getMessage(), "Table : doesntExists does not exist.");
    }

    @Test(groups = "wso2.das", description = "lists all the tables", dependsOnMethods = "tableNotExist")
    public void getAllTables() throws Exception {
        log.info("Executing getAllTables test case ...");
        HttpResponse response = Utils.doGet(TestConstants.ANALYTICS_TABLES_ENDPOINT_URL, headers);
        log.info("Response: " + response.getData());
        Type listType = new TypeToken<List<String>>(){}.getType();
        List< String> tableNames = gson.fromJson(response.getData(), listType);
        Assert.assertTrue(tableNames.contains("testtable".toUpperCase()), "Table : testtable not found");
        Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
    }

    /*@Test(groups = "wso2.das", description = "Create table schema", dependsOnMethods = "createTable")
    public void setTableSchema() throws Exception {
        log.info("Executing createTableSchema test case ...");
        URL restUrl = new URL(TestConstants.ANALYTICS_TABLES_ENDPOINT_URL + TABLE_NAME + TestConstants.SCHEMA);
        HttpResponse response = HttpRequestUtil.doPost(restUrl, gson.toJson(schemaBean), headers);
        log.info("Response: " + response.getData());
        Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
    }*/

    @Test(groups = "wso2.das", description = "Create table schema", dependsOnMethods = "getAllTables")
    public void setTableSchema() throws Exception {
        log.info("Executing createTableSchema test case ...");
        List<ColumnDefinition> columns = new ArrayList<>();
        columns.add(new ColumnDefinition("key1@", AnalyticsSchema.ColumnType.STRING, true, false));
        columns.add(new ColumnDefinition("key2@", AnalyticsSchema.ColumnType.STRING, true, false));
        columns.add(new ColumnDefinition("key3", AnalyticsSchema.ColumnType.STRING, true, false));
        columns.add(new ColumnDefinition("key4@", AnalyticsSchema.ColumnType.STRING, true, false));
        columns.add(new ColumnDefinition("key5@", AnalyticsSchema.ColumnType.STRING, true, false));
        columns.add(new ColumnDefinition("IndexedKey", AnalyticsSchema.ColumnType.STRING, true, false));
        columns.add(new ColumnDefinition("facet", AnalyticsSchema.ColumnType.FACET, true, false));
        columns.add(new ColumnDefinition("aggregateValue", AnalyticsSchema.ColumnType.INTEGER, true, true));
        columns.add(new ColumnDefinition("doubleValue", AnalyticsSchema.ColumnType.DOUBLE, true, true));
        columns.add(new ColumnDefinition("floatValue", AnalyticsSchema.ColumnType.FLOAT, true, true));
        columns.add(new ColumnDefinition("longValue", AnalyticsSchema.ColumnType.LONG, true, true));

        AnalyticsSchema analyticsSchema = new AnalyticsSchema(columns, null);
        analyticsDataAPI.setTableSchema(MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME, analyticsSchema);
    }

    @Test(groups = "wso2.das", description = "Get table schema", dependsOnMethods = "setTableSchema")
    public void getTableSchema() throws Exception {
        log.info("Executing getTableSchema test case ...");
        HttpResponse response = Utils.doGet(TestConstants.ANALYTICS_TABLES_ENDPOINT_URL +
                                            TABLE_NAME + TestConstants.SCHEMA, headers);
        log.info("Response: " + response.getData());
        Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
        AnalyticsSchemaBean responseBean = gson.fromJson(response.getData(), AnalyticsSchemaBean.class);
        List<String> primaryKeys =  responseBean.getPrimaryKeys();
        Map<String, ColumnDefinitionBean> columns = responseBean.getColumns();
        Assert.assertTrue(primaryKeys.isEmpty());
        Assert.assertTrue(columns.containsKey("key1@"));
        Assert.assertTrue(columns.containsKey("key2@"));
        Assert.assertTrue(columns.containsKey("key3"));
        Assert.assertTrue(columns.containsKey("key4@"));
        Assert.assertTrue(columns.containsKey("key5@"));
        Assert.assertTrue(columns.containsKey("IndexedKey"));
        Assert.assertTrue(columns.containsKey("facet"));
        Assert.assertTrue(columns.containsKey("aggregateValue"));

        ColumnDefinitionBean bean = columns.get("key1@");
        Assert.assertTrue(bean.getType() == ColumnTypeBean.STRING);
        Assert.assertTrue(bean.isIndex());
        Assert.assertTrue(!bean.isScoreParam());

        bean = columns.get("key2@");
        Assert.assertTrue(bean.getType() == ColumnTypeBean.STRING);
        Assert.assertTrue(bean.isIndex());
        Assert.assertTrue(!bean.isScoreParam());

        bean = columns.get("key3");
        Assert.assertTrue(bean.getType() == ColumnTypeBean.STRING);
        Assert.assertTrue(bean.isIndex());
        Assert.assertTrue(!bean.isScoreParam());

        bean = columns.get("key4@");
        Assert.assertTrue(bean.getType() == ColumnTypeBean.STRING);
        Assert.assertTrue(bean.isIndex());
        Assert.assertTrue(!bean.isScoreParam());

        bean = columns.get("key5@");
        Assert.assertTrue(bean.getType() == ColumnTypeBean.STRING);
        Assert.assertTrue(bean.isIndex());
        Assert.assertTrue(!bean.isScoreParam());

        bean = columns.get("IndexedKey");
        Assert.assertTrue(bean.getType() == ColumnTypeBean.STRING);
        Assert.assertTrue(bean.isIndex());
        Assert.assertTrue(!bean.isScoreParam());

        bean = columns.get("facet");
        Assert.assertTrue(bean.getType() != ColumnTypeBean.FACET); // rest api do not return the FACET type but the attribute "isFacet"
        Assert.assertTrue(bean.isIndex());
        Assert.assertTrue(bean.isFacet());
        Assert.assertTrue(!bean.isScoreParam());

        bean = columns.get("aggregateValue");
        Assert.assertTrue(bean.getType() == ColumnTypeBean.INTEGER);
        Assert.assertTrue(bean.isIndex());
        Assert.assertTrue(bean.isScoreParam());
    }

    @Test(groups = "wso2.das", description = "Create records without optional parameters", dependsOnMethods = "getTableSchema")
    public void createRecordsWithoutOptionalParams() throws Exception {
        log.info("Executing create records without Optional Parameters test case ...");
        List<Record> records = new ArrayList<>();
        records.add(new Record(MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME, valueSet1));
        records.add(new Record(MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME, valueSet2));
        analyticsDataAPI.put(records);
    }

    @Test(groups = "wso2.das", description = "Create records with optional params", dependsOnMethods = "createRecordsWithoutOptionalParams")
    public void createRecordsWithOptionalParams() throws Exception {

        log.info("Executing create records test case ...");
        List<Record> records = new ArrayList<>();
        records.add(new Record("id1", MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME, valueSet1));
        records.add(new Record("id2", MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME, valueSet2));
        analyticsDataAPI.put(records);
    }

    @Test(groups = "wso2.das", description = "Get the record count of a table", dependsOnMethods = "createRecordsWithOptionalParams")
    public void getRecordCount() throws Exception {

        log.info("Executing getRecordCount test case ...");
        HttpResponse response = Utils.doGet(TestConstants.ANALYTICS_TABLES_ENDPOINT_URL + TABLE_NAME +
                "/recordcount", headers);
        log.info("Response: " + response.getData());
        if (!response.getData().equals("-1")) {
            Assert.assertEquals(response.getData(), "4", "record count is different");
        }
        Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
    }

    @Test(groups = "wso2.das", description = "Get records without pagination", dependsOnMethods = "createRecordsWithoutOptionalParams")
    public void getRecordsWithoutPagination() throws Exception {

        log.info("Executing get records without pagination test case ...");
        long currentTime = System.currentTimeMillis();
        HttpResponse response = Utils.doGet(TestConstants.ANALYTICS_TABLES_ENDPOINT_URL + TABLE_NAME + "/" +
                                            (currentTime - ONE_HOUR_MILLISECOND) + "/" +
                                            (currentTime + ONE_HOUR_MILLISECOND), headers);
        Type listType = new TypeToken<List<RecordBean>>(){}.getType();
        List< RecordBean> recordList = gson.fromJson(response.getData(), listType);
		Assert.assertEquals(recordList.size(), 4,
                            "Size mismatch!");
        Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
        for (RecordBean bean : recordList) {
            if (bean.getId().equals("id1")) {
                Assert.assertTrue(bean.getValues().entrySet().containsAll(valueSet1.entrySet()));
            } else if (bean.getId().equals("id2")) {
                Assert.assertTrue(bean.getValues().entrySet().containsAll(valueSet2.entrySet()));
            }
        }
    }

    @Test(groups = "wso2.das", description = "Get records with pagination", dependsOnMethods =
            "createRecordsWithOptionalParams")
    public void getRecordsWithPagination() throws Exception {

        log.info("Executing get records with pagination test case ...");
        long currentTime = System.currentTimeMillis();
        HttpResponse response = Utils.doGet(TestConstants.ANALYTICS_TABLES_ENDPOINT_URL + TABLE_NAME +
                                            "/" +
                                            (currentTime - ONE_HOUR_MILLISECOND) + "/" +
                                            (currentTime + ONE_HOUR_MILLISECOND) + "/" +
                                            "0" + "/" + "2", headers);
        log.info("Response: " + response.getData());
        Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
        Type listType = new TypeToken<List<RecordBean>>(){}.getType();
        List< RecordBean> recordList = gson.fromJson(response.getData(), listType);
        for (RecordBean bean : recordList) {
            if (bean.getId().equals("id1")) {
                Assert.assertTrue(bean.getValues().entrySet().containsAll(valueSet1.entrySet()));
            } else if (bean.getId().equals("id2")) {
                Assert.assertTrue(bean.getValues().entrySet().containsAll(valueSet2.entrySet()));
            }
        }
    }


    @Test(groups = "wso2.das", description = "Get all records", dependsOnMethods = "getRecordCount")
    public void getAllRecords() throws Exception {

        log.info("Executing get All records test case ...");
        HttpResponse response = Utils.doGet(TestConstants.ANALYTICS_TABLES_ENDPOINT_URL +
                                            TABLE_NAME, headers);
        Type listType = new TypeToken<List<RecordBean>>(){}.getType();
        List< RecordBean> recordList = gson.fromJson(response.getData(), listType);
        log.info("Response :" + response.getData());
		Assert.assertEquals(recordList.size(), 4,
                            "Size mismatch!");
        Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
        for (RecordBean bean : recordList) {
            if (bean.getId().equals("id1")) {
                Assert.assertTrue(bean.getValues().entrySet().containsAll(valueSet1.entrySet()));
            } else if (bean.getId().equals("id2")) {
                Assert.assertTrue(bean.getValues().entrySet().containsAll(valueSet2.entrySet()));
            }
        }
    }

    @Test(groups = "wso2.das", description = "update existing records", dependsOnMethods = "search")
    public void updateRecords() throws Exception {

        log.info("Executing updateRecords test case ...");
        List<Record> records = new ArrayList<>();
        records.add(new Record("id1", MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME, updateValueSet1));
        records.add(new Record("id2", MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME, updateValueSet2));
        analyticsDataAPI.put(records);
    }


    @Test(groups = "wso2.das", description = "update existing records in a specific table", dependsOnMethods = "insertRecordsToTable")
    public void updateRecordsInTable() throws Exception {

        log.info("Executing updateRecordsInTable test case ...");
        updateValueSet1 = new LinkedHashMap<>();
        updateValueSet1.put("newupdatedkey7@", "newupdated@value1");
        updateValueSet1.put("newupdatedkey6@", "newupdated@value2");
        updateValueSet1.put("newupdatedkey9@", "newupdated@value3");
        updateValueSet1.put("newupdatedkey0@", "newupdated@value4");
        updateValueSet1.put("newupdatedkey4@", "newupdated@value5");
        updateValueSet2 = new LinkedHashMap<>();
        updateValueSet2.put("newkey1@", "new@value1");
        updateValueSet2.put("newkey2@", "new@value2");
        updateValueSet2.put("newkey3@", "new@value3");
        updateValueSet2.put("newkey4@", "new@value4");
        updateValueSet2.put("newkey5@", "new@value5");
        List<Record> records = new ArrayList<>();
        records.add(new Record("id1", MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME, updateValueSet1));
        records.add(new Record("id2", MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME, updateValueSet2));
        analyticsDataAPI.put(records);

    }

    @Test(groups = "wso2.das", description = "Insert records in a specific table", dependsOnMethods = "updateRecords")
    public void insertRecordsToTable() throws Exception {

        log.info("Executing insertRecordsInTable test case ...");
        updateValueSet1 = new LinkedHashMap<>();
		updateValueSet1.put("newKey1", "new Value1");
		updateValueSet1.put("newKey2", "new Value2");
		updateValueSet2 = new LinkedHashMap<>();
		updateValueSet2.put("newKey3", "new value3");
		updateValueSet2.put("newKey4", "new value4");
        List<Record> records = new ArrayList<>();
        records.add(new Record("id3", MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME, updateValueSet1));
        records.add(new Record("id4", MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME, updateValueSet2));
        analyticsDataAPI.put(records);
    }

    @Test(groups = "wso2.das", description = "search records in a specific table", dependsOnMethods = "searchCount")
    public void search() throws Exception {
        log.info("Executing search test case ...");
        URL restUrl = new URL(TestConstants.ANALYTICS_SEARCH_ENDPOINT_URL);
        QueryBean query = new QueryBean();
        query.setTableName(TABLE_NAME);
        query.setQuery("key3:value3");
        query.setStart(0);
        query.setCount(10);
        HttpResponse response = HttpRequestUtil.doPost(restUrl, gson.toJson(query), headers);
        log.info("Response: " + response.getData());
        Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
        Type listType = new TypeToken<List<RecordBean>>(){}.getType();
        List< RecordBean> recordList = gson.fromJson(response.getData(), listType);
        Assert.assertTrue(recordList.size() == 2);
        Assert.assertTrue(recordList.get(0).getValue("key3").equals("value3"));
    }

    @Test(groups = "wso2.das", description = "get the search record count in a specific table", dependsOnMethods = "getAllRecords")
    public void searchCount() throws Exception {

        log.info("Executing searchCount test case ...");
        URL restUrl = new URL(TestConstants.ANALYTICS_SEARCH_COUNT_ENDPOINT_URL);
        QueryBean query = new QueryBean();
        query.setTableName(TABLE_NAME);
        query.setQuery("key3:value3");
        boolean codeOK = false;
        int counter = 0;
        HttpResponse response;
        while (!codeOK) {
            response = HttpRequestUtil.doPost(restUrl, gson.toJson(query), headers);
            codeOK = (response.getResponseCode() == 200) && response.getData().contains("2");
            if (!codeOK) {
                Thread.sleep(2000L);
            }
            if (counter == 10) {
                Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
                Assert.assertTrue(response.getData().contains("2"), "Search Count mismatch!");
            }
            counter++;
        }
    }

    @Test(groups = "wso2.das", description = "delete records by ids in a specific table", dependsOnMethods = "updateRecordsInTable")
    public void deleteRecordsByIds() throws Exception {

        log.info("Executing deleteRecordsByIds test case ...");
        List<String> recordList = new ArrayList<>();
        recordList.add("id3");
        recordList.add("id4");
        analyticsDataAPI.delete(MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME, recordList);
    }

    @Test(groups = "wso2.das", description = "delete records given a time range in a specific table"
            , dependsOnMethods = "deleteRecordsByIds")
    public void deleteRecordsByTimeRange() throws Exception {
        log.info("Executing deleteRecordsByTimeRange test case ...");
        long currentTime = System.currentTimeMillis();
        analyticsDataAPI.delete(MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME,
                                currentTime - ONE_HOUR_MILLISECOND, currentTime + ONE_HOUR_MILLISECOND);
    }

    @Test(groups = "wso2.das", description = "Add records which have facet fields", dependsOnMethods = "deleteRecordsByTimeRange")
    public void addFacetRecords() throws Exception {
        log.info("Executing addFacetRecords test case ...");
        Map<String, Object> values1 = record1.getValues();
        /* this must be an ArrayList, since it needs to have a no-arg constructor to work with Kryo serialization */
        values1.put("facet", "SriLanka,Colombo");
        values1.put("aggregateValue", 345);
        values1.put("doubleValue", 345);
        values1.put("longValue", 345);
        values1.put("floatValue", 345);
        Map<String, Object> values2 = record2.getValues();
        values2.put("facet", "2015,April,28,12,34,24");
        values2.put("aggregateValue", 654);
        values2.put("doubleValue", 654);
        values2.put("longValue", 654);
        values2.put("floatValue", 654);
        List<Record> records = new ArrayList<>();
        records.add(new Record(MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME, values1));
        records.add(new Record(MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME, values2));
        analyticsDataAPI.put(records);
    }

    @Test(groups = "wso2.das", description = "Add records which have facet fields to a table",
            dependsOnMethods = "addFacetRecords")
    public void addFacetRecordsToTable() throws Exception {
        log.info("Executing addFacetRecordsToTable test case ...");
        Map<String, Object> values1 = record1.getValues();
        values1.put("facet", new ArrayList<String>(Arrays.asList("SriLanka", "Colombo")));
        values1.put("aggregateValue", 1245);
        values1.put("longValue", 1245);
        values1.put("floatValue", 1245);
        values1.put("doubleValue", 1245);
        Map<String, Object> values2 = record2.getValues();
        values2.put("facet", new ArrayList<String>(Arrays.asList("2015", "April", "28", "12", "34", "24")));
        values2.put("aggregateValue", 6789);
        values2.put("longValue", 6789);
        values2.put("floatValue", 6789);
        values2.put("doubleValue", 6789);
        List<Record> records = new ArrayList<>();
        records.add(new Record(MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME, values1));
        records.add(new Record(MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME, values2));
        analyticsDataAPI.put(records);
    }

    @Test(groups = "wso2.das", description = "drilldown through the faceted fields",
            dependsOnMethods = "addFacetRecordsToTable")
    public void drillDownSearchWithoutSearchQuery() throws Exception {
        log.info("Executing drillDownSearch test case ...");
        URL restUrl = new URL(TestConstants.ANALYTICS_DRILLDOWN_ENDPOINT_URL);
        DrillDownRequestBean request = new DrillDownRequestBean();
        List<DrillDownPathBean> paths = new ArrayList<>();
        DrillDownPathBean path = new DrillDownPathBean();
        path.setPath(new String[]{"SriLanka", "Colombo"});
        path.setFieldName("facet");
        paths.add(path);
        request.setTableName(TABLE_NAME);
        request.setRecordStart(0);
        request.setRecordCount(1);
        request.setCategories(paths);
        String postBody = gson.toJson(request);
        boolean codeOK = false;
        int counter = 0;
        while (!codeOK) {
            HttpResponse response = HttpRequestUtil.doPost(restUrl, postBody, headers);
            codeOK = (response.getResponseCode() == 200) && !response.getData().contains("[]");
            if (!codeOK) {
                Thread.sleep(2000L);
            }
            if (counter == 10) {
                Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
                Assert.assertFalse(response.getData().contains("[]"));
                Type listType = new TypeToken<List<RecordBean>>(){}.getType();
                List< RecordBean> recordList = gson.fromJson(response.getData(), listType);
                Assert.assertTrue(recordList.size() == 1);
                Assert.assertTrue(recordList.get(0).getValue("facet").equals("SriLanka,Colombo"));

            }
            counter++;
        }
    }

    @Test(groups = "wso2.das", description = "drilldown through the faceted fields",
            dependsOnMethods = "drillDownSearchWithoutSearchQuery")
    public void drillDownSearchWithSearchQuery() throws Exception {
        log.info("Executing drillDownSearch test case ...");
        URL restUrl = new URL(TestConstants.ANALYTICS_DRILLDOWN_ENDPOINT_URL);
        DrillDownRequestBean request = new DrillDownRequestBean();
        List<DrillDownPathBean> paths = new ArrayList<>();
        DrillDownPathBean path = new DrillDownPathBean();
        path.setPath(new String[]{"SriLanka", "Colombo"});
        path.setFieldName("facet");
        paths.add(path);
        request.setTableName(TABLE_NAME);
        request.setQuery("key1@:@value1");
        request.setRecordStart(0);
        request.setRecordCount(1);
        request.setCategories(paths);
        String postBody = gson.toJson(request);
        HttpResponse response = HttpRequestUtil.doPost(restUrl, postBody, headers);
        Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
        Assert.assertFalse(response.getData().contains("[]"));
        Type listType = new TypeToken<List<RecordBean>>(){}.getType();
        List< RecordBean> recordList = gson.fromJson(response.getData(), listType);
        Assert.assertTrue(recordList.size() == 1);
        Assert.assertTrue(recordList.get(0).getValue("facet").equals("SriLanka,Colombo"));
        Assert.assertTrue(recordList.get(0).getValue("key1@").equals("@value1"));
    }

    @Test(groups = "wso2.das", description = "drilldown Count",
            dependsOnMethods = "drillDownSearchWithSearchQuery")
    public void drillDownSearchCount() throws Exception {
        log.info("Executing DrilldownCount test case ...");
        URL restUrl = new URL(TestConstants.ANALYTICS_DRILLDOWNCOUNT_ENDPOINT_URL);
        DrillDownRequestBean request = new DrillDownRequestBean();
        List<DrillDownPathBean> paths = new ArrayList<>();
        DrillDownPathBean path = new DrillDownPathBean();
        path.setPath(new String[]{"SriLanka", "Colombo"});
        path.setFieldName("facet");
        paths.add(path);
        request.setTableName(TABLE_NAME);
        request.setQuery("key1@:@value1");
        request.setCategories(paths);
        String postBody = gson.toJson(request);
        log.info("request : " + postBody);
        HttpResponse response = HttpRequestUtil.doPost(restUrl, postBody, headers);
        Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
        Assert.assertTrue(response.getData().contains("1"));
    }

    @Test(groups = "wso2.das", description = "drilldown categories",
            dependsOnMethods = "drillDownSearchCount")
    public void drillDownCategories() throws Exception {
        log.info("Executing drilldownCategories test case ...");
        URL restUrl = new URL(TestConstants.ANALYTICS_DRILLDOWNCATEGORIES_ENDPOINT_URL);
        CategoryDrillDownRequestBean request = new CategoryDrillDownRequestBean();
        String[] path = new String[]{"SriLanka"};
        request.setTableName(TABLE_NAME);
        request.setCategoryPath(path);
        request.setFieldName("facet");
        request.setQuery("key1@:@value1");
        String postBody = gson.toJson(request);
        log.info("request: " + postBody);
        HttpResponse response = HttpRequestUtil.doPost(restUrl, postBody, headers);
        log.info("Response: " + response.getData());
        Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
        SubCategoriesBean bean = gson.fromJson(response.getData(), SubCategoriesBean.class);
        Assert.assertTrue(bean.getCategoryCount() == 1);
        Assert.assertTrue(bean.getCategoryPath()[0].equals("SriLanka"));
        Assert.assertTrue(bean.getCategories().size() == 1);
        Assert.assertTrue(bean.getCategories().get("Colombo") != null);
        Assert.assertTrue(bean.getCategories().get("Colombo").equals(new Double(1)));
    }

    @Test(groups = "wso2.das", description = "drilldown categories",
            dependsOnMethods = "drillDownCategories")
    public void drillDownCategoriesWithScoreParams() throws Exception {
        log.info("Executing drilldownCategoriesWithScoreParams test case ...");
        URL restUrl = new URL(TestConstants.ANALYTICS_DRILLDOWNCATEGORIES_ENDPOINT_URL);
        CategoryDrillDownRequestBean request = new CategoryDrillDownRequestBean();
        String[] path = new String[]{"SriLanka"};
        request.setTableName(TABLE_NAME);
        request.setCategoryPath(path);
        request.setFieldName("facet");
        request.setQuery("key1@:@value1");
        request.setScoreFunction("1");
        String postBody = gson.toJson(request);
        log.info("request: " + postBody);
        HttpResponse response = HttpRequestUtil.doPost(restUrl, postBody, headers);
        log.info("Response: " + response.getData());
        Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
        SubCategoriesBean bean = gson.fromJson(response.getData(), SubCategoriesBean.class);
        Assert.assertTrue(bean.getCategoryCount() == 1);
        Assert.assertTrue(bean.getCategoryPath()[0].equals("SriLanka"));
        Assert.assertTrue(bean.getCategories().size() == 1);
        Assert.assertTrue(bean.getCategories().get("Colombo") != null);
        Assert.assertTrue(bean.getCategories().get("Colombo").equals(new Double(1))); // 345 + facet : SriLanka,Colombo
    }

    @Test(groups = "wso2.das", description = "Perform SUM aggregation",
            dependsOnMethods = "drillDownCategoriesWithScoreParams")
    public void performAggregate() throws Exception {
        log.info("Executing perFormSUMAggregate test case ...");
        URL restUrl = new URL(TestConstants.ANALYTICS_AGGREGATES_ENDPOINT_URL);
        String[] path = new String[]{"SriLanka"};
        AggregateRequestBean request = new AggregateRequestBean();
        request.setTableName(TABLE_NAME);
        request.setAggregateLevel(0);
        request.setGroupByField("facet");
        request.setParentPath(new ArrayList<String>(Arrays.asList(path)));
        request.setQuery("*:*");
        ArrayList fields = new ArrayList();
        fields.add(new AggregateFieldBean(new String[]{"aggregateValue"}, "SUM", "sum"));
        fields.add(new AggregateFieldBean(new String[]{"aggregateValue"}, "AVG", "avg"));
        fields.add(new AggregateFieldBean(new String[]{"aggregateValue"}, "COUNT", "count"));
        fields.add(new AggregateFieldBean(new String[]{"aggregateValue"}, "MIN", "min"));
        fields.add(new AggregateFieldBean(new String[]{"aggregateValue"}, "MAX", "max"));
        fields.add(new AggregateFieldBean(new String[]{"aggregateValue"}, "FIRST", "first"));
        request.setFields(fields);
        String postBody = gson.toJson(request);
        log.info("request: " + postBody);
        HttpResponse response = HttpRequestUtil.doPost(restUrl, postBody, headers);
        log.info("Response: " + response.getData());
        Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
        Type listType = new TypeToken<List<RecordBean>>(){}.getType();
        List< RecordBean> recordList = gson.fromJson(response.getData(), listType);
        Assert.assertTrue(recordList.size() == 1);
        Assert.assertTrue(recordList.get(0).getValue("sum").equals(new Double(345)));
        Assert.assertTrue(recordList.get(0).getValue("avg").equals(new Double(345)));
        Assert.assertTrue(recordList.get(0).getValue("min").equals(new Double(345)));
        Assert.assertTrue(recordList.get(0).getValue("max").equals(new Double(345)));
        Assert.assertTrue(recordList.get(0).getValue("count").equals(new Double(1)));
        Assert.assertTrue(recordList.get(0).getValue("first") != null);
    }

    @Test(groups = "wso2.das", description = "Perform SUM aggregation",
            dependsOnMethods = "performAggregate", enabled = false)
    public void performAggregateForAllRecords() throws Exception {
        log.info("Executing performAggregateForAllRecords test case ...");
        URL restUrl = new URL(TestConstants.ANALYTICS_AGGREGATES_ENDPOINT_URL);
        AggregateRequestBean request = new AggregateRequestBean();
        request.setTableName(TABLE_NAME);
        request.setQuery("*:*");
        request.setNoOfRecords(100000);
        ArrayList fields = new ArrayList();
        fields.add(new AggregateFieldBean(new String[]{"aggregateValue"}, "SUM", "sum"));
        fields.add(new AggregateFieldBean(new String[]{"aggregateValue"}, "AVG", "avg"));
        fields.add(new AggregateFieldBean(new String[]{"aggregateValue"}, "COUNT", "count"));
        fields.add(new AggregateFieldBean(new String[]{"aggregateValue"}, "MIN", "min"));
        fields.add(new AggregateFieldBean(new String[]{"aggregateValue"}, "MAX", "max"));
        fields.add(new AggregateFieldBean(new String[]{"aggregateValue"}, "FIRST", "first"));
        request.setFields(fields);
        String postBody = gson.toJson(request);
        log.info("request: " + postBody);
        HttpResponse response = HttpRequestUtil.doPost(restUrl, postBody, headers);
        log.info("Response: " + response.getData());
        Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
        Type listType = new TypeToken<List<RecordBean>>(){}.getType();
        List< RecordBean> recordList = gson.fromJson(response.getData(), listType);
        Assert.assertTrue(recordList.size() == 1);
        Assert.assertTrue(recordList.get(0).getValue("sum").equals(new Double(9033)));
        Assert.assertTrue(recordList.get(0).getValue("avg").equals(new Double(2258.25)));
        Assert.assertTrue(recordList.get(0).getValue("min").equals(new Double(345)));
        Assert.assertTrue(recordList.get(0).getValue("max").equals(new Double(6789)));
        Assert.assertTrue(recordList.get(0).getValue("count").equals(new Double(4)));
        Assert.assertTrue(recordList.get(0).getValue("first") != null);
    }

    @Test(groups = "wso2.das", description = "search records in a specific table", dependsOnMethods = "performAggregate")
    public void searchWithSorting() throws Exception {
        log.info("Executing search with sorting test case ...");
        URL restUrl = new URL(TestConstants.ANALYTICS_SEARCH_ENDPOINT_URL);
        QueryBean query = new QueryBean();
        query.setTableName(TABLE_NAME);
        query.setQuery("*:*");
        query.setStart(0);
        query.setCount(10);
        List<SortByFieldBean> beans = new ArrayList<>();
        SortByFieldBean byFieldBean = new SortByFieldBean("aggregateValue", "DESC");
        beans.add(byFieldBean);
        query.setSortBy(beans);
        String request = gson.toJson(query);
        log.info("Request: " + request);
        HttpResponse response = HttpRequestUtil.doPost(restUrl, request, headers);
        log.info("Response: " + response.getData());
        Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
        Type listType = new TypeToken<List<RecordBean>>(){}.getType();
        List< RecordBean> recordList = gson.fromJson(response.getData(), listType);
        Assert.assertEquals(recordList.get(0).getValue("aggregateValue"), new Double(6789));
        Assert.assertEquals(recordList.get(recordList.size() -1).getValue("aggregateValue"), new Double(345));
    }

    @Test(groups = "wso2.das", description = "drilldown through the faceted fields with sorting",
            dependsOnMethods = "searchWithSorting")
    public void drillDownSearchWithSorting() throws Exception {
        log.info("Executing drillDownSearch test case ...");
        URL restUrl = new URL(TestConstants.ANALYTICS_DRILLDOWN_ENDPOINT_URL);
        DrillDownRequestBean request = new DrillDownRequestBean();
        List<DrillDownPathBean> paths = new ArrayList<>();
        DrillDownPathBean path = new DrillDownPathBean();
        path.setPath(new String[]{"SriLanka"});
        path.setFieldName("facet");
        paths.add(path);
        request.setTableName(TABLE_NAME);
        request.setQuery("*:*");
        request.setRecordStart(0);
        request.setRecordCount(10);
        request.setCategories(paths);
        List<SortByFieldBean> sortByFieldBeans = new ArrayList<>();
        SortByFieldBean byFieldBean = new SortByFieldBean("aggregateValue", "DESC");
        sortByFieldBeans.add(byFieldBean);
        request.setSortByFields(sortByFieldBeans);
        String postBody = gson.toJson(request);
        log.info("request: " + postBody);
        HttpResponse response = HttpRequestUtil.doPost(restUrl, postBody, headers);
        Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
        log.info("Response: " + response.getData());
        Assert.assertFalse(response.getData().contains("[]"));
        Type listType = new TypeToken<List<RecordBean>>(){}.getType();
        List< RecordBean> recordList = gson.fromJson(response.getData(), listType);
        Assert.assertTrue(recordList.get(0).getValue("facet").equals("SriLanka,Colombo"));
    }

    @Test(groups = "wso2.das", description = "drilldown range count operation",
            dependsOnMethods = "drillDownSearchWithSorting")
    public void drillDownRanges() throws Exception {
        log.info("Executing drillDownRange test case ...");
        URL restUrl = new URL(TestConstants.ANALYTICS_DRILLDOWNRANGE_ENDPOINT_URL);
        DrillDownRequestBean request = new DrillDownRequestBean();
        DrillDownRangeBean bean1 = new DrillDownRangeBean();
        bean1.setLabel("0-1000");
        bean1.setFrom(0);
        bean1.setTo(1000);
        DrillDownRangeBean bean2 = new DrillDownRangeBean();
        bean2.setLabel("1000-2000");
        bean2.setFrom(1000);
        bean2.setTo(2000);
        List<DrillDownRangeBean> rangeBeans = new ArrayList<>();
        rangeBeans.add(bean1);
        rangeBeans.add(bean2);
        request.setTableName(TABLE_NAME);
        request.setQuery("*:*");
        request.setRangeField("aggregateValue");
        request.setRanges(rangeBeans);
        String postBody = gson.toJson(request);
        log.info("request: " + postBody);
        HttpResponse response = HttpRequestUtil.doPost(restUrl, postBody, headers);
        Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
        log.info("Response: " + response.getData());
        Assert.assertFalse(response.getData().contains("[]"));
        Type rangeListType = new TypeToken<List<DrillDownRangeBean>>(){}.getType();
        List<DrillDownRangeBean> rangeBeanList = gson.fromJson(response.getData(), rangeListType);
        Assert.assertEquals(rangeBeanList.get(0).getScore(), new Double(2));
        Assert.assertEquals(rangeBeanList.get(1).getScore(), new Double(1));
    }


    @Test(groups = "wso2.das", description = "re-index records in a specific table", dependsOnMethods = "drillDownRanges")
    public void reIndex() throws Exception {
        log.info("Executing reIndex test case ...");
        long currentTime = System.currentTimeMillis();
        HttpResponse response;
        int n = AnalyticsDataAPIUtil.listRecords(analyticsDataAPI, analyticsDataAPI.get(MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME,
                                                                                           1, null, currentTime - ONE_HOUR_MILLISECOND, currentTime + ONE_HOUR_MILLISECOND, 0, -1)).size();
        Assert.assertEquals(n, 4);
        analyticsDataAPI.clearIndexData(MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME);
        List<SearchResultEntry> ids = analyticsDataAPI.search(MultitenantConstants.SUPER_TENANT_ID,TABLE_NAME, "*:*", 0, 100, null);
        Assert.assertEquals(ids.size(), 0, "Indices are not cleared..");
        URL restUrl = new URL(TestConstants.ANALYTICS_REINDEX_ENDPOINT_URL + TABLE_NAME + "?from=" +
                              (currentTime - ONE_HOUR_MILLISECOND) + "&to=" + (currentTime + ONE_HOUR_MILLISECOND));
        response = HttpRequestUtil.doPost(restUrl, gson.toJson(null), headers);
        Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
        boolean codeOK = false;
        int counter = 0;
        while (!codeOK) {
            ids = analyticsDataAPI.search(MultitenantConstants.SUPER_TENANT_ID,TABLE_NAME, "*:*", 0, 100, null);
            codeOK = (ids.size() != 0);
            if (!codeOK) {
                Thread.sleep(2000L);
            }
            if (counter == 10) {
                Assert.assertEquals(ids.size(), n, "Records not fully re-indexed");
            }
            counter++;
        }
    }

    @Test(groups = "wso2.das", description = "clear indexData in a specific table"
    		, dependsOnMethods = "reIndex")
    public void clearIndices() throws Exception {

        log.info("Executing clearIndices test case ...");
        HttpClient httpClient = new DefaultHttpClient();
        HttpDeleteWithBody httpDelete = new HttpDeleteWithBody(TestConstants.ANALYTICS_TABLES_ENDPOINT_URL +
                                                               TABLE_NAME + "/" + INDICES);
        httpDelete.setHeader("Authorization", TestConstants.BASE64_ADMIN_ADMIN);
        org.apache.http.HttpResponse response = httpClient.execute(httpDelete);
        String responseBody = EntityUtils.toString(response.getEntity());
		log.info("Response: " + responseBody);
		Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Status code is different");
		Assert.assertTrue(responseBody.contains("Successfully cleared indices"), "Record deletion by IDs failed");
		EntityUtils.consume(response.getEntity()); //ensures the http connection is closed
    }
    
    /*@Test(groups = "wso2.das", description = "deletes a specific table"
    		, dependsOnMethods = "clearIndices")
    public void deleteTable() throws Exception {
        log.info("Executing deleteTable test case ...");
        TableBean table = new TableBean();
        table.setTableName(TABLE_NAME);
        HttpClient httpClient = new DefaultHttpClient();
        HttpDeleteWithBody httpDelete = new HttpDeleteWithBody(TestConstants.ANALYTICS_TABLES_ENDPOINT_URL);
        httpDelete.setHeader("Content-Type", TestConstants.CONTENT_TYPE_JSON);
        httpDelete.setHeader("Authorization", TestConstants.BASE64_ADMIN_ADMIN);
        httpDelete.setEntity(new StringEntity(gson.toJson(table)));
        org.apache.http.HttpResponse response = httpClient.execute(httpDelete);
        String responseBody = EntityUtils.toString(response.getEntity());
		log.info("Response: " + responseBody);
		Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "Status code is different");
		Assert.assertTrue(responseBody.contains("Successfully deleted table"), "Table deletion failed");
		EntityUtils.consume(response.getEntity()); //ensures the http connection is closed
    }*/

    @Test(groups = "wso2.das", description = "deletes a specific table"
            , dependsOnMethods = "clearIndices")
    public void deleteTable() throws Exception {
        log.info("Executing deleteTable test case ...");
        analyticsDataAPI.deleteTable(MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME);
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
}
