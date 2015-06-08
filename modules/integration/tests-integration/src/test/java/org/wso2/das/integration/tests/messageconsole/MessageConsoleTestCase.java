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

package org.wso2.das.integration.tests.messageconsole;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.messageconsole.stub.beans.PermissionBean;
import org.wso2.carbon.analytics.messageconsole.stub.beans.ScheduleTaskInfo;
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
import org.wso2.das.integration.common.clients.MessageConsoleClient;
import org.wso2.das.integration.common.utils.DASIntegrationTest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MessageConsoleTestCase extends DASIntegrationTest {

    private static final String TABLE1 = "integration.test.messageconsole.table1";
    private static final String STREAM_VERSION_1 = "1.0.0";
    private MessageConsoleClient messageConsoleClient;
    private AnalyticsWebServiceClient webServiceClient;
    private EventStreamPersistenceClient persistenceClient;

    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        super.init();
        String session = getSessionCookie();
        messageConsoleClient = new MessageConsoleClient(backendURL, session);
        webServiceClient = new AnalyticsWebServiceClient(backendURL, session);
        persistenceClient = new EventStreamPersistenceClient(backendURL, session);
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws Exception {
        messageConsoleClient.scheduleDataPurgingTask(TABLE1.replace('.', '_'), null, 0);
    }

    @Test(groups = "wso2.das", description = "Adding script with task")
    public void scheduleTask() throws Exception {
        StreamDefinitionBean streamDefTable1Version1 = getEventStreamBeanTable1Version1();
        webServiceClient.addStreamDefinition(streamDefTable1Version1);
        AnalyticsTable table1Version1 = getAnalyticsTable1Version1();
        persistenceClient.addAnalyticsTable(table1Version1);
        deployEventReceivers();
        Thread.sleep(15000);
        List<Event> events = new ArrayList<>(100);
        for (int i = 0; i < 100; i++) {
            Event event = new Event(null, System.currentTimeMillis(),
                                    new Object[0], new Object[0], new Object[]{(long) i, String.valueOf(i)});
            events.add(event);
        }
        publishEvents(events);
        Assert.assertEquals(webServiceClient.getRecordCount(TABLE1.replace('.', '_'), 0, System.currentTimeMillis()),
                            100, "Record count is invalid");
        messageConsoleClient.scheduleDataPurgingTask(TABLE1.replace('.', '_'), "30 * * * * ?", -1);
        Thread.sleep(90000);
        Assert.assertEquals(webServiceClient.getRecordCount(TABLE1.replace('.', '_'), 0, System.currentTimeMillis()),
                            0, "Record count is invalid");
    }

    @Test(groups = "wso2.das", description = "Get purging task information", dependsOnMethods = "scheduleTask")
    public void getDataPurgingDetails() throws Exception {
        ScheduleTaskInfo dataPurgingDetails = messageConsoleClient.getDataPurgingDetails(TABLE1.replace('.', '_'));
        Assert.assertEquals(dataPurgingDetails.getCronString(), "30 * * * * ?", "Cron expression wrong");
        Assert.assertEquals(dataPurgingDetails.getRetentionPeriod(), -1, "Retention period is wrong");
    }

    @Test(groups = "wso2.das", description = "Test permissions")
    public void getAvailablePermissions() throws Exception {
        PermissionBean permissions = messageConsoleClient.getAvailablePermissions();
        Assert.assertTrue(permissions.getListRecord(), "Returning invalid result.");
        Assert.assertTrue(permissions.getListTable(), "Returning invalid result.");
        Assert.assertTrue(permissions.getDeleteRecord(), "Returning invalid result.");
        Assert.assertTrue(permissions.getSearchRecord(), "Returning invalid result.");
        System.out.println("permissions.toString() = " + permissions.toString());
    }

    private void publishEvents(List<Event> events) throws Exception {
        DataPublisherClient dataPublisherClient = new DataPublisherClient();
        dataPublisherClient.publish(TABLE1, STREAM_VERSION_1, events);
        Thread.sleep(10000);
        dataPublisherClient.shutdown();
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

    private void deployEventReceivers() throws IOException {
        String streamResourceDir = FrameworkPathUtil.getSystemResourceLocation() + "messageconsole" + File.separator;
        String streamsLocation = FrameworkPathUtil.getCarbonHome() + File.separator + "repository"
                                 + File.separator + "deployment" + File.separator + "server" + File.separator + "eventreceivers" + File.separator;
        FileManager.copyResourceToFileSystem(streamResourceDir + "messageconsole.table1.xml", streamsLocation, "messageconsole.table1.xml");
    }
}
