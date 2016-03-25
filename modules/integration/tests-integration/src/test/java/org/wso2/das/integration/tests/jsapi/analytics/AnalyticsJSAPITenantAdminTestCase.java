/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.das.integration.tests.jsapi.analytics;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.jsservice.beans.AnalyticsSchemaBean;
import org.wso2.carbon.analytics.jsservice.beans.EventBean;
import org.wso2.carbon.analytics.jsservice.beans.ResponseBean;
import org.wso2.carbon.analytics.jsservice.beans.StreamDefinitionBean;
import org.wso2.carbon.analytics.jsservice.beans.StreamDefinitionQueryBean;
import org.wso2.carbon.analytics.stream.persistence.stub.dto.AnalyticsTable;
import org.wso2.carbon.analytics.stream.persistence.stub.dto.AnalyticsTableRecord;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.carbon.integration.common.admin.client.LogViewerClient;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.das.analytics.rest.beans.QueryBean;
import org.wso2.das.integration.common.clients.EventStreamPersistenceClient;
import org.wso2.das.integration.common.utils.DASIntegrationTest;
import org.wso2.das.integration.common.utils.TestConstants;
import org.wso2.das.integration.common.utils.Utils;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class contains integration tests for Javascript analytics API
 */
public class AnalyticsJSAPITenantAdminTestCase extends DASIntegrationTest {
    private static final int ONE_HOUR_IN_MILLISECONDS = 3600000;
    private Log log = LogFactory.getLog(AnalyticsJSAPITenantAdminTestCase.class);
    public static final String STREAM_NAME = "tenantTestStream";
    private static final String STREAM_VERSION = "1.0.0";
    private static final String STREAM_DESCRIPTION = "Sample Description";
    private Gson gson;
    private Map<String, String> httpHeaders;
    private static LogViewerClient logViewerClient;

