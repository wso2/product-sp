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

package org.wso2.das.integration.tests.eventstreampersist;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.analytics.api.CarbonAnalyticsAPI;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.analytics.stream.persistence.stub.dto.AnalyticsTable;
import org.wso2.carbon.analytics.stream.persistence.stub.dto.AnalyticsTableRecord;
import org.wso2.carbon.analytics.webservice.stub.beans.RecordBean;
import org.wso2.carbon.analytics.webservice.stub.beans.RecordValueEntryBean;
import org.wso2.carbon.analytics.webservice.stub.beans.StreamDefAttributeBean;
import org.wso2.carbon.analytics.webservice.stub.beans.StreamDefinitionBean;
import org.wso2.carbon.analytics.webservice.stub.beans.ValuesBatchBean;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.das.integration.common.clients.AnalyticsWebServiceClient;
import org.wso2.das.integration.common.clients.DataPublisherClient;
import org.wso2.das.integration.common.clients.EventReceiverClient;
import org.wso2.das.integration.common.clients.EventStreamPersistenceClient;
import org.wso2.das.integration.common.utils.DASIntegrationTest;
import org.wso2.das.integration.common.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * Event stream persistence related tests.
 */
public class EventStreamPersistenceTestCase extends DASIntegrationTest {

    private EventStreamPersistenceClient persistenceClient;
    private DataPublisherClient dataPublisherClient;
    private AnalyticsWebServiceClient webServiceClient;
    private EventReceiverClient eventReceiverClient;
    AnalyticsDataAPI analyticsDataAPI;
    private static final String TABLE1 = "integration.test.event.persist.table1";
    private static final String TABLE2 = "integration.test.event.persist.table2";
    private static final String TABLE3 = "integration.test.event.persist.table3";
    private static final String STREAM_VERSION_1 = "1.0.0";
    private static final String STREAM_VERSION_2 = "2.0.0";

    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        super.init();
        String session = getSessionCookie();
        this.persistenceClient = new EventStreamPersistenceClient(this.backendURL, session);
        this.webServiceClient = new AnalyticsWebServiceClient(this.backendURL, session);
        this.dataPublisherClient = new DataPublisherClient();
        this.eventReceiverClient = new EventReceiverClient(this.backendURL, session);
        String apiConf = new File(this.getClass().getClassLoader().getResource("dasconfig" + File.separator + 
                "api" + File.separator + "analytics-data-config.xml").toURI()).getAbsolutePath();
        this.analyticsDataAPI = new CarbonAnalyticsAPI(apiConf);
        this.analyticsDataAPI.deleteTable(MultitenantConstants.SUPER_TENANT_ID, "integration_test_event_persist_table1");
        this.analyticsDataAPI.deleteTable(MultitenantConstants.SUPER_TENANT_ID, "integration_test_event_persist_table2");
        this.analyticsDataAPI.deleteTable(MultitenantConstants.SUPER_TENANT_ID, "integration_test_event_persist_table3");
    }
    
    @AfterClass(alwaysRun = true)
    public void cleanup() throws Exception {
        this.dataPublisherClient.shutdown();
        this.analyticsDataAPI.deleteTable(MultitenantConstants.SUPER_TENANT_ID, "integration_test_event_persist_table1");
        this.analyticsDataAPI.deleteTable(MultitenantConstants.SUPER_TENANT_ID, "integration_test_event_persist_table2");
        this.analyticsDataAPI.deleteTable(MultitenantConstants.SUPER_TENANT_ID, "integration_test_event_persist_table3");
    }

    @Test(groups = "wso2.das", description = "Test backend availability of persistence service")
    public void testBackendAvailability() throws Exception {
        init();
        Assert.assertTrue(persistenceClient.isBackendServicePresent(), "Method returns value other than true");
    }

    @Test(groups = "wso2.das", description = "Adding new analytics table1", dependsOnMethods = "testBackendAvailability")
    public void addAnalyticsTable1() throws Exception {
        StreamDefinitionBean streamDefTable1Version1 = getEventStreamBeanTable1Version1();
        AnalyticsTable table1Version1 = getAnalyticsTable1Version1();
        Utils.addStreamAndPersistence(this.webServiceClient, this.persistenceClient, streamDefTable1Version1, table1Version1);
    }

    @Test(groups = "wso2.das", description = "Get new analytics1 table", dependsOnMethods = "addAnalyticsTable1")
    public void getAnalyticsTable1() throws Exception {
        AnalyticsTable analyticsTable = persistenceClient.getAnalyticsTable(TABLE1, STREAM_VERSION_1);
        Assert.assertEquals(analyticsTable.getAnalyticsTableRecords().length, 3, "Table column count is wrong");
        Assert.assertEquals(analyticsTable.getPersist(), true, "Table persistence state is wrong");
    }

    @Test(groups = "wso2.das", description = "Adding new analytics table with all type of column", dependsOnMethods = "getAnalyticsTable1")
    public void addAnalyticsTableWithAllTypes() throws Exception {
        StreamDefinitionBean streamDefinitionBean = getEventStreamBeanTable2();
        AnalyticsTable table = getAnalyticsTable2Version1();
        Utils.addStreamAndPersistence(this.webServiceClient, this.persistenceClient, streamDefinitionBean, table);
    }

    @Test(groups = "wso2.das", description = "Adding new analytics table2", dependsOnMethods = "addAnalyticsTableWithAllTypes")
    public void addAnalyticsTable1v2() throws Exception {
        StreamDefinitionBean streamDefTable1Version2 = getEventStreamBeanTable1Version2();
        AnalyticsTable table1Version2 = getAnalyticsTable1Version2();
        Utils.addStreamAndPersistence(this.webServiceClient, this.persistenceClient, streamDefTable1Version2, table1Version2);
    }

    @Test(groups = "wso2.das", description = "Get new analytics2 table", dependsOnMethods = "addAnalyticsTable1v2")
    public void getAnalyticsTable1v2() throws Exception {
        AnalyticsTable analyticsTable = persistenceClient.getAnalyticsTable(TABLE1, STREAM_VERSION_2);
        Assert.assertEquals(analyticsTable.getAnalyticsTableRecords().length, 4, "Table column count is wrong");
        Assert.assertEquals(analyticsTable.getPersist(), true, "Table persistence state is wrong");
    }

    @Test(groups = "wso2.das", description = "Check event stream persistence", dependsOnMethods = "getAnalyticsTable1")
    public void checkDataPersistence() throws Exception {
        deployEventReceivers();
        Thread.sleep(20000);
        publishEventTable1(1, "Test Event 1");
        Utils.checkAndWaitForTableSize(webServiceClient, GenericUtils.streamToTableName(TABLE1), 1);
    }

    @Test(groups = "wso2.das", description = "Check event stream persistence removing", dependsOnMethods =
            "checkDataPersistence")
    public void checkPersistenceRemoving() throws Exception {
        AnalyticsTable table = getAnalyticsTable1Version1();
        table.setPersist(false);
        persistenceClient.addAnalyticsTable(table);
        Utils.checkAndWaitForStreamAndPersist(this.webServiceClient, this.persistenceClient, TABLE1, STREAM_VERSION_1, false);
        publishEventTable1(2, "Test Event 2");
        Thread.sleep(5000);
        Utils.checkAndWaitForTableSize(webServiceClient, GenericUtils.streamToTableName(TABLE1), 1);
    }

    @Test(groups = "wso2.das", description = "Check event stream persistence removing", dependsOnMethods =
            "checkPersistenceRemoving")
    public void resumeEventPersistence() throws Exception {
        AnalyticsTable table = getAnalyticsTable1Version1();
        table.setPersist(true);
        persistenceClient.addAnalyticsTable(table);
        Utils.checkAndWaitForStreamAndPersist(this.webServiceClient, this.persistenceClient, TABLE1, STREAM_VERSION_1);
        publishEventTable1(2, "Test Event 2");
        Utils.checkAndWaitForTableSize(webServiceClient, GenericUtils.streamToTableName(TABLE1), 2);
    }

    @Test(groups = "wso2.das", description = "Check column level persistence", dependsOnMethods =
            "resumeEventPersistence")
    public void checkColumnLevelPersistence() throws Exception {
        RecordBean[] records = webServiceClient.getByRange(GenericUtils.streamToTableName(TABLE1), 0, Long.MAX_VALUE, 0, 100);
        Assert.assertTrue(checkFieldExistingInRecord(records[0], "Name"), "Name field is not existing in the records");
        AnalyticsTable table = getAnalyticsTable1Version1();
        
        table.getAnalyticsTableRecords()[0].setPrimaryKey(true);
        table.getAnalyticsTableRecords()[1].setPersist(false);
        persistenceClient.addAnalyticsTable(table);
        Utils.checkAndWaitForStreamAndPersistColumn(this.webServiceClient, this.persistenceClient, TABLE1, STREAM_VERSION_1, 
                table.getAnalyticsTableRecords()[1].getColumnName(), false);
        publishEventTable1(3, "Test Event 3");
        Utils.checkAndWaitForTableSize(this.webServiceClient, GenericUtils.streamToTableName(TABLE1), 3);
        
        table.getAnalyticsTableRecords()[1].setPersist(true);
        persistenceClient.addAnalyticsTable(table);
        Utils.checkAndWaitForStreamAndPersistColumn(this.webServiceClient, this.persistenceClient, TABLE1, STREAM_VERSION_1, 
                table.getAnalyticsTableRecords()[1].getColumnName(), true);
        publishEventTable1(4, "Test Event 4");
        Utils.checkAndWaitForTableSize(this.webServiceClient, GenericUtils.streamToTableName(TABLE1), 4);
        ValuesBatchBean[] batchBeans = new ValuesBatchBean[1];
        RecordValueEntryBean[] valueEntryBeans = new RecordValueEntryBean[1];
        RecordValueEntryBean entryBean = new RecordValueEntryBean();
        entryBean.setFieldName("uuid");
        entryBean.setStringValue("4");
        valueEntryBeans[0] = entryBean;
        ValuesBatchBean batchBean = new ValuesBatchBean();
        batchBeans[0] = batchBean;
        batchBean.setKeyValues(valueEntryBeans);
        int retry = 0;
        records = webServiceClient.getWithKeyValues(GenericUtils.streamToTableName(TABLE1), null, batchBeans);
        if (records != null) {
            RecordBean recordBean = null;
            outer:
            for (RecordBean record : records) {
                if (record != null && record.getValues() != null) {
                    for (RecordValueEntryBean recordValueEntryBean : record.getValues()) {
                        if ("UUID".equalsIgnoreCase(recordValueEntryBean.getFieldName()) && 4 == recordValueEntryBean
                                .getLongValue()) {
                            recordBean = record;
                            break outer;
                        }
                    }
                }
            }
            if (recordBean != null) {
                Assert.assertEquals(recordBean.getValues().length, 3, "Expected number of columns persisting is 3");
            }
        }
    }

    private boolean checkFieldExistingInRecord(RecordBean recordBean, String fieldName) {
        for (RecordValueEntryBean recordValueEntryBean : recordBean.getValues()) {
            if (recordValueEntryBean.getFieldName().equalsIgnoreCase(fieldName)) {
                return recordValueEntryBean.getStringValue() != null;
            }
        }
        return false;
    }

    @Test(groups = "wso2.das", description = "Update schema", dependsOnMethods = "checkColumnLevelPersistence")
    public void updateSchema() throws Exception {
        AnalyticsTable table1Version1 = getAnalyticsTable1Version1Updated();
        persistenceClient.addAnalyticsTable(table1Version1);
        Utils.checkAndWait(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                AnalyticsTable analyticsTable = persistenceClient.getAnalyticsTable(TABLE1, STREAM_VERSION_1);
                boolean contains = false;
                for (AnalyticsTableRecord analyticsTableRecord : analyticsTable.getAnalyticsTableRecords()) {
                    if ("school".equals(analyticsTableRecord.getColumnName())) {
                        contains = true;
                        break;
                    }
                }
                return contains;
            }
        });
    }

    @Test(groups = "wso2.das", description = "Check schema for invalid stream name", dependsOnMethods = "updateSchema")
    public void getSchemaForInvalidName() throws Exception {
        AnalyticsTable nonExist = persistenceClient.getAnalyticsTable("xyz", "1.0.0");
        Assert.assertFalse(nonExist.getPersist(), "Getting incorrect persist state");
        Assert.assertNull(nonExist.getAnalyticsTableRecords()[0], "Not getting empty array for columns");
    }

    @Test(groups = "wso2.das", description = "Check schema for invalid stream version", dependsOnMethods = "getSchemaForInvalidName")
    public void getSchemaForInvalidVersion() throws Exception {
        AnalyticsTable analyticsTable = persistenceClient.getAnalyticsTable(TABLE1, "3.0.0");
        Assert.assertFalse(analyticsTable.getPersist(), "Getting incorrect persist state");
        Assert.assertEquals(analyticsTable.getAnalyticsTableRecords().length, 5, "Table column count is wrong");
    }

    @Test(groups = "wso2.das", description = "Check stream updates by adding new attribute", dependsOnMethods = "getSchemaForInvalidVersion")
    public void checkStreamUpdateWithAddingNewAttribute() throws Exception {
        StreamDefinitionBean streamDefTable3 = getEventStreamBeanTable3();
        webServiceClient.addStreamDefinition(streamDefTable3);
        AnalyticsTable table3 = getAnalyticsTable3();
        persistenceClient.addAnalyticsTable(table3);
        Utils.checkAndWaitForStreamAndPersist(this.webServiceClient, this.persistenceClient, TABLE3, STREAM_VERSION_1);
        publishEventTable3(1);
        Utils.checkAndWaitForTableSize(this.webServiceClient, GenericUtils.streamToTableName(TABLE3), 1);
        webServiceClient.removeStreamDefinition(streamDefTable3);
        Utils.checkAndWaitForStream(this.webServiceClient, streamDefTable3.getName(), streamDefTable3.getVersion(), false);
        streamDefTable3 = getEventStreamBeanTable3Updated();
        webServiceClient.addStreamDefinition(streamDefTable3);
        table3 = getAnalyticsTable3Updated();
        persistenceClient.addAnalyticsTable(table3);
        Utils.checkAndWaitForStreamAndPersistColumn(this.webServiceClient, this.persistenceClient, TABLE3, STREAM_VERSION_1, "two", true);
        publishEventTable3Updated(1, 2);
        Utils.checkAndWaitForTableSize(this.webServiceClient, GenericUtils.streamToTableName(TABLE3), 2);
        this.eventReceiverClient.undeployEventReceiver("table3");
        this.eventReceiverClient.undeployEventReceiver("test_table_1");
        webServiceClient.removeStreamDefinition(streamDefTable3);
    }

    private void deployEventReceivers() throws Exception {
        /* the following are blocking calls */
        boolean status = this.eventReceiverClient.addOrUpdateEventReceiver("test_table_1", getResourceContent(
                EventStreamPersistenceTestCase.class, "eventstreampersist" + File.separator +  "test_table_1.xml"));
        status &= this.eventReceiverClient.addOrUpdateEventReceiver("table3", getResourceContent(
                EventStreamPersistenceTestCase.class, "eventstreampersist" + File.separator +  "table3.xml"));
        Assert.assertTrue(status);
    }

    private AnalyticsTable getAnalyticsTable1Version1() {
        AnalyticsTable table = new AnalyticsTable();
        table.setPersist(true);
        table.setMergeSchema(false);
        table.setTableName(TABLE1);
        table.setStreamVersion(STREAM_VERSION_1);
        AnalyticsTableRecord[] records = new AnalyticsTableRecord[3];
        AnalyticsTableRecord uuid = new AnalyticsTableRecord();
        uuid.setPersist(true);
        uuid.setPrimaryKey(true);
        uuid.setIndexed(true);
        uuid.setColumnName("uuid");
        uuid.setColumnType("LONG");
        uuid.setScoreParam(false);
        records[0] = uuid;
        AnalyticsTableRecord name = new AnalyticsTableRecord();
        name.setPersist(true);
        name.setPrimaryKey(false);
        name.setIndexed(false);
        name.setColumnName("name");
        name.setColumnType("STRING");
        name.setScoreParam(false);
        records[1] = name;
        AnalyticsTableRecord age = new AnalyticsTableRecord();
        age.setPersist(true);
        age.setPrimaryKey(false);
        age.setIndexed(false);
        age.setColumnName("_age");
        age.setColumnType("INTEGER");
        age.setScoreParam(false);
        records[2] = age;
        table.setAnalyticsTableRecords(records);
        return table;
    }

    private AnalyticsTable getAnalyticsTable1Version1Updated() {
        AnalyticsTable table = new AnalyticsTable();
        table.setPersist(true);
        table.setMergeSchema(false);
        table.setTableName(TABLE1);
        table.setStreamVersion(STREAM_VERSION_1);
        AnalyticsTableRecord[] records = new AnalyticsTableRecord[3];
        AnalyticsTableRecord uuid = new AnalyticsTableRecord();
        uuid.setPersist(true);
        uuid.setPrimaryKey(true);
        uuid.setIndexed(false);
        uuid.setColumnName("uuid");
        uuid.setColumnType("LONG");
        uuid.setScoreParam(false);
        records[0] = uuid;
        AnalyticsTableRecord school = new AnalyticsTableRecord();
        school.setPersist(true);
        school.setPrimaryKey(false);
        school.setIndexed(false);
        school.setColumnName("school");
        school.setColumnType("STRING");
        school.setScoreParam(false);
        records[1] = school;
        AnalyticsTableRecord grade = new AnalyticsTableRecord();
        grade.setPersist(true);
        grade.setPrimaryKey(false);
        grade.setIndexed(false);
        grade.setColumnName("_grade");
        grade.setColumnType("INTEGER");
        grade.setScoreParam(false);
        records[2] = grade;
        table.setAnalyticsTableRecords(records);
        return table;
    }

    private StreamDefinitionBean getEventStreamBeanTable1Version1() {
        StreamDefinitionBean definitionBean = new StreamDefinitionBean();
        definitionBean.setName(TABLE1);
        definitionBean.setVersion(STREAM_VERSION_1);
        StreamDefAttributeBean[] attributeBeans = new StreamDefAttributeBean[2];
        StreamDefAttributeBean uuid = new StreamDefAttributeBean();
        uuid.setName("uuid");
        uuid.setType("LONG");
        attributeBeans[0] = uuid;
        StreamDefAttributeBean name = new StreamDefAttributeBean();
        name.setName("name");
        name.setType("STRING");
        attributeBeans[1] = name;
        definitionBean.setPayloadData(attributeBeans);
        return definitionBean;
    }

    private AnalyticsTable getAnalyticsTable1Version2() {
        AnalyticsTable table = new AnalyticsTable();
        table.setPersist(true);
        table.setMergeSchema(false);
        table.setTableName(TABLE1);
        table.setStreamVersion(STREAM_VERSION_2);
        AnalyticsTableRecord[] records = new AnalyticsTableRecord[3];
        AnalyticsTableRecord uuid = new AnalyticsTableRecord();
        uuid.setPersist(true);
        uuid.setPrimaryKey(true);
        uuid.setIndexed(true);
        uuid.setColumnName("uuid");
        uuid.setColumnType("LONG");
        uuid.setScoreParam(false);
        records[0] = uuid;
        AnalyticsTableRecord empId = new AnalyticsTableRecord();
        empId.setPersist(true);
        empId.setPrimaryKey(true);
        empId.setIndexed(true);
        empId.setColumnName("empId");
        empId.setColumnType("LONG");
        empId.setScoreParam(false);
        records[1] = empId;
        AnalyticsTableRecord salary = new AnalyticsTableRecord();
        salary.setPersist(true);
        salary.setPrimaryKey(false);
        salary.setIndexed(false);
        salary.setColumnName("_salary");
        salary.setColumnType("FLOAT");
        salary.setScoreParam(false);
        records[2] = salary;
        table.setAnalyticsTableRecords(records);
        return table;
    }

    private StreamDefinitionBean getEventStreamBeanTable1Version2() {
        StreamDefinitionBean definitionBean = new StreamDefinitionBean();
        definitionBean.setName(TABLE1);
        definitionBean.setVersion(STREAM_VERSION_2);
        StreamDefAttributeBean[] attributeBeans = new StreamDefAttributeBean[2];
        StreamDefAttributeBean uuid = new StreamDefAttributeBean();
        uuid.setName("uuid");
        uuid.setType("LONG");
        attributeBeans[0] = uuid;
        StreamDefAttributeBean empId = new StreamDefAttributeBean();
        empId.setName("empId");
        empId.setType("LONG");
        attributeBeans[1] = empId;
        definitionBean.setPayloadData(attributeBeans);
        return definitionBean;
    }

    private AnalyticsTable getAnalyticsTable2Version1() {
        AnalyticsTable table = new AnalyticsTable();
        table.setPersist(true);
        table.setMergeSchema(false);
        table.setTableName(TABLE2);
        table.setStreamVersion(STREAM_VERSION_1);
        AnalyticsTableRecord[] records = new AnalyticsTableRecord[8];
        AnalyticsTableRecord col1 = new AnalyticsTableRecord();
        col1.setPersist(true);
        col1.setPrimaryKey(true);
        col1.setIndexed(true);
        col1.setColumnName("STRING");
        col1.setColumnType("STRING");
        col1.setScoreParam(false);
        records[0] = col1;
        AnalyticsTableRecord col2 = new AnalyticsTableRecord();
        col2.setPersist(true);
        col2.setPrimaryKey(false);
        col2.setIndexed(false);
        col2.setColumnName("INTEGER");
        col2.setColumnType("INTEGER");
        col2.setScoreParam(false);
        records[1] = col2;
        AnalyticsTableRecord col3 = new AnalyticsTableRecord();
        col3.setPersist(true);
        col3.setPrimaryKey(true);
        col3.setIndexed(true);
        col3.setColumnName("LONG");
        col3.setColumnType("LONG");
        col3.setScoreParam(false);
        records[2] = col3;
        AnalyticsTableRecord col4 = new AnalyticsTableRecord();
        col4.setPersist(true);
        col4.setPrimaryKey(false);
        col4.setIndexed(false);
        col4.setColumnName("BOOLEAN");
        col4.setColumnType("BOOLEAN");
        col4.setScoreParam(false);
        records[3] = col4;
        AnalyticsTableRecord col5 = new AnalyticsTableRecord();
        col5.setPersist(true);
        col5.setPrimaryKey(false);
        col5.setIndexed(false);
        col5.setColumnName("FLOAT");
        col5.setColumnType("FLOAT");
        col5.setScoreParam(false);
        records[4] = col5;
        AnalyticsTableRecord col6 = new AnalyticsTableRecord();
        col6.setPersist(true);
        col6.setPrimaryKey(true);
        col6.setIndexed(true);
        col6.setColumnName("_DOUBLE");
        col6.setColumnType("DOUBLE");
        col6.setScoreParam(false);
        records[5] = col6;
        AnalyticsTableRecord col7 = new AnalyticsTableRecord();
        col7.setPersist(true);
        col7.setPrimaryKey(false);
        col7.setIndexed(false);
        col7.setColumnName("_FACET");
        col7.setColumnType("FACET");
        col7.setScoreParam(false);
        records[6] = col7;
        AnalyticsTableRecord col8 = new AnalyticsTableRecord();
        col8.setPersist(true);
        col8.setPrimaryKey(false);
        col8.setIndexed(false);
        col8.setColumnName("_Default");
        col8.setColumnType("");
        col8.setScoreParam(false);
        records[7] = col8;
        table.setAnalyticsTableRecords(records);
        return table;
    }

    private StreamDefinitionBean getEventStreamBeanTable2() {
        StreamDefinitionBean definitionBean = new StreamDefinitionBean();
        definitionBean.setName(TABLE2);
        definitionBean.setVersion(STREAM_VERSION_1);
        StreamDefAttributeBean[] payloads = new StreamDefAttributeBean[5];
        StreamDefAttributeBean col1 = new StreamDefAttributeBean();
        col1.setName("STRING");
        col1.setType("STRING");
        payloads[0] = col1;
        StreamDefAttributeBean col2 = new StreamDefAttributeBean();
        col2.setName("INTEGER");
        col2.setType("INTEGER");
        payloads[1] = col2;
        StreamDefAttributeBean col3 = new StreamDefAttributeBean();
        col3.setName("LONG");
        col3.setType("LONG");
        payloads[2] = col3;
        StreamDefAttributeBean col4 = new StreamDefAttributeBean();
        col4.setName("BOOLEAN");
        col4.setType("BOOLEAN");
        payloads[3] = col4;
        StreamDefAttributeBean col5 = new StreamDefAttributeBean();
        col5.setName("FLOAT");
        col5.setType("FLOAT");
        payloads[4] = col5;
        definitionBean.setPayloadData(payloads);
        return definitionBean;
    }

    private StreamDefinitionBean getEventStreamBeanTable3() {
        StreamDefinitionBean definitionBean = new StreamDefinitionBean();
        definitionBean.setName(TABLE3);
        definitionBean.setVersion(STREAM_VERSION_1);
        StreamDefAttributeBean[] payloads = new StreamDefAttributeBean[1];
        StreamDefAttributeBean col1 = new StreamDefAttributeBean();
        col1.setName("one");
        col1.setType("INTEGER");
        payloads[0] = col1;
        definitionBean.setPayloadData(payloads);
        return definitionBean;
    }

    private AnalyticsTable getAnalyticsTable3() {
        AnalyticsTable table = new AnalyticsTable();
        table.setPersist(true);
        table.setMergeSchema(false);
        table.setTableName(TABLE3);
        table.setStreamVersion(STREAM_VERSION_1);
        AnalyticsTableRecord[] records = new AnalyticsTableRecord[1];
        AnalyticsTableRecord col1 = new AnalyticsTableRecord();
        col1.setPersist(true);
        col1.setPrimaryKey(false);
        col1.setIndexed(false);
        col1.setColumnName("one");
        col1.setColumnType("INTEGER");
        col1.setScoreParam(false);
        records[0] = col1;
        table.setAnalyticsTableRecords(records);
        return table;
    }

    private StreamDefinitionBean getEventStreamBeanTable3Updated() {
        StreamDefinitionBean definitionBean = new StreamDefinitionBean();
        definitionBean.setName(TABLE3);
        definitionBean.setVersion(STREAM_VERSION_1);
        StreamDefAttributeBean[] payloads = new StreamDefAttributeBean[2];
        StreamDefAttributeBean col1 = new StreamDefAttributeBean();
        col1.setName("one");
        col1.setType("INTEGER");
        payloads[0] = col1;
        StreamDefAttributeBean col2 = new StreamDefAttributeBean();
        col2.setName("two");
        col2.setType("INTEGER");
        payloads[1] = col2;
        definitionBean.setPayloadData(payloads);
        return definitionBean;
    }

    private AnalyticsTable getAnalyticsTable3Updated() {
        AnalyticsTable table = new AnalyticsTable();
        table.setPersist(true);
        table.setMergeSchema(false);
        table.setTableName(TABLE3);
        table.setStreamVersion(STREAM_VERSION_1);
        AnalyticsTableRecord[] records = new AnalyticsTableRecord[2];
        AnalyticsTableRecord col1 = new AnalyticsTableRecord();
        col1.setPersist(true);
        col1.setPrimaryKey(false);
        col1.setIndexed(false);
        col1.setColumnName("one");
        col1.setColumnType("INTEGER");
        col1.setScoreParam(false);
        records[0] = col1;
        AnalyticsTableRecord col2 = new AnalyticsTableRecord();
        col1.setPersist(true);
        col1.setPrimaryKey(false);
        col1.setIndexed(false);
        col1.setColumnName("two");
        col1.setColumnType("INTEGER");
        col1.setScoreParam(false);
        records[1] = col1;
        table.setAnalyticsTableRecords(records);
        return table;
    }

    private void publishEventTable1(long id, String name) throws Exception {
        Event event = new Event(null, System.currentTimeMillis(), new Object[0], new Object[0], new Object[]{id, name});
        this.dataPublisherClient.publish(TABLE1, STREAM_VERSION_1, event);
    }

    private void publishEventTable3(int one) throws Exception {
        Event event = new Event(null, System.currentTimeMillis(), new Object[0], new Object[0], new Object[]{one});
        dataPublisherClient = new DataPublisherClient();
        this.dataPublisherClient.publish(TABLE3, STREAM_VERSION_1, event);
    }

    private void publishEventTable3Updated(int one, int two) throws Exception {
        Event event = new Event(null, System.currentTimeMillis(), new Object[0], new Object[0], new Object[]{one, two});
        dataPublisherClient = new DataPublisherClient();
        this.dataPublisherClient.publish(TABLE3, STREAM_VERSION_1, event);
    }
    
}
