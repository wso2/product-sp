/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.das.integration.tests.analytics.execution;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.analytics.api.CarbonAnalyticsAPI;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema.ColumnType;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.analytics.spark.admin.stub.AnalyticsProcessorAdminServiceStub;
import org.wso2.carbon.analytics.spark.admin.stub.AnalyticsProcessorAdminServiceStub.AnalyticsQueryResultDto;
import org.wso2.carbon.analytics.spark.admin.stub.AnalyticsProcessorAdminServiceStub.AnalyticsRowResultDto;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.das.integration.common.utils.DASIntegrationTest;

public class CompressedEventAnalyticsTestsCase extends DASIntegrationTest {
    
    private static final Log log = LogFactory.getLog(CompressedEventAnalyticsTestsCase.class);
    private static final String ANALYTICS_SERVICE_NAME = "AnalyticsProcessorAdminService";
    private static final String COMPRESSED_EVENTS_TABLE = "COMPRESSED_EVENTS_TABLE";
    private static final String DECOMPRESSED_EVENTS_TABLE = "DecompressedEventsTable";
    private static final String MESSAGE_FLOW_ID = "urn_uuid_f403b0b6-4431-4a83-935d-c7b72867a111";
    private static final String FLOW_DATA_FIELD= "flowData";
    private static final String COMPRESSED_FLAG_FIELD= "meta_compressed";
    private AnalyticsDataAPI analyticsDataAPI;
    private AnalyticsProcessorAdminServiceStub analyticsStub;
    
    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        super.init();
        String apiConf = new File(this.getClass().getClassLoader().getResource("dasconfig" + File.separator + "api"
                + File.separator + "analytics-data-config.xml").toURI()).getAbsolutePath();
        this.analyticsDataAPI = new CarbonAnalyticsAPI(apiConf);
        deleteTableIfExists(COMPRESSED_EVENTS_TABLE);
        deleteTableIfExists(DECOMPRESSED_EVENTS_TABLE);
        populateSampleData();
        initializeStub();
        String query = "CREATE TEMPORARY TABLE " + DECOMPRESSED_EVENTS_TABLE + " USING CompressedEventAnalytics " +
                "OPTIONS(tableName \"" + COMPRESSED_EVENTS_TABLE + "\", schema \"messageFlowId STRING, " +
                "compotentType STRING, componentName STRING, compotentIndex INT, componentId STRING, startTime LONG," +
                " endTime LONG, duration LONG, beforePayload STRING, afterPayload STRING, contextPropertyMap STRING," +
                " transportPropertyMap STRING, children STRING, entryPoint STRING, entryPointHashcode INT, " +
                "faultCount INT, hashCode INT, host STRING, _tenantId INT, _timestamp LONG\", " +
                "incrementalParams \"esbFlowEvent, MINUTE\", mergeSchema \"false\", globalTenantAccess \"true\")";
        this.analyticsStub.execute(query);
    }

    @Test(groups = "wso2.das", description = "Testing decompressed event counts")
    public void testDecompressedEventCounts() throws Exception {
        // Check overall events count
        AnalyticsQueryResultDto queryResult = this.analyticsStub.executeQuery("SELECT * FROM " +
                DECOMPRESSED_EVENTS_TABLE);
        AnalyticsRowResultDto [] rowResults =  queryResult.getRowsResults();
        Assert.assertEquals(rowResults.length, 54, "Incorrect number of rows after decompressing.");
        // Check selective events count
        rowResults =  this.analyticsStub.executeQuery("SELECT * FROM " + DECOMPRESSED_EVENTS_TABLE + " WHERE " +
                "messageFlowId=\"" + MESSAGE_FLOW_ID + "\"").getRowsResults();
        Assert.assertEquals(rowResults.length, 27, "Incorrect number of rows after decompressing.");
        // Check attribute count in a single event
        Assert.assertEquals(rowResults[0].getColumnValues().length, 20, "Incorrect number of fileds in an event," +
                " after decompressing.");
    }
    
    @Test(groups = "wso2.das", description = "Testing decompressed events content", 
            dependsOnMethods = "testDecompressedEventCounts")
    public void testDecompressedEventContent() throws Exception {
        AnalyticsRowResultDto [] rowResults =  this.analyticsStub.executeQuery("SELECT * FROM " + 
                DECOMPRESSED_EVENTS_TABLE + " WHERE messageFlowId=\"" + MESSAGE_FLOW_ID + "\"").getRowsResults();
        log.info("Checking row content after decompressing..");
        for (int i = 0 ; i < rowResults.length ; i++) {
            String [] fields = rowResults[i].getColumnValues();
            Assert.assertEquals(fields[0], MESSAGE_FLOW_ID, "Incorrect message Id.");
            String componentType;
            if (i == 0) {
                componentType = "Proxy Service";
            } else {
                componentType = "Mediator";
            }
            Assert.assertEquals(fields[1], componentType, "Incorrect component type.");
            Assert.assertEquals(fields[2], "compName"+i, "Incorrect component name.");
            Assert.assertEquals(Integer.parseInt(fields[3]), i, "Incorrect component index.");
            Assert.assertEquals(fields[4], "compId"+i, "Incorrect component Id.");
            //This is a stats-only event. Hence payload/properties should be null
            for(int j = 8 ; j < 12 ; j++) {
                Assert.assertEquals(fields[j], null, "Incorrect payloads/properties.");
            }
        }
    }
    
    @Test(groups = "wso2.das", description = "Testing schema of the original table", 
            dependsOnMethods = "testDecompressedEventCounts")
    public void testSchemaAfterDecompressing() throws AnalyticsException {
        AnalyticsSchema schema = this.analyticsDataAPI.getTableSchema(MultitenantConstants.SUPER_TENANT_ID, 
                COMPRESSED_EVENTS_TABLE);
        Map<String, ColumnDefinition> columns = schema.getColumns();
        Assert.assertEquals(columns.size(), 2, "Compressed Events table's schema has changed after decompressing");
    }
    
    private void initializeStub() throws Exception {
        ConfigurationContext configContext = ConfigurationContextFactory.
                createConfigurationContextFromFileSystem(null);
        String loggedInSessionCookie = getSessionCookie();
        analyticsStub = new AnalyticsProcessorAdminServiceStub(configContext,
                backendURL + "/services/" + ANALYTICS_SERVICE_NAME);
        ServiceClient client = analyticsStub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, loggedInSessionCookie);
    }
    
    private void populateSampleData() throws AnalyticsException, URISyntaxException {
        //Creating sample tables used to test scripts.
        log.info("Creating table: " + COMPRESSED_EVENTS_TABLE + " for Compressed Event Analytics test case");
        this.analyticsDataAPI.createTable(MultitenantConstants.SUPER_TENANT_ID, COMPRESSED_EVENTS_TABLE);
        
        // Set schema
        List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
        columns.add(new ColumnDefinition(COMPRESSED_FLAG_FIELD, ColumnType.BOOLEAN));
        columns.add(new ColumnDefinition(FLOW_DATA_FIELD, ColumnType.STRING));
        AnalyticsSchema schema = new  AnalyticsSchema(columns, null);
        this.analyticsDataAPI.setTableSchema(MultitenantConstants.SUPER_TENANT_ID, COMPRESSED_EVENTS_TABLE, schema);
        
        //Push some events to the table
        log.info("Inserting events to table: " + COMPRESSED_EVENTS_TABLE);
        List<Record> recordList = generateCompressedEventsRecords(MultitenantConstants.SUPER_TENANT_ID, 
                COMPRESSED_EVENTS_TABLE, false);
        this.analyticsDataAPI.put(recordList);
    }
    
    private void deleteTableIfExists(String tableName) throws AnalyticsException {
        this.analyticsDataAPI.deleteTable(MultitenantConstants.SUPER_TENANT_ID, tableName);
    }
    
    private List<Record> generateCompressedEventsRecords(int tenantId, String tableName,
        boolean generateRecordIds) throws AnalyticsException {
        List<Record> records = new ArrayList<>();
        Map<String, Object> values;
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        String[] sampleData = null;
        try {
            sampleData = IOUtils.toString(classLoader.getResourceAsStream("analytics" + File.separator + "sample-data" +
                    File.separator + "CompressedEventData")).split("\n");
        } catch (IOException e) {
            throw new AnalyticsException(e.getMessage());
        }
        long timestamp;
        for (int j = 0; j < sampleData.length; j++) {
            values = new HashMap<>();
            String [] fields = sampleData[j].split(",",2);
            values.put(COMPRESSED_FLAG_FIELD, Boolean.parseBoolean(fields[0]));
            values.put(FLOW_DATA_FIELD, fields[1]);
            timestamp = System.currentTimeMillis();
            records.add(new Record(generateRecordIds ? GenericUtils.generateRecordID() : null, tenantId, tableName,
                values, timestamp));
        }
        return records;
    }
}