    private static final int TYPE_CLEAR_INDEX_DATA = 1;
    //    public static final int TYPE_CREATE_TABLE = 2;
//    public static final int TYPE_DELETE_BY_ID = 3;
//    public static final int TYPE_DELETE_BY_RANGE = 4;
//    public static final int TYPE_DELETE_TABLE = 5;
    private static final int TYPE_GET_RECORD_COUNT = 6;
    private static final int TYPE_GET_BY_ID = 7;
    private static final int TYPE_GET_BY_RANGE = 8;
    private static final int TYPE_LIST_TABLES = 9;
    private static final int TYPE_GET_SCHEMA = 10;
    //    public static final int TYPE_PUT_RECORDS_TO_TABLE = 11;
    private static final int TYPE_PUT_RECORDS = 12;
    private static final int TYPE_SEARCH = 13;
    private static final int TYPE_SEARCH_COUNT = 14;
    //    public static final int TYPE_SET_SCHEMA = 15;
    private static final int TYPE_TABLE_EXISTS = 16;
    private static final int TYPE_WAIT_FOR_INDEXING = 17;
    private static final int TYPE_PAGINATION_SUPPORTED = 18;
    private static final int TYPE_DRILLDOWN_CATEGORIES = 19;
    private static final int TYPE_DRILLDOWN_SEARCH = 20;
    private static final int TYPE_DRILLDOWN_SEARCH_COUNT = 21;
    private static final int TYPE_ADD_STREAM_DEFINITION = 22;
    private static final int TYPE_GET_STREAM_DEFINITION = 23;
    private static final int TYPE_PUBLISH_EVENT = 24;

    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        super.init(TestUserMode.TENANT_ADMIN);
        gson = new Gson();
        httpHeaders = new HashMap<>();
        httpHeaders.put("Content-Type", TestConstants.CONTENT_TYPE_JSON);
        httpHeaders.put("Accept", TestConstants.CONTENT_TYPE_JSON);
        httpHeaders.put("Authorization", TestConstants.BASE64_TENANT_ADMIN_ADMIN);
        logViewerClient = new LogViewerClient(backendURL, getSessionCookie());
    }

    @Test(groups = "wso2.das", description = "Adds a stream definition")
    public void addStreamDefinition() throws Exception {
        log.info("Executing JSAPI.addStreamDefinition");
        StreamDefinitionBean streamDefinitionBean = new StreamDefinitionBean();
        Map<String, String> metaData = new LinkedHashMap<>();
        Map<String, String> payloadData = new LinkedHashMap<>();
        Map<String, String> correlationData = new LinkedHashMap<>();
        metaData.put("timestamp", "LONG");
//        payloadData.put("id", "STRING");
        payloadData.put("name", "STRING");
        payloadData.put("department", "STRING");
        payloadData.put("married", "BOOLEAN");
        streamDefinitionBean.setName(STREAM_NAME);
        streamDefinitionBean.setVersion(STREAM_VERSION);
        streamDefinitionBean.setDescription(STREAM_DESCRIPTION);
        streamDefinitionBean.setPayloadData(payloadData);
        streamDefinitionBean.setMetaData(metaData);
        streamDefinitionBean.setCorrelationData(correlationData);

        String url = TestConstants.ANALYTICS_JS_ENDPOINT +
                     "?type=" + TYPE_ADD_STREAM_DEFINITION;
        URL jsapiURL = new URL(url);
        HttpResponse response = HttpRequestUtil.doPost(jsapiURL, gson.toJson(streamDefinitionBean),httpHeaders);
        log.info("response: " + response.getData());
        Assert.assertEquals(response.getResponseCode(), 201, "response code is different");
        ResponseBean responseBean = gson.fromJson(response.getData(), ResponseBean.class);
        Assert.assertEquals(responseBean.getStatus(), "created", "Response status is not 'Created'");
        Assert.assertEquals(responseBean.getMessage(), "tenantTestStream:1.0.0", "Response message is different");
        String session = getSessionCookie();
        EventStreamPersistenceClient persistenceClient = new EventStreamPersistenceClient(backendURL, session);
        persistenceClient.addAnalyticsTable(getAnalyticsTable());
        boolean isLogAvailable = false;
        int count = 0;
        while (!isLogAvailable) {
            LogEvent[] logEvents = logViewerClient.getAllRemoteSystemLogs();
            for (LogEvent logEvent : logEvents) {
                if (logEvent.getMessage().equalsIgnoreCase("Deployed successfully analytics event store: tenantTestStream.xml")) {
                    isLogAvailable = true;
                }
            }
            if (!isLogAvailable) {
                Thread.sleep(2000);
            }
            if (count == 10 && !isLogAvailable) {
                log.error("tenantTestStream.xml was not deployed, took longer than usual");
                isLogAvailable = true;
            }
            count++;
        }
    }

    private AnalyticsTable getAnalyticsTable() {
        AnalyticsTable table = new AnalyticsTable();
        table.setPersist(true);
        table.setTableName(STREAM_NAME);
        table.setStreamVersion(STREAM_VERSION);
        AnalyticsTableRecord[] records = new AnalyticsTableRecord[4];
        AnalyticsTableRecord timestamp = new AnalyticsTableRecord();
        timestamp.setPersist(true);
        timestamp.setPrimaryKey(false);
        timestamp.setIndexed(true);
        timestamp.setColumnName("meta_timestamp");
        timestamp.setColumnType("LONG");
        timestamp.setScoreParam(false);
        records[0] = timestamp;
        AnalyticsTableRecord name = new AnalyticsTableRecord();
        name.setPersist(true);
        name.setPrimaryKey(true);
        name.setIndexed(true);
        name.setColumnName("name");
        name.setColumnType("STRING");
        name.setScoreParam(false);
        records[1] = name;
        AnalyticsTableRecord married = new AnalyticsTableRecord();
        married.setPersist(true);
        married.setPrimaryKey(true);
        married.setIndexed(true);
        married.setColumnName("married");
        married.setColumnType("BOOLEAN");
        married.setScoreParam(false);
        records[2] = married;
        AnalyticsTableRecord department = new AnalyticsTableRecord();
        department.setPersist(true);
        department.setPrimaryKey(false);
        department.setIndexed(true);
        department.setColumnName("department");
        department.setColumnType("FACET");
        department.setScoreParam(false);
        records[3] = department;
//        AnalyticsTableRecord id = new AnalyticsTableRecord();
//        id.setPersist(true);
//        id.setPrimaryKey(false);
//        id.setIndexed(true);
//        id.setColumnName("id");
//        id.setColumnType("STRING");
//        id.setScoreParam(false);
//        records[4] = id;
        table.setAnalyticsTableRecords(records);
        return table;
    }

    @Test(groups = "wso2.das", description = "publishes an event to an event stream", dependsOnMethods = "addStreamDefinition")
    public void publishEvent1() throws Exception {
        Thread.sleep(15000);
        log.info("Executing JSAPI.publishEvent");
        EventBean eventBean = new EventBean();
        eventBean.setTimeStamp(System.currentTimeMillis());
        eventBean.setStreamName(STREAM_NAME);
        eventBean.setStreamVersion(STREAM_VERSION);
        Map<String, Object> metaData = new HashMap<>();
        metaData.put("timestamp", System.currentTimeMillis());
        Map<String, Object> payloadData = new HashMap<>();
//        payloadData.put("id", "001");
        payloadData.put("name", "AAA");
        payloadData.put("department", "['WSO2', 'Engineering', 'R&D']");
        payloadData.put("married", false);
        eventBean.setMetaData(metaData);
        eventBean.setPayloadData(payloadData);
        String url = TestConstants.ANALYTICS_JS_ENDPOINT + "?type=" + TYPE_PUBLISH_EVENT;
        URL jsapiURL = new URL(url);
        HttpResponse response = HttpRequestUtil.doPost(jsapiURL, gson.toJson(eventBean), httpHeaders);
        log.info("Response: " + response.getData());
        ResponseBean responseBean = gson.fromJson(response.getData(), ResponseBean.class);
        Assert.assertEquals(response.getResponseCode(), 200);
        Assert.assertEquals(responseBean.getStatus(), "success");
    }

    @Test(groups = "wso2.das", description = "publishes an event to an event stream", dependsOnMethods = "publishEvent1")
    public void publishEvent2() throws Exception {
        log.info("Executing JSAPI.publishEvent");
        EventBean eventBean = new EventBean();
        eventBean.setTimeStamp(System.currentTimeMillis());
        eventBean.setStreamName(STREAM_NAME);
        eventBean.setStreamVersion(STREAM_VERSION);
        Map<String, Object> metaData = new HashMap<>();
        metaData.put("timestamp", System.currentTimeMillis());
        Map<String, Object> payloadData = new HashMap<>();
//        payloadData.put("id", "002");
        payloadData.put("name", "BBB");
        payloadData.put("department", "['WSO2', 'Engineering', 'Support']");
        payloadData.put("married", true);
        eventBean.setMetaData(metaData);
        eventBean.setPayloadData(payloadData);
        String url = TestConstants.ANALYTICS_JS_ENDPOINT + "?type=" + TYPE_PUBLISH_EVENT;
        URL jsapiURL = new URL(url);
        HttpResponse response = HttpRequestUtil.doPost(jsapiURL, gson.toJson(eventBean), httpHeaders);
        log.info("Response: " + response.getData());
        ResponseBean responseBean = gson.fromJson(response.getData(), ResponseBean.class);
        Assert.assertEquals(response.getResponseCode(), 200);
        Assert.assertEquals(responseBean.getStatus(), "success");
    }

    @Test(groups = "wso2.das", description = "publishes an event to an event stream", dependsOnMethods = "publishEvent2")
    public void publishEvent3() throws Exception {
        log.info("Executing JSAPI.publishEvent");
        EventBean eventBean = new EventBean();
        eventBean.setTimeStamp(System.currentTimeMillis());
        eventBean.setStreamName(STREAM_NAME);
        eventBean.setStreamVersion(STREAM_VERSION);
        Map<String, Object> metaData = new HashMap<>();
        metaData.put("timestamp", System.currentTimeMillis());
        Map<String, Object> payloadData = new HashMap<>();
//        payloadData.put("id", "003");
        payloadData.put("name", "CCC");
        payloadData.put("department", "['WSO2', 'HR', 'Intern']");
        payloadData.put("married", false);
        eventBean.setMetaData(metaData);
        eventBean.setPayloadData(payloadData);
        String url = TestConstants.ANALYTICS_JS_ENDPOINT + "?type=" + TYPE_PUBLISH_EVENT;
        URL jsapiURL = new URL(url);
        HttpResponse response = HttpRequestUtil.doPost(jsapiURL, gson.toJson(eventBean), httpHeaders);
        log.info("Response: " + response.getData());
        ResponseBean responseBean = gson.fromJson(response.getData(), ResponseBean.class);
        Assert.assertEquals(response.getResponseCode(), 200);
        Assert.assertEquals(responseBean.getStatus(), "success");
    }

    @Test(groups = "wso2.das", description = "Get the stream definition", dependsOnMethods = "addStreamDefinition")
    public void getStreamDefinition() throws Exception {
        log.info("Executing JSAPI.getStreamDefinition");
        StreamDefinitionQueryBean bean = new StreamDefinitionQueryBean();
        bean.setName(STREAM_NAME);
        bean.setVersion(STREAM_VERSION);
        String url = TestConstants.ANALYTICS_JS_ENDPOINT + "?type=" + TYPE_GET_STREAM_DEFINITION;
        URL jsapiURL = new URL(url);
        HttpResponse response = HttpRequestUtil.doPost(jsapiURL, gson.toJson(bean), httpHeaders);
        log.info("Response: " + response.getData());
        ResponseBean responseBean = gson.fromJson(response.getData(), ResponseBean.class);
        StreamDefinitionBean streamDefinitionBean = gson.fromJson(responseBean.getMessage(), StreamDefinitionBean.class);
        Assert.assertEquals(streamDefinitionBean.getVersion(), STREAM_VERSION);
        Assert.assertEquals(streamDefinitionBean.getName(), STREAM_NAME);
    }

    @Test(groups = "wso2.das", description = "Lists the tables", dependsOnMethods = "addStreamDefinition")
    public void listTables() throws Exception {
        log.info("Executing JSAPI.listTables");
        String url = TestConstants.ANALYTICS_JS_ENDPOINT + "?type=" + TYPE_LIST_TABLES;
        HttpResponse response = Utils.doGet(url, httpHeaders);
        log.info("Response: " + response.getData());
        Assert.assertTrue(response.getData().contains(STREAM_NAME.toUpperCase()));
        //Check if the tenant cant see the super tenants tables
        Assert.assertFalse(response.getData().contains(AnalyticsJSAPITestCase.STREAM_NAME.toUpperCase()));
    }

    @Test(groups = "wso2.das", description = "Check if the table exists or not", dependsOnMethods = "addStreamDefinition")
    public void tableExists() throws Exception{
        log.info("Executing JSAPI.tableExists");
        String url = TestConstants.ANALYTICS_JS_ENDPOINT + "?type=" + TYPE_TABLE_EXISTS +
                     "&tableName=" + STREAM_NAME;
        HttpResponse response = Utils.doGet(url, httpHeaders);
        log.info("Response: " + response.getData());
        ResponseBean responseBean = gson.fromJson(response.getData(), ResponseBean.class);
        Assert.assertTrue(responseBean.getStatus().equals("success"));
    }

    @Test(groups = "wso2.das", description = "Get the records by range info", dependsOnMethods = "publishEvent3")
    public void getByRangeWithoutOptionalParams() throws Exception{
        Thread.sleep(15000);
        log.info("Executing JSAPI.getRecordsByRange - without optional params");
        String url = TestConstants.ANALYTICS_JS_ENDPOINT + "?type=" + TYPE_GET_BY_RANGE +
                     "&tableName=" + STREAM_NAME + "&timeFrom=undefined&timeTo=undefined&start=undefined&count=undefined";
        HttpResponse response = Utils.doGet(url, httpHeaders);
        log.info("Response: " + response.getData());
        ResponseBean responseBean = gson.fromJson(response.getData(), ResponseBean.class);
        Assert.assertTrue(responseBean.getStatus().equals("success"));
    }

    @Test(groups = "wso2.das", description = "Get the records by range info", dependsOnMethods = "getByRangeWithoutOptionalParams")
    public void getByRangeWithOptionalParams() throws Exception{
        log.info("Executing JSAPI.getRecordsByRange - With optional params");
        int start = 0;
        int count = 10;
        long from = System.currentTimeMillis() - ONE_HOUR_IN_MILLISECONDS;
        long to = System.currentTimeMillis() + ONE_HOUR_IN_MILLISECONDS;
        String url = TestConstants.ANALYTICS_JS_ENDPOINT + "?type=" + TYPE_GET_BY_RANGE +
                     "&tableName=" + STREAM_NAME + "&timeFrom=" + from + "&timeTo=" + to +
                     "&start=" + start + "&count=" + count;
        HttpResponse response = Utils.doGet(url, httpHeaders);
        log.info("Response: " + response.getData());
        ResponseBean responseBean = gson.fromJson(response.getData(), ResponseBean.class);
        Assert.assertTrue(responseBean.getStatus().equals("success"));
    }

    @Test(groups = "wso2.das", description = "Get the records by id info", dependsOnMethods = "getByRangeWithOptionalParams", enabled = true)
    public void getByIds() throws Exception{
        log.info("Executing JSAPI.getRecordsByIds");
        String url = TestConstants.ANALYTICS_JS_ENDPOINT + "?type=" + TYPE_GET_BY_ID +
                     "&tableName=" + STREAM_NAME;
        URL jsapiURL = new URL(url);
        String[] ids = new String[]{"001", "002", "003"};
        HttpResponse response = HttpRequestUtil.doPost(jsapiURL, gson.toJson(ids), httpHeaders);
        log.info("Response: " + response.getData());
        ResponseBean responseBean = gson.fromJson(response.getData(), ResponseBean.class);
        Assert.assertTrue(responseBean.getStatus().equals("success"));
    }

    @Test(groups = "wso2.das", description = "Get the record count", dependsOnMethods = "getByRangeWithOptionalParams")
    public void getRecordCount() throws Exception {
        log.info("Executing JSAPI.getRecordsCount");
        String url = TestConstants.ANALYTICS_JS_ENDPOINT + "?type=" + TYPE_GET_RECORD_COUNT +
                "&tableName=" + STREAM_NAME;
        HttpResponse response = Utils.doGet(url, httpHeaders);
        log.info("Response: " + response.getData());
        ResponseBean responseBean = gson.fromJson(response.getData(), ResponseBean.class);
        Assert.assertTrue(responseBean.getStatus().equals("success"));
        String count = responseBean.getMessage();
        if (!count.equals("-1")) {
            Assert.assertEquals(count, "3");
        }
    }

    @Test(groups = "wso2.das", description = "Get the search count", dependsOnMethods = "getRecordCount", enabled = true)
    public void searchCount() throws Exception{
        log.info("Executing JSAPI.searchCount");
        String url = TestConstants.ANALYTICS_JS_ENDPOINT + "?type=" + TYPE_SEARCH_COUNT +
                     "&tableName=" + STREAM_NAME;
        URL jsapiURL = new URL(url);
        QueryBean bean = new QueryBean();
        bean.setQuery("name : AAA");
        boolean codeOK = false;
        int counter = 0;
        while (!codeOK) {
            HttpResponse response = HttpRequestUtil.doPost(jsapiURL, gson.toJson(bean), httpHeaders);
            log.info("Response: " + response.getData());
            ResponseBean responseBean = gson.fromJson(response.getData(), ResponseBean.class);
            codeOK = (responseBean.getStatus().equals("success")) && (responseBean.getMessage().equals("1"));
            if (!codeOK) {
                Thread.sleep(2000L);
            }
            if (counter == 10) {
                Assert.assertTrue(responseBean.getStatus().equals("success"));
                Assert.assertEquals(responseBean.getMessage(), "1");
            }
            counter++;
        }
    }

    @Test(groups = "wso2.das", description = "Get the records which match the search query", dependsOnMethods = "searchCount",enabled = true)
    public void search() throws Exception{
        log.info("Executing JSAPI.search");
        String url = TestConstants.ANALYTICS_JS_ENDPOINT + "?type=" + TYPE_SEARCH +
                     "&tableName=" + STREAM_NAME;
        URL jsapiURL = new URL(url);
        QueryBean bean = new QueryBean();
        bean.setQuery("name : BBB");
        bean.setCount(10);
        HttpResponse response = HttpRequestUtil.doPost(jsapiURL, gson.toJson(bean), httpHeaders);
        log.info("Response: " + response.getData());
        ResponseBean responseBean = gson.fromJson(response.getData(), ResponseBean.class);
        Assert.assertTrue(responseBean.getStatus().equals("success"));
    }

    @Test(groups = "wso2.das", description = "Get the record count", dependsOnMethods = "getRecordCount")
    public void getSchema() throws Exception{
        log.info("Executing JSAPI.getSchema");
        String url = TestConstants.ANALYTICS_JS_ENDPOINT + "?type=" + TYPE_GET_SCHEMA +
                     "&tableName=" + STREAM_NAME;
        HttpResponse response = Utils.doGet(url, httpHeaders);
        log.info("Response: " + response.getData());
        ResponseBean responseBean = gson.fromJson(response.getData(), ResponseBean.class);
        Assert.assertTrue(responseBean.getStatus().equals("success"));
        AnalyticsSchemaBean bean = gson.fromJson(responseBean.getMessage(), AnalyticsSchemaBean.class);
        Assert.assertTrue(bean.getColumns().size() == 4);
        Assert.assertTrue(bean.getPrimaryKeys().size() == 2);
    }
}
