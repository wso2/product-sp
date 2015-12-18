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
package org.wso2.das.integration.tests.eventstreampersist;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.analytics.api.CarbonAnalyticsAPI;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataServiceUtils;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;
import org.wso2.carbon.analytics.stream.persistence.stub.dto.AnalyticsTable;
import org.wso2.carbon.analytics.stream.persistence.stub.dto.AnalyticsTableRecord;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.extensions.servers.utils.ClientConnectionUtil;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.das.integration.common.clients.EventStreamPersistenceClient;
import org.wso2.das.integration.common.utils.DASIntegrationTest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AnalyticsSchemaTestCase extends DASIntegrationTest {

    private static final String TABLE_NAME = "smart_home_data";

    private EventStreamPersistenceClient persistenceClient;
    private AnalyticsDataAPI analyticsAPI;
    private String streamResourceDir;

    private ServerConfigurationManager serverManager;

    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        super.init();
        String session = getSessionCookie();
        persistenceClient = new EventStreamPersistenceClient(backendURL, session);
        String apiConf =
                new File(this.getClass().getClassLoader().
                        getResource("dasconfig" + File.separator + "api" + File.separator + "analytics-data-config.xml").toURI())
                        .getAbsolutePath();
        analyticsAPI = new CarbonAnalyticsAPI(apiConf);
        streamResourceDir = FrameworkPathUtil.getSystemResourceLocation() + "eventstreampersist" + File.separator;
        serverManager = new ServerConfigurationManager(dasServer);

        analyticsAPI.deleteTable(MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME);
    }

    @Test(groups = "wso2.das", description = "Test backend availability of persistence service")
    public void testBackendAvailability() throws Exception {
        this.init();
        Assert.assertTrue(persistenceClient.isBackendServicePresent(), "Persistence backend could not be found!");
    }

    @Test(groups = "wso2.das", description = "Check merging of schema with existing, through service call")
    public void testSchemaMergeWithServiceCall() throws Exception {
        AnalyticsTable table = this.getSampleAnalyticsTable(true);
        persistenceClient = new EventStreamPersistenceClient(backendURL, super.getSessionCookie());
        persistenceClient.addAnalyticsTable(table);
        boolean found = false;
        int counter = 0;
        while (!found) {
            if (counter == 20) {
                throw new RuntimeException("Timed out waiting for table to be persisted!");
            }
            Thread.sleep(1000L);
            found = analyticsAPI.tableExists(MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME);
            counter++;
        }
        AnalyticsSchema originalSchema = analyticsAPI.getTableSchema(MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME);
        AnalyticsSchema testSchema = this.getSampleSchema();
        analyticsAPI.setTableSchema(MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME, testSchema);
        serverManager.restartGracefully();
        ClientConnectionUtil.waitForPort(10143, 150000, false, "localhost"); //9443 + 700 = 10143
        AnalyticsSchema mergedSchema = AnalyticsDataServiceUtils.createMergedSchema(originalSchema,
                testSchema.getPrimaryKeys(),
                new ArrayList<>(testSchema.getColumns().values()),
                new ArrayList<>(testSchema.getIndexedColumns().keySet()));
        AnalyticsSchema resultSchema = analyticsAPI.getTableSchema(MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME);
        try {
            Assert.assertEquals(mergedSchema, resultSchema, "Schema merge operation was not carried over after restart");
        } finally {
            analyticsAPI.deleteTable(MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME);
        }
    }

    @Test(groups = "wso2.das", description = "Check merging of schema with existing, based on artifact deployment")
    public void testSchemaMergeWithArtifactDeployment() throws Exception {
        analyticsAPI.deleteTable(MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME);
        this.deployStreamDefinition();
        this.deployEventSink();
        Thread.sleep(20000L);
        AnalyticsSchema originalSchema = analyticsAPI.getTableSchema(MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME);
        AnalyticsSchema testSchema = this.getSampleSchema();
        analyticsAPI.setTableSchema(MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME, testSchema);
        serverManager.restartGracefully();
        ClientConnectionUtil.waitForPort(10143, 150000L, false, "localhost"); //9443 + 700 = 10143
        AnalyticsSchema mergedSchema = AnalyticsDataServiceUtils.createMergedSchema(originalSchema,
                testSchema.getPrimaryKeys(),
                new ArrayList<>(testSchema.getColumns().values()),
                new ArrayList<>(testSchema.getIndexedColumns().keySet()));
        AnalyticsSchema resultSchema = analyticsAPI.getTableSchema(MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME);
        try {
            Assert.assertEquals(mergedSchema, resultSchema, "Schema merge operation was not carried over after restart");
        } finally {
            this.undeployStreamDefinition();
            this.undeployEventSink();
            analyticsAPI.deleteTable(MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME);
        }
    }

    @Test(groups = "wso2.das", description = "Check non-merging of schema", dependsOnMethods = "testSchemaMergeWithServiceCall")
    public void testSchemaNonMergeWithServiceCall() throws Exception {
        AnalyticsTable table = this.getSampleAnalyticsTable(false);
        persistenceClient = new EventStreamPersistenceClient(backendURL, super.getSessionCookie());
        persistenceClient.addAnalyticsTable(table);
        boolean found = false;
        int counter = 0;
        while (!found) {
            if (counter == 20) {
                throw new RuntimeException("Timed out waiting for table to be persisted!");
            }
            Thread.sleep(1000L);
            found = analyticsAPI.tableExists(MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME);
            counter++;
        }
        AnalyticsSchema originalSchema = analyticsAPI.getTableSchema(MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME);
        AnalyticsSchema testSchema = this.getSampleSchema();
        analyticsAPI.setTableSchema(MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME, testSchema);
        serverManager.restartGracefully();
        ClientConnectionUtil.waitForPort(10143, 150000, true, "localhost");
        AnalyticsSchema resultSchema = analyticsAPI.getTableSchema(MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME);
        try {
            Assert.assertEquals(originalSchema, resultSchema, "Schema merge operation has occurred even when set not to");
        } finally {
            analyticsAPI.deleteTable(MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME);
        }
    }

    private void deployStreamDefinition() throws IOException {
        String streamsLocation = FrameworkPathUtil.getCarbonHome() + File.separator + "repository"
                + File.separator + "deployment" + File.separator + "server" + File.separator + "eventstreams" + File.separator;
        FileManager.copyResourceToFileSystem(streamResourceDir + "smart.home.data_1.0.0.json",
                streamsLocation, "smart.home.data_1.0.0.json");
    }

    private void deployEventSink() throws IOException {
        String streamsLocation = FrameworkPathUtil.getCarbonHome() + File.separator + "repository"
                + File.separator + "deployment" + File.separator + "server" + File.separator + "eventsink" + File.separator;
        FileManager.copyResourceToFileSystem(streamResourceDir + "smart_home_data.xml",
                streamsLocation, "smart_home_data.xml");
    }

    private void undeployStreamDefinition() throws IOException {
        String streamsLocation = FrameworkPathUtil.getCarbonHome() + File.separator + "repository"
                + File.separator + "deployment" + File.separator + "server" + File.separator + "eventstreams" + File.separator;
        FileManager.deleteFile(streamsLocation + "smart.home.data_1.0.0.json");
    }

    private void undeployEventSink() throws IOException {
        String streamsLocation = FrameworkPathUtil.getCarbonHome() + File.separator + "repository"
                + File.separator + "deployment" + File.separator + "server" + File.separator + "eventsink" + File.separator;
        FileManager.deleteFile(streamsLocation + "smart_home_data.xml");
    }

    private AnalyticsSchema getSampleSchema() {
        List<ColumnDefinition> columns = new ArrayList<>();
        List<String> primaryKeys = new ArrayList<>();
        columns.add(new ColumnDefinition("house_id", AnalyticsSchema.ColumnType.INTEGER, true, false));
        columns.add(new ColumnDefinition("device_id", AnalyticsSchema.ColumnType.INTEGER, true, false));
        primaryKeys.add("device_id");
        columns.add(new ColumnDefinition("state", AnalyticsSchema.ColumnType.STRING, true, false));
        columns.add(new ColumnDefinition("max_usage", AnalyticsSchema.ColumnType.DOUBLE, false, false));
        return new AnalyticsSchema(columns, primaryKeys);
    }

    private AnalyticsTable getSampleAnalyticsTable(boolean mergeSchema) {
        AnalyticsTable table = new AnalyticsTable();
        table.setPersist(true);
        table.setMergeSchema(mergeSchema);
        table.setTableName(TABLE_NAME);
        table.setStreamVersion("1.0.0");
        AnalyticsTableRecord[] records = new AnalyticsTableRecord[4];

        AnalyticsTableRecord house_id = new AnalyticsTableRecord();
        house_id.setPersist(true);
        house_id.setPrimaryKey(true);
        house_id.setIndexed(true);
        house_id.setColumnName("house_id");
        house_id.setColumnType("INTEGER");
        house_id.setScoreParam(false);
        records[0] = house_id;

        AnalyticsTableRecord metro_area = new AnalyticsTableRecord();
        metro_area.setPersist(true);
        metro_area.setPrimaryKey(false);
        metro_area.setIndexed(true);
        metro_area.setColumnName("metro_area");
        metro_area.setColumnType("STRING");
        metro_area.setScoreParam(false);
        records[1] = metro_area;

        AnalyticsTableRecord power_reading = new AnalyticsTableRecord();
        power_reading.setPersist(true);
        power_reading.setPrimaryKey(false);
        power_reading.setIndexed(false);
        power_reading.setColumnName("power_reading");
        power_reading.setColumnType("FLOAT");
        power_reading.setScoreParam(false);
        records[2] = power_reading;

        AnalyticsTableRecord is_peak = new AnalyticsTableRecord();
        is_peak.setPersist(true);
        is_peak.setPrimaryKey(false);
        is_peak.setIndexed(false);
        is_peak.setColumnName("is_peak");
        is_peak.setColumnType("BOOLEAN");
        is_peak.setScoreParam(false);
        records[3] = is_peak;

        table.setAnalyticsTableRecords(records);
        return table;
    }
}