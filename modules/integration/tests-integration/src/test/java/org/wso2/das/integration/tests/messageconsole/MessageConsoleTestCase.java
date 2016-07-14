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
import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.analytics.api.CarbonAnalyticsAPI;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.analytics.messageconsole.stub.beans.PermissionBean;
import org.wso2.carbon.analytics.messageconsole.stub.beans.ScheduleTaskInfo;
import org.wso2.carbon.analytics.stream.persistence.stub.dto.AnalyticsTable;
import org.wso2.carbon.analytics.stream.persistence.stub.dto.AnalyticsTableRecord;
import org.wso2.carbon.analytics.webservice.stub.beans.StreamDefAttributeBean;
import org.wso2.carbon.analytics.webservice.stub.beans.StreamDefinitionBean;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.das.integration.common.clients.AnalyticsWebServiceClient;
import org.wso2.das.integration.common.clients.DataPublisherClient;
import org.wso2.das.integration.common.clients.EventReceiverClient;
import org.wso2.das.integration.common.clients.EventStreamPersistenceClient;
import org.wso2.das.integration.common.clients.MessageConsoleClient;
import org.wso2.das.integration.common.utils.DASIntegrationTest;
import org.wso2.das.integration.common.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Tests related to the message console functionality.
 */
public class MessageConsoleTestCase extends DASIntegrationTest {

    private static final String TABLE1 = "integration.test.messageconsole.table1";
    private static final String STREAM_VERSION_1 = "1.0.0";
    private MessageConsoleClient messageConsoleClient;
    private AnalyticsWebServiceClient webServiceClient;
    private EventStreamPersistenceClient persistenceClient;
    private EventReceiverClient eventReceiverClient;
    private AnalyticsDataAPI analyticsDataAPI;
    private DataPublisherClient dataPublisherClient;

    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        super.init();
        String session = getSessionCookie();
        this.dataPublisherClient = new DataPublisherClient();
        this.messageConsoleClient = new MessageConsoleClient(this.backendURL, session);
        this.webServiceClient = new AnalyticsWebServiceClient(this.backendURL, session);
        this.persistenceClient = new EventStreamPersistenceClient(this.backendURL, session);
        this.eventReceiverClient = new EventReceiverClient(this.backendURL, session);
        String apiConf = new File(this.getClass().getClassLoader().getResource("dasconfig" + File.separator + "api" + 
                File.separator + "analytics-data-config.xml").toURI()).getAbsolutePath();
        this.analyticsDataAPI = new CarbonAnalyticsAPI(apiConf);
        this.analyticsDataAPI.deleteTable(MultitenantConstants.SUPER_TENANT_ID, GenericUtils.streamToTableName(TABLE1));
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws Exception {
        this.dataPublisherClient.shutdown();
        this.undeployEventReceivers();
        this.messageConsoleClient.scheduleDataPurgingTask(GenericUtils.streamToTableName(TABLE1), null, 0);
        this.analyticsDataAPI.deleteTable(MultitenantConstants.SUPER_TENANT_ID, GenericUtils.streamToTableName(TABLE1));
    }

    @Test(groups = "wso2.das", description = "Adding script with task")
    public void scheduleTask() throws Exception {
        StreamDefinitionBean streamDefTable1Version1 = getEventStreamBeanTable1Version1();
        this.webServiceClient.addStreamDefinition(streamDefTable1Version1);
        AnalyticsTable table1Version1 = getAnalyticsTable1Version1();
        this.persistenceClient.addAnalyticsTable(table1Version1);
        Utils.checkAndWaitForStreamAndPersist(this.webServiceClient, this.persistenceClient, TABLE1, STREAM_VERSION_1);
        deployEventReceivers();
        List<Event> events = new ArrayList<>(100);
        for (int i = 0; i < 100; i++) {
            Event event = new Event(null, System.currentTimeMillis(),
                    new Object[0], new Object[0], new Object[] { (long) i, String.valueOf(i) });
            events.add(event);
        }
        this.dataPublisherClient.publish(TABLE1, STREAM_VERSION_1, events);
        Utils.checkAndWaitForTableSize(this.webServiceClient, GenericUtils.streamToTableName(TABLE1), 100);
        this.messageConsoleClient.scheduleDataPurgingTask(GenericUtils.streamToTableName(TABLE1), "/10 * * * * ?", -1);
        Utils.checkAndWaitForTableSize(this.webServiceClient, GenericUtils.streamToTableName(TABLE1), 0);
    }

    @Test(groups = "wso2.das", description = "Get purging task information", dependsOnMethods = "scheduleTask")
    public void getDataPurgingDetails() throws Exception {
        ScheduleTaskInfo dataPurgingDetails = messageConsoleClient.getDataPurgingDetails(GenericUtils.streamToTableName(TABLE1));
        Assert.assertEquals(dataPurgingDetails.getCronString(), "/10 * * * * ?", "Cron expression wrong");
        Assert.assertEquals(dataPurgingDetails.getRetentionPeriod(), -1, "Retention period is wrong");
    }

    @Test(groups = "wso2.das", description = "Test permissions", dependsOnMethods = "getDataPurgingDetails")
    public void getAvailablePermissions() throws Exception {
        PermissionBean permissions = messageConsoleClient.getAvailablePermissions();
        Assert.assertTrue(permissions.getListRecord(), "Returning invalid result.");
        Assert.assertTrue(permissions.getListTable(), "Returning invalid result.");
        Assert.assertTrue(permissions.getDeleteRecord(), "Returning invalid result.");
        Assert.assertTrue(permissions.getSearchRecord(), "Returning invalid result.");
        System.out.println("permissions.toString() = " + permissions.toString());
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

    private void deployEventReceivers() throws Exception {
        boolean status = this.eventReceiverClient.addOrUpdateEventReceiver("messageconsole.table1", getResourceContent(
                MessageConsoleTestCase.class, "messageconsole" + File.separator +  "messageconsole.table1.xml"));
        Assert.assertTrue(status);
    }
    
    private void undeployEventReceivers() throws Exception {
        this.eventReceiverClient.undeployEventReceiver("messageconsole.table1");
    }
    
}
