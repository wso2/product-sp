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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.stream.persistence.stub.dto.AnalyticsTable;
import org.wso2.carbon.analytics.stream.persistence.stub.dto.AnalyticsTableRecord;
import org.wso2.carbon.analytics.webservice.stub.beans.StreamDefAttributeBean;
import org.wso2.carbon.analytics.webservice.stub.beans.StreamDefinitionBean;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.das.integration.common.clients.AnalyticsWebServiceClient;
import org.wso2.das.integration.common.clients.DataPublisherClient;
import org.wso2.das.integration.common.clients.EventStreamPersistenceClient;
import org.wso2.das.integration.common.utils.BAMIntegrationTest;

import java.io.File;
import java.io.IOException;

public class EventStreamPersistenceTestCase extends BAMIntegrationTest {

    private EventStreamPersistenceClient persistenceClient;
    private DataPublisherClient dataPublisherClient;
    private AnalyticsWebServiceClient webServiceClient;
    private static final String TABLE1 = "org.wso2.carbon.integration.test.table1";
    private static final String STREAM_VERSION = "1.0.0";

    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        super.init();
        String session = getSessionCookie();
        persistenceClient = new EventStreamPersistenceClient(backendURL, session);
        webServiceClient = new AnalyticsWebServiceClient(backendURL, session);
    }

    @Test(groups = "wso2.das", description = "Adding new analytics table")
    public void addAnalyticsTable() throws Exception {
        StreamDefinitionBean streamDefinitionBean = getEventStreamBeanTable1();
        webServiceClient.addStreamDefinition(streamDefinitionBean);
        AnalyticsTable table = getAnalyticsTable1();
        persistenceClient.addAnalyticsTable(table);
        Thread.sleep(15000);
    }

    @Test(groups = "wso2.das", description = "Get new analytics table", dependsOnMethods = "addAnalyticsTable")
    public void getAnalyticsTable() throws Exception {
        AnalyticsTable analyticsTable = persistenceClient.getAnalyticsTable(TABLE1, STREAM_VERSION);
        Assert.assertEquals(analyticsTable.getAnalyticsTableRecords().length, 2, "Table column count is wrong");
        Assert.assertEquals(analyticsTable.getPersist(), true, "Table persistence state is wrong");
    }

    @Test(groups = "wso2.das", description = "Check event stream persistence", dependsOnMethods = "getAnalyticsTable")
    public void checkDataPersistence() throws Exception {
        deployEventReceivers();
        Thread.sleep(20000);
        Event event = new Event(null, System.currentTimeMillis(),
                                new Object[0], new Object[0], new Object[]{(long) 1, "Test Event 1"});
        dataPublisherClient = new DataPublisherClient();
        dataPublisherClient.publish(TABLE1, STREAM_VERSION, event);
        Thread.sleep(10000);
        dataPublisherClient.shutdown();
        Assert.assertEquals(webServiceClient.getRecordCount(TABLE1.replace('.', '_'), 0, Long.MAX_VALUE), 1, "Record count is invalid");
    }

    @Test(groups = "wso2.das", description = "Check event stream persistence removing", dependsOnMethods =
            "checkDataPersistence")
    public void checkPersistenceRemoving() throws Exception {
        AnalyticsTable table = getAnalyticsTable1();
        table.setPersist(false);
        persistenceClient.addAnalyticsTable(table);
        Thread.sleep(15000);
        Event event = new Event(null, System.currentTimeMillis(),
                                new Object[0], new Object[0], new Object[]{(long) 2, "Test Event 2"});
        dataPublisherClient = new DataPublisherClient();
        dataPublisherClient.publish(TABLE1, STREAM_VERSION, event);
        Thread.sleep(10000);
        dataPublisherClient.shutdown();
        Assert.assertEquals(webServiceClient.getRecordCount(TABLE1.replace('.', '_'), 0, Long.MAX_VALUE), 1,
                            "Record count is invalid");
    }

    @Test(groups = "wso2.das", description = "Check event stream persistence removing", dependsOnMethods =
            "checkPersistenceRemoving")
    public void resumeEventPersistence() throws Exception {
        AnalyticsTable table = getAnalyticsTable1();
        table.setPersist(true);
        persistenceClient.addAnalyticsTable(table);
        Thread.sleep(15000);
        Event event = new Event(null, System.currentTimeMillis(),
                                new Object[0], new Object[0], new Object[]{(long) 2, "Test Event 2"});
        dataPublisherClient = new DataPublisherClient();
        dataPublisherClient.publish(TABLE1, STREAM_VERSION, event);
        Thread.sleep(10000);
        dataPublisherClient.shutdown();
        Assert.assertEquals(webServiceClient.getRecordCount(TABLE1.replace('.', '_'), 0, Long.MAX_VALUE), 2,
                            "Record count is invalid");
    }

    private void deployEventReceivers() throws IOException {
        String streamResourceDir = FrameworkPathUtil.getSystemResourceLocation() + "eventstreampersist" + File.separator;
        String streamsLocation = FrameworkPathUtil.getCarbonHome() + File.separator + "repository"
                                 + File.separator + "deployment" + File.separator + "server" + File.separator + "eventreceivers" + File.separator;
        FileManager.copyResourceToFileSystem(streamResourceDir + "test_table_1.xml", streamsLocation, "test_table_1.xml");
    }

    private AnalyticsTable getAnalyticsTable1() {
        AnalyticsTable table = new AnalyticsTable();
        table.setPersist(true);
        table.setTableName(TABLE1);
        table.setStreamVersion(STREAM_VERSION);
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

    private StreamDefinitionBean getEventStreamBeanTable1() {
        StreamDefinitionBean definitionBean = new StreamDefinitionBean();
        definitionBean.setName(TABLE1);
        definitionBean.setVersion(STREAM_VERSION);
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
}
