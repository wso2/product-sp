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
import org.wso2.carbon.analytics.jsservice.AnalyticsWebServiceConnector;
import org.wso2.carbon.analytics.jsservice.beans.EventBean;
import org.wso2.carbon.analytics.jsservice.beans.ResponseBean;
import org.wso2.carbon.analytics.jsservice.beans.StreamDefinitionBean;
import org.wso2.carbon.analytics.stream.persistence.stub.dto.AnalyticsTable;
import org.wso2.carbon.analytics.stream.persistence.stub.dto.AnalyticsTableRecord;
import org.wso2.carbon.analytics.webservice.stub.beans.RecordBean;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.das.integration.common.clients.AnalyticsWebServiceClient;
import org.wso2.das.integration.common.clients.EventStreamPersistenceClient;
import org.wso2.das.integration.common.utils.DASIntegrationTest;
import org.wso2.das.integration.common.utils.TestConstants;

import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class contains integration tests for Javascript analytics API
 */
public class AnalyticsJSAPITestCase extends DASIntegrationTest {
    private Log log = LogFactory.getLog(AnalyticsJSAPITestCase.class);
    private static final String STREAM_NAME = "sampleStream";
    private static final String STREAM_VERSION = "1.0.0";
    private static final String STREAM_DESCRIPTION = "Sample Description";
    private Gson gson;
    private Map<String, String> httpHeaders;

    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        super.init();
        gson = new Gson();
        httpHeaders = new HashMap<>();
        httpHeaders.put("Content-Type", TestConstants.CONTENT_TYPE_JSON);
        httpHeaders.put("Accept", TestConstants.CONTENT_TYPE_JSON);
        httpHeaders.put("Authorization", TestConstants.BASE64_ADMIN_ADMIN);
    }

    @Test(groups = "wso2.das", description = "Adds a stream definition")
    public void addStreamDefinition() throws Exception {
        log.info("Executing JSAPI.addStreamDefinition");
        StreamDefinitionBean streamDefinitionBean = new StreamDefinitionBean();
        Map<String, String> metaData = new LinkedHashMap<>();
        Map<String, String> payloadData = new LinkedHashMap<>();
        Map<String, String> correlationData = new LinkedHashMap<>();
        metaData.put("timestamp", "LONG");
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
                     "?type=" + AnalyticsWebServiceConnector.TYPE_ADD_STREAM_DEFINITION;
        URL jsapiURL = new URL(url);
        HttpResponse response = HttpRequestUtil.doPost(jsapiURL, gson.toJson(streamDefinitionBean),httpHeaders);
        log.info("response: " + response.getData());
        Assert.assertEquals(response.getResponseCode(), 201, "response code is different");
        ResponseBean responseBean = gson.fromJson(response.getData(), ResponseBean.class);
        Assert.assertEquals(responseBean.getStatus(), "created", "Response status is not 'Created'");
        Assert.assertEquals(responseBean.getMessage(), "sampleStream:1.0.0", "Response message is different");
        Thread.sleep(10000);
        String session = getSessionCookie();
        EventStreamPersistenceClient persistenceClient = new EventStreamPersistenceClient(backendURL, session);
        persistenceClient.addAnalyticsTable(getAnalyticsTable());
        Thread.sleep(10000);
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
        name.setPrimaryKey(false);
        name.setIndexed(true);
        name.setColumnName("name");
        name.setColumnType("STRING");
        name.setScoreParam(false);
        records[1] = name;
        AnalyticsTableRecord married = new AnalyticsTableRecord();
        married.setPersist(true);
        married.setPrimaryKey(false);
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
        table.setAnalyticsTableRecords(records);
        return table;
    }

    @Test(groups = "wso2.das", description = "publishes an event to an event stream", dependsOnMethods = "addStreamDefinition")
    public void publishEvent() throws Exception {
        log.info("executing JSAPI.publishEvent");
        EventBean eventBean = new EventBean();
        eventBean.setTimeStamp(new Date().getTime());
        eventBean.setStreamName(STREAM_NAME);
        eventBean.setStreamVersion(STREAM_VERSION);
        Map<String, Object> metaData = new HashMap<>();
        metaData.put("timestamp", new Date().getTime());
        Map<String, Object> payloadData = new HashMap<>();
        payloadData.put("name", "Gimantha");
        payloadData.put("department", "['WSO2', 'Engineering', 'R&D']");
        payloadData.put("married", false);
        eventBean.setMetaData(metaData);
        eventBean.setPayloadData(payloadData);
        String url = TestConstants.ANALYTICS_JS_ENDPOINT + "?type=" + AnalyticsWebServiceConnector.TYPE_PUBLISH_EVENT;
        URL jsapiURL = new URL(url);
        HttpResponse response = HttpRequestUtil.doPost(jsapiURL, gson.toJson(eventBean), httpHeaders);
        log.info("Response: " + response.getData());
        Thread.sleep(5000);
        String session = getSessionCookie();
        AnalyticsWebServiceClient webServiceClient = new AnalyticsWebServiceClient(backendURL, session);
        RecordBean[] dd = webServiceClient.getByRange(STREAM_NAME, Long.MIN_VALUE, Long.MAX_VALUE, 0, 100);
        log.info(dd.length);

    }
}
