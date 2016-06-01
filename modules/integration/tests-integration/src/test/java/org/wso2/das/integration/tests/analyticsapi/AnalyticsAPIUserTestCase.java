/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.das.integration.tests.analyticsapi;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.analytics.api.CarbonAnalyticsAPI;
import org.wso2.carbon.analytics.api.exception.AnalyticsServiceException;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataServiceUtils;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.das.integration.common.utils.DASIntegrationTest;

import java.io.File;
import java.util.*;

public class AnalyticsAPIUserTestCase extends DASIntegrationTest {
    private AnalyticsDataAPI analyticsDataAPI;
    private static final String CREATE_TABLE_NAME = "LogApiUserTable";
    private static final String DELETE_TABLE_NAME = "LogApiUserDeleteTable";
    private static final String LOG_FIELD = "log";
    private static final String IP_FIELD = "ip";
    private static final String LOG_TIMESTAMP = "logTimeStamp";
    private static final String USERNAME = "admin";
    private List<String> recordIds;


    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        super.init();
        String apiConf =
                new File(this.getClass().getClassLoader().
                        getResource("dasconfig" + File.separator + "api" + File.separator + "analytics-data-config.xml").toURI())
                        .getAbsolutePath();
        analyticsDataAPI = new CarbonAnalyticsAPI(apiConf);
        recordIds = new ArrayList<>();
        analyticsDataAPI.deleteTable(USERNAME, CREATE_TABLE_NAME);
        analyticsDataAPI.deleteTable(USERNAME, DELETE_TABLE_NAME);
    }

    @Test(groups = "wso2.das", description = "Adding a new table")
    public void createTableTest() throws Exception {
        String apiConf =
                new File(this.getClass().getClassLoader().
                        getResource("dasconfig" + File.separator + "api" + File.separator + "analytics-data-config.xml").toURI())
                        .getAbsolutePath();
        analyticsDataAPI = new CarbonAnalyticsAPI(apiConf);
        analyticsDataAPI.createTable(USERNAME, CREATE_TABLE_NAME);
        List<String> tableNames = analyticsDataAPI.listTables(MultitenantConstants.SUPER_TENANT_ID);
        Assert.assertTrue(isTableExists(CREATE_TABLE_NAME, tableNames));
    }

    @Test(groups = "wso2.das", description = "List tables")
    public void listTablesTest() throws AnalyticsServiceException, AnalyticsException {
        analyticsDataAPI.listTables(USERNAME);
    }

    @Test(groups = "wso2.das", description = "setting a schema for the table", dependsOnMethods = "createTableTest")
    public void setSchemaTest() throws AnalyticsException, AnalyticsServiceException {
        List<ColumnDefinition> columns = new ArrayList<>();
        columns.add(new ColumnDefinition(LOG_FIELD, AnalyticsSchema.ColumnType.STRING));
        columns.add(new ColumnDefinition(IP_FIELD, AnalyticsSchema.ColumnType.STRING));
        columns.add(new ColumnDefinition(LOG_TIMESTAMP, AnalyticsSchema.ColumnType.LONG));

        List<String> primaryKeys = new ArrayList<>();
        primaryKeys.add(LOG_TIMESTAMP);
        primaryKeys.add(IP_FIELD);
        AnalyticsSchema analyticsSchema = new AnalyticsSchema(columns, primaryKeys);
        analyticsDataAPI.setTableSchema(USERNAME, CREATE_TABLE_NAME, analyticsSchema);
    }

    @Test(groups = "wso2.das", description = "getting a schema for the table", dependsOnMethods = "setSchemaTest")
    public void getSchema() throws AnalyticsException, AnalyticsServiceException {
        AnalyticsSchema schema = analyticsDataAPI.getTableSchema(USERNAME, CREATE_TABLE_NAME);
        Assert.assertTrue(schema != null, "No schema returned!");
        Assert.assertEquals(schema.getColumns().get(LOG_FIELD).getType(), AnalyticsSchema.ColumnType.STRING,
                "Log field column type wasn't String type");
        Assert.assertEquals(schema.getColumns().get(IP_FIELD).getType(), AnalyticsSchema.ColumnType.STRING,
                "IP field column type wasn't String type");
        Assert.assertEquals(schema.getColumns().get(LOG_TIMESTAMP).getType(), AnalyticsSchema.ColumnType.LONG,
                "Log Timestamp field column type wasn't Long type");
        Assert.assertTrue(schema.getPrimaryKeys().contains(LOG_TIMESTAMP), "Log time stamp is not existing in the primary key fields");
        Assert.assertTrue(schema.getPrimaryKeys().contains(IP_FIELD), "IP field is not existing in the primary key fields");
    }

    @Test(groups = "wso2.das", description = "check whether table exists", dependsOnMethods = "createTableTest")
    public void tableExistsTest() throws AnalyticsServiceException, AnalyticsException {
        Assert.assertTrue(analyticsDataAPI.tableExists(USERNAME, CREATE_TABLE_NAME),
                "Created table " + CREATE_TABLE_NAME + " is not existing");
    }

    @Test(groups = "wso2.das", description = "delete table exists")
    public void deleteTableTest() throws AnalyticsServiceException, AnalyticsException {
        analyticsDataAPI.createTable(USERNAME, DELETE_TABLE_NAME);
        Assert.assertTrue(analyticsDataAPI.tableExists(USERNAME, DELETE_TABLE_NAME));
        analyticsDataAPI.deleteTable(USERNAME, DELETE_TABLE_NAME);
        Assert.assertFalse(analyticsDataAPI.tableExists(USERNAME, DELETE_TABLE_NAME));
    }

    @Test(groups = "wso2.das", description = "put records", dependsOnMethods = "createTableTest")
    public void putRecordsTest() throws AnalyticsException, AnalyticsServiceException {
        List<Record> records = new ArrayList<>();
        Map<String, Object> values = new HashMap<>();
        values.put(IP_FIELD, "127.0.0.1");
        values.put(LOG_FIELD, "some logggggggggg");
        values.put(LOG_TIMESTAMP, 12345567);
        for (int i = 0; i < 10; i++) {
            records.add(new Record(MultitenantConstants.SUPER_TENANT_ID, CREATE_TABLE_NAME, values));
        }
        analyticsDataAPI.put(records);
    }

    @Test(groups = "wso2.das", description = "put records", dependsOnMethods = "putRecordsTest")
    public void getRecordCountTest() throws AnalyticsException, AnalyticsServiceException {
        long recordCount = analyticsDataAPI.getRecordCount(USERNAME, CREATE_TABLE_NAME, Long.MIN_VALUE, Long.MAX_VALUE);
        if (recordCount != -1) {
            Assert.assertEquals(recordCount, 10, "Records inserted was 10, but found : " + recordCount);
        }
    }

    @Test(groups = "wso2.das", description = "get Range records", dependsOnMethods = "getRecordCountTest")
    public void getRangeRecordGroupTest() throws AnalyticsException, AnalyticsServiceException {
        List<String> cols = new ArrayList<>();
        cols.add(IP_FIELD);
        cols.add(LOG_FIELD);
        AnalyticsDataResponse analyticsDataResponse = analyticsDataAPI.get(USERNAME, CREATE_TABLE_NAME, 1, cols, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1);
        Assert.assertEquals(analyticsDataResponse.getEntries().size(), 1);
        Iterator<Record> recordIterator = AnalyticsDataServiceUtils.responseToIterator(analyticsDataAPI, analyticsDataResponse);
        int recordCount = 0;
        while (recordIterator.hasNext()) {
            Record record = recordIterator.next();
            recordIds.add(record.getId());
            recordCount++;
        }
        Assert.assertEquals(recordCount, 10);
    }

    @Test(groups = "wso2.das", description = "get records based on Ids", dependsOnMethods = "getRangeRecordGroupTest")
    public void getIdsRecordGroupTest() throws AnalyticsException, AnalyticsServiceException {
        List<String> cols = new ArrayList<>();
        cols.add(IP_FIELD);
        cols.add(LOG_FIELD);
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ids.add(recordIds.get(i));
        }
        AnalyticsDataResponse analyticsDataResponse = analyticsDataAPI.get(USERNAME, CREATE_TABLE_NAME, 1, cols, ids);
        Assert.assertEquals(analyticsDataResponse.getEntries().size(), 1);
        Iterator<Record> recordIterator = AnalyticsDataServiceUtils.responseToIterator(analyticsDataAPI, analyticsDataResponse);
        int recordCount = 0;
        while (recordIterator.hasNext()) {
            recordIterator.next();
            recordCount++;
        }
        Assert.assertEquals(recordCount, 3);
    }

    @Test(groups = "wso2.das", description = "delete records based on Ids", dependsOnMethods = "getIdsRecordGroupTest")
    public void deleteRecordIdsTest() throws AnalyticsException, AnalyticsServiceException {
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ids.add(recordIds.get(i));
        }
        analyticsDataAPI.delete(USERNAME, CREATE_TABLE_NAME, ids);
        long recordCount = analyticsDataAPI.getRecordCount(USERNAME, CREATE_TABLE_NAME,
                Long.MIN_VALUE, Long.MAX_VALUE);
        if (recordCount != -1) {
            Assert.assertEquals(recordCount, 7);
        }
    }

    @Test(groups = "wso2.das", description = "delete records based on range", dependsOnMethods = "deleteRecordIdsTest")
    public void deleteRecordRangeTest() throws AnalyticsException, AnalyticsServiceException {
        recordIds = new ArrayList<>();
        analyticsDataAPI.delete(USERNAME, CREATE_TABLE_NAME, Long.MIN_VALUE, Long.MAX_VALUE);
        long recordCount = analyticsDataAPI.getRecordCount(USERNAME, CREATE_TABLE_NAME,
                Long.MIN_VALUE, Long.MAX_VALUE);
        if (recordCount != -1) {
            Assert.assertEquals(recordCount, 0);
        }
    }

    @Test(groups = "wso2.das", description = "get record store name for table", dependsOnMethods = "deleteRecordRangeTest")
    public void getRecordStoreForTable() throws AnalyticsException, AnalyticsServiceException {
        recordIds = new ArrayList<>();
        String recordStoreName = analyticsDataAPI.getRecordStoreNameByTable(USERNAME, CREATE_TABLE_NAME);
        Assert.assertEquals(recordStoreName, "EVENT_STORE");
    }

    private boolean isTableExists(String tableName, List<String> tables) {
        if (tables != null) {
            for (String aTableName : tables) {
                if (aTableName.equalsIgnoreCase(tableName)) {
                    return true;
                }
            }
        }
        return false;
    }

}

