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

package org.wso2.automation.platform.tests;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.stream.persistence.stub.dto.AnalyticsTable;
import org.wso2.carbon.analytics.stream.persistence.stub.dto.AnalyticsTableRecord;
import org.wso2.carbon.analytics.webservice.stub.beans.AnalyticsSchemaBean;
import org.wso2.carbon.analytics.webservice.stub.beans.RecordBean;
import org.wso2.carbon.analytics.webservice.stub.beans.StreamDefAttributeBean;
import org.wso2.carbon.analytics.webservice.stub.beans.StreamDefinitionBean;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.das.integration.common.clients.AnalyticsWebServiceClient;
import org.wso2.das.integration.common.clients.DataPublisherClient;
import org.wso2.das.integration.common.clients.EventStreamPersistenceClient;
import org.wso2.das.integration.common.utils.DASIntegrationTest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class tests if DAS-395 is fixed. Please note this will not run with the integration tests as
 * this requires a HA clustered setup to reproduce the issue.
 */
public class DAS395SearchPaginationInHAClusterTestCase extends DASIntegrationTest {

    private static final String TABLE1 = "platform.test.analytics.webservice.table1";
    private static final String STREAM_VERSION_1 = "1.0.0";
    private AnalyticsWebServiceClient webServiceClient;
    private EventStreamPersistenceClient persistenceClient;
    private DataPublisherClient dataPublisherClient;

    @BeforeClass(groups = {"wso2.das"}, alwaysRun = true)
    protected void init() throws Exception {
        super.init();
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

    @Test(groups = "wso2.das", description = "Check get table schema", dependsOnMethods = "addStreamDefinition")
    public void addTableSchema() throws Exception {
        AnalyticsTable table1Version1 = getAnalyticsTable1Version1();
        persistenceClient.addAnalyticsTable(table1Version1);
        Thread.sleep(15000);
        AnalyticsSchemaBean tableSchema = webServiceClient.getTableSchema(TABLE1.replace('.', '_'));
        Assert.assertNotNull(table1Version1, "Getting null table schema object");
        Assert.assertEquals(tableSchema.getColumns().length, 3, "Getting invalid column count");
        Assert.assertEquals(tableSchema.getPrimaryKeys().length, 1, "Getting invalid primary key count");
    }

    @Test(groups = "wso2.das", description = "Range operations", dependsOnMethods = "addTableSchema")
    public void searchWithPagination() throws Exception {
        deployEventReceivers();
        Thread.sleep(15000);
        List<Event> events = new ArrayList<>(100);
        for (int i = 0; i < 100; i++) {
            Event event = new Event(null, System.currentTimeMillis(),
                                    new Object[0], new Object[0], new Object[]{(long) i, String.valueOf(i)});
            events.add(event);
        }
        publishEvents(events);
        Thread.sleep(5000);
        for (int i = 0; i < 4; i++) {
            RecordBean[] search = webServiceClient.search(TABLE1.replace('.', '_'), "*:*", i*25, 25);
            Assert.assertEquals(search.length, 25, "Pagination result count is wrong");
            Assert.assertNotNull(search, "Returning null array");
        }

        Assert.assertEquals(webServiceClient.searchCount(TABLE1.replace('.', '_'), "*:*"), 100, "Search count is " +
                                                                                                 "wrong");
        webServiceClient.clearIndices(TABLE1.replace('.', '_'));
        Thread.sleep(5000);
        Assert.assertEquals(webServiceClient.searchCount(TABLE1.replace('.', '_'), "*:*"), 0, "Clear indexing not " +
                                                                                                 "happening");
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

    private void publishEvents(List<Event> events) throws Exception {
        dataPublisherClient = new DataPublisherClient();
        dataPublisherClient.publish(TABLE1, STREAM_VERSION_1, events);
        Thread.sleep(10000);
        dataPublisherClient.shutdown();
    }

    private void deployEventReceivers() throws IOException {
        String streamResourceDir = FrameworkPathUtil.getSystemResourceLocation() + "webservice" + File.separator;
        String streamsLocation = FrameworkPathUtil.getCarbonHome() + File.separator + "repository"
                                 + File.separator + "deployment" + File.separator + "server" + File.separator + "eventreceivers" + File.separator;
        FileManager.copyResourceToFileSystem(streamResourceDir + "webservice_test_table1.xml", streamsLocation, "webservice_test_table1.xml");
    }
}
