/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.automation.platform.tests.apim.das;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.analytics.api.CarbonAnalyticsAPI;
import org.wso2.carbon.analytics.stream.persistence.stub.dto.AnalyticsTable;
import org.wso2.carbon.analytics.stream.persistence.stub.dto.AnalyticsTableRecord;
import org.wso2.carbon.analytics.webservice.stub.beans.AnalyticsSchemaBean;
import org.wso2.carbon.analytics.webservice.stub.beans.EventBean;
import org.wso2.carbon.analytics.webservice.stub.beans.RecordValueEntryBean;
import org.wso2.carbon.analytics.webservice.stub.beans.StreamDefAttributeBean;
import org.wso2.carbon.analytics.webservice.stub.beans.StreamDefinitionBean;
import org.wso2.das.integration.common.clients.AnalyticsWebServiceClient;
import org.wso2.das.integration.common.clients.DataPublisherClient;
import org.wso2.das.integration.common.clients.EventStreamPersistenceClient;
import org.wso2.das.integration.common.utils.DASIntegrationTest;

import java.io.File;
import java.util.Arrays;

/**
 * Note: This test case is not run with default DAS integration tests. To run this test we assume
 * that DAS server is running with a load balancer in front. All the servers should be configured
 * properly.
 */

public class DASLoadbalancerAccessTestCase extends DASIntegrationTest {

    private static final String TABLE1 = "integration.test.analytics.webservice.table1";
    private static final String TABLE2 = "integration.test.analytics.webservice.table2";
    private static final String STREAM_VERSION_1 = "1.0.0";
    private AnalyticsWebServiceClient webServiceClient;
    private EventStreamPersistenceClient persistenceClient;
    private DataPublisherClient dataPublisherClient;
    private AnalyticsDataAPI analyticsDataAPI;

    @BeforeClass(groups = {"wso2.das"}, alwaysRun = true)
    protected void init() throws Exception {
        super.init();
        String apiConf =
                new File(this.getClass().getClassLoader().
                        getResource("dasconfig" + File.separator + "api" + File.separator + "analytics-data-config.xml").toURI())
                        .getAbsolutePath();
        analyticsDataAPI = new CarbonAnalyticsAPI(apiConf);
        String session = getSessionCookie();
        webServiceClient = new AnalyticsWebServiceClient(backendURL, session);
        persistenceClient = new EventStreamPersistenceClient(backendURL, session);
    }

    @Test(groups = "wso2.das", description = "Adding valid stream definition")
    public void addStreamDefinition() throws Exception {
        init();
        StreamDefinitionBean streamDefinitionBean = getEventStreamBeanTable1Version1();
        webServiceClient.addStreamDefinition(streamDefinitionBean);
        Thread.sleep(5000);
        StreamDefinitionBean streamDefinition = webServiceClient.getStreamDefinition(TABLE1, STREAM_VERSION_1);
        Assert.assertEquals(streamDefinition.getName(), TABLE1, "Stream name mismatch");
        Assert.assertNotNull(streamDefinition.getPayloadData(), "Empty payload array");
        Assert.assertEquals(streamDefinition.getPayloadData().length, 2, "Invalid payload data count");
    }


    @Test(groups = "wso2.das", description = "Publish event", dependsOnMethods = "addStreamDefinition")
    public void publishEvent() throws Exception {
        StreamDefinitionBean streamDefTable2Version1 = getEventStreamBeanTable2Version1();
        webServiceClient.addStreamDefinition(streamDefTable2Version1);
        EventBean eventBean = new EventBean();
        eventBean.setStreamName(TABLE2);
        eventBean.setStreamVersion(STREAM_VERSION_1);
        RecordValueEntryBean[] payloadData = new RecordValueEntryBean[2];
        RecordValueEntryBean uuid = new RecordValueEntryBean();
        uuid.setFieldName("uuid");
        uuid.setType("LONG");
        uuid.setLongValue(123);
        payloadData[0] = uuid;
        RecordValueEntryBean name = new RecordValueEntryBean();
        name.setFieldName("name");
        name.setType("STRING");
        name.setStringValue("DAS");
        payloadData[1] = name;
        eventBean.setPayloadData(payloadData);
        webServiceClient.publishEvent(eventBean);
    }

    @Test(groups = "wso2.das", description = "Check get table schema", dependsOnMethods = "addStreamDefinition")
    public void getTableSchema() throws Exception {
        AnalyticsTable table1Version1 = getAnalyticsTable1Version1();
        persistenceClient.addAnalyticsTable(table1Version1);
        Thread.sleep(15000);
        AnalyticsSchemaBean tableSchema = webServiceClient.getTableSchema(TABLE1.replace('.', '_'));
        Assert.assertNotNull(table1Version1, "Getting null table schema object");
        Assert.assertEquals(tableSchema.getColumns().length, 3, "Getting invalid column count");
        Assert.assertEquals(tableSchema.getPrimaryKeys().length, 1, "Getting invalid primary key count");
    }

    @Test(groups = "wso2.das", description = "Get table list", dependsOnMethods = "getTableSchema")
    public void listTables() throws Exception {
        AnalyticsTable table2Version1 = getAnalyticsTable2Version1();
        persistenceClient.addAnalyticsTable(table2Version1);
        Thread.sleep(15000);
        String[] tables = webServiceClient.listTables();
        Assert.assertNotNull(tables, "Return null array");
        Assert.assertTrue(Arrays.asList(tables).contains(TABLE1.replace('.', '_').toUpperCase()), "Table1 name does not return");
        Assert.assertTrue(Arrays.asList(tables).contains(TABLE2.replace('.', '_').toUpperCase()), "Table2 name does not return");
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


    private AnalyticsTable getAnalyticsTable1Version1() {
        AnalyticsTable table = new AnalyticsTable();
        table.setPersist(true);
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

    private StreamDefinitionBean getEventStreamBeanTable2Version1() {
        StreamDefinitionBean definitionBean = new StreamDefinitionBean();
        definitionBean.setName(TABLE2);
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

    private AnalyticsTable getAnalyticsTable2Version1() {
        AnalyticsTable table = new AnalyticsTable();
        table.setPersist(true);
        table.setTableName(TABLE2);
        table.setStreamVersion(STREAM_VERSION_1);
        AnalyticsTableRecord[] records = new AnalyticsTableRecord[2];
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
        table.setAnalyticsTableRecords(records);
        return table;
    }


}
