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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.analytics.api.CarbonAnalyticsAPI;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.das.analytics.rest.beans.RecordBean;
import org.wso2.das.integration.common.utils.DASIntegrationTest;
import org.wso2.das.integration.common.utils.TestConstants;
import org.wso2.das.integration.common.utils.Utils;

import java.io.File;
import java.net.URL;
import java.util.*;

public class GetSelectedColumnsRestAPITestCase extends DASIntegrationTest {
    private static final Log log = LogFactory.getLog(GetSelectedColumnsRestAPITestCase.class);
    private static final String TABLE_NAME = "Person";
    private Map<String, String> headers;
    private Map<String, Object> valueSet1;
    private Map<String, Object> valueSet2;
    private RecordBean record1;
    private RecordBean record2;
    AnalyticsDataAPI analyticsDataAPI;

    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        super.init();
        headers = new HashMap<>();
        headers.put("Content-Type", TestConstants.CONTENT_TYPE_JSON);
        headers.put("Accept", TestConstants.CONTENT_TYPE_JSON);
        headers.put("Authorization", TestConstants.BASE64_ADMIN_ADMIN);

        valueSet1 = new LinkedHashMap<>();
        valueSet1.put("name", "das");
        valueSet1.put("age", "2");

        valueSet2 = new LinkedHashMap<>();
        valueSet2.put("name", "BAN");
        valueSet2.put("age", "3");

        record1 = new RecordBean();
        record1.setTableName(TABLE_NAME);
        record1.setValues(valueSet1);
        record2 = new RecordBean();
        record2.setTableName(TABLE_NAME);
        record2.setValues(valueSet2);

        String apiConf = new File(this.getClass().getClassLoader().
                getResource("dasconfig" + File.separator + "api" + File.separator + "analytics-data-config.xml").toURI())
                .getAbsolutePath();
        analyticsDataAPI = new CarbonAnalyticsAPI(apiConf);
    }

    @Test(groups = "wso2.das", description = "Create table")
    public void createTable() throws Exception {
        log.info("Executing create table test case ...");
        analyticsDataAPI.createTable(org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME);
    }

    @Test(groups = "wso2.das", description = "Checks if table exists", dependsOnMethods = "createTable")
    public void tableExists() throws Exception {
        log.info("Executing Table Exist test case ...");
        HttpResponse response = Utils.doGet(TestConstants.ANALYTICS_ENDPOINT_URL +
                TestConstants.TABLE_EXISTS + TABLE_NAME, headers);
        log.info("Response: " + response.getData());
        Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
    }

    @Test(groups = "wso2.das", description = "Create table schema", dependsOnMethods = "tableExists")
    public void setTableSchema() throws Exception {
        log.info("Executing createTableSchema test case ...");
        List<ColumnDefinition> columns = new ArrayList<>();
        columns.add(new ColumnDefinition("name", AnalyticsSchema.ColumnType.STRING, true, false));
        columns.add(new ColumnDefinition("age", AnalyticsSchema.ColumnType.STRING, true, false));
        List<String> primaryKeys = new ArrayList<>();
        primaryKeys.add("name");
        AnalyticsSchema analyticsSchema = new AnalyticsSchema(columns, primaryKeys);
        analyticsDataAPI.setTableSchema(org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME, analyticsSchema);
    }

    @Test(groups = "wso2.das", description = "Test table schema", dependsOnMethods = "setTableSchema")
    public void getTableSchema() throws Exception {
        log.info("Executing getTableSchema test case ...");
        HttpResponse response = Utils.doGet(TestConstants.ANALYTICS_TABLES_ENDPOINT_URL +
                TABLE_NAME + TestConstants.SCHEMA, headers);
        log.info("Response: " + response.getData());
        Assert.assertFalse(response.getData().contains("{}"), "Schema is not set");
        Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
    }

    @Test(groups = "wso2.das", description = "Create records without optional parameters", dependsOnMethods = "getTableSchema")
    public void createRecordsWithoutOptionalParams() throws Exception {
        log.info("Executing create records without Optional Parameters test case ...");
        List<Record> records = new ArrayList<>();
        records.add(new Record(org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME, valueSet1));
        records.add(new Record(org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME, valueSet2));
        analyticsDataAPI.put(records);
    }

    @Test(groups = "wso2.das", description = "Get the record count of a table", dependsOnMethods = "createRecordsWithoutOptionalParams")
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

    @Test(groups = "wso2.das", description = "Getting selected columns from primary key search ...", dependsOnMethods = "getRecordCount")
    public void getSelectedColumns() throws Exception {
        log.info("Testing if only the selected columns are returned in primary key search...");
        URL restUrl = new URL(TestConstants.ANALYTICS_TABLES_ENDPOINT_URL + TABLE_NAME + "/keyed_records");
        String query = "{\n" +
                "\"valueBatches\" : [{ \"name\" : \"das\"}],\n" +
                "  \"columns\" : [\"age\"]\n" +
                "}";
        HttpResponse response = HttpRequestUtil.doPost(restUrl, query, headers);
        log.info("Response: " + response.getData());
        Assert.assertTrue(response.getData().contains("\"values\":{\"age\":\"2\"}"), "Search result not found");
    }
}

