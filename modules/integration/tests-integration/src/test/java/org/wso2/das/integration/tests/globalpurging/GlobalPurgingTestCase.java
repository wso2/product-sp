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

package org.wso2.das.integration.tests.globalpurging;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.stream.persistence.stub.dto.AnalyticsTable;
import org.wso2.carbon.analytics.stream.persistence.stub.dto.AnalyticsTableRecord;
import org.wso2.carbon.analytics.webservice.stub.beans.EventBean;
import org.wso2.carbon.analytics.webservice.stub.beans.RecordValueEntryBean;
import org.wso2.carbon.analytics.webservice.stub.beans.StreamDefAttributeBean;
import org.wso2.carbon.analytics.webservice.stub.beans.StreamDefinitionBean;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.das.integration.common.clients.AnalyticsWebServiceClient;
import org.wso2.das.integration.common.clients.EventStreamPersistenceClient;
import org.wso2.das.integration.common.utils.DASIntegrationTest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GlobalPurgingTestCase extends DASIntegrationTest {

    private static final String STREAM_VERSION_1 = "1.0.0";
    public static final String SOMETABLE_PATTERN1_TABLE1 = "sometable.pattern1.table1";
    public static final String SOMETABLE_PATTERN1_TABLE2 = "sometable.pattern1.table2";
    public static final String PREFIX_TABLE_1 = "prefix_table1";
    public static final String PREFIX_TABLE_2 = "prefix_table2";
    public static final String DAS_PREFIX_TABLE_1 = "DAS_prefix_table1";
    public static final String RANDOM_TABLE_1 = "random_table1";
    public static final String RANDOM_TABLE_2 = "random_table2";
    private AnalyticsWebServiceClient webServiceClient;
    private EventStreamPersistenceClient persistenceClient;
    private ServerConfigurationManager serverManager;

    @BeforeClass(alwaysRun = true, dependsOnGroups = "wso2.das")
    protected void init() throws Exception {
        super.init();
        String session = getSessionCookie();
        webServiceClient = new AnalyticsWebServiceClient(backendURL, session);
        persistenceClient = new EventStreamPersistenceClient(backendURL, session);
    }

    @Test(groups = "wso2.das.purging", description = "Checking global data purging")
    public void publishData() throws Exception {
        StreamDefinitionBean patternDef1 = getEventStreamBeanTable(SOMETABLE_PATTERN1_TABLE1);
        webServiceClient.addStreamDefinition(patternDef1);
        AnalyticsTable patternTable1 = getAnalyticsTable(SOMETABLE_PATTERN1_TABLE1);
        persistenceClient.addAnalyticsTable(patternTable1);
        StreamDefinitionBean patternDef2 = getEventStreamBeanTable(SOMETABLE_PATTERN1_TABLE2);
        webServiceClient.addStreamDefinition(patternDef2);
        AnalyticsTable patternTable2 = getAnalyticsTable(SOMETABLE_PATTERN1_TABLE2);
        persistenceClient.addAnalyticsTable(patternTable2);
        StreamDefinitionBean prefixDef1 = getEventStreamBeanTable(PREFIX_TABLE_1);
        webServiceClient.addStreamDefinition(prefixDef1);
        AnalyticsTable prefixTable1 = getAnalyticsTable(PREFIX_TABLE_1);
        persistenceClient.addAnalyticsTable(prefixTable1);
        StreamDefinitionBean prefixDef2 = getEventStreamBeanTable(PREFIX_TABLE_2);
        webServiceClient.addStreamDefinition(prefixDef2);
        AnalyticsTable prefixTable2 = getAnalyticsTable(PREFIX_TABLE_2);
        persistenceClient.addAnalyticsTable(prefixTable2);
        StreamDefinitionBean dasPrefixDef1 = getEventStreamBeanTable(DAS_PREFIX_TABLE_1);
        webServiceClient.addStreamDefinition(dasPrefixDef1);
        AnalyticsTable dasPrefixTable1 = getAnalyticsTable(DAS_PREFIX_TABLE_1);
        persistenceClient.addAnalyticsTable(dasPrefixTable1);
        StreamDefinitionBean randomDef1 = getEventStreamBeanTable(RANDOM_TABLE_1);
        webServiceClient.addStreamDefinition(randomDef1);
        AnalyticsTable randomTable1 = getAnalyticsTable(RANDOM_TABLE_1);
        persistenceClient.addAnalyticsTable(randomTable1);
        StreamDefinitionBean randomDef2 = getEventStreamBeanTable(RANDOM_TABLE_2);
        webServiceClient.addStreamDefinition(randomDef2);
        AnalyticsTable randomTable2 = getAnalyticsTable(RANDOM_TABLE_2);
        persistenceClient.addAnalyticsTable(randomTable2);
        Thread.sleep(15000);

        List<EventBean> eventBeans = getEventBeans(patternDef1);
        for (EventBean eventBean : eventBeans) {
            webServiceClient.publishEvent(eventBean);
        }
        eventBeans = getEventBeans(patternDef2);
        for (EventBean eventBean : eventBeans) {
            webServiceClient.publishEvent(eventBean);
        }
        eventBeans = getEventBeans(prefixDef1);
        for (EventBean eventBean : eventBeans) {
            webServiceClient.publishEvent(eventBean);
        }
        eventBeans = getEventBeans(prefixDef2);
        for (EventBean eventBean : eventBeans) {
            webServiceClient.publishEvent(eventBean);
        }
        eventBeans = getEventBeans(dasPrefixDef1);
        for (EventBean eventBean : eventBeans) {
            webServiceClient.publishEvent(eventBean);
        }
        eventBeans = getEventBeans(randomDef1);
        for (EventBean eventBean : eventBeans) {
            webServiceClient.publishEvent(eventBean);
        }
        eventBeans = getEventBeans(randomDef2);
        for (EventBean eventBean : eventBeans) {
            webServiceClient.publishEvent(eventBean);
        }
        Thread.sleep(2000);
        Assert.assertEquals(webServiceClient.getRecordCount(SOMETABLE_PATTERN1_TABLE1.replace('.', '_'), 0, System
                .currentTimeMillis()), 25, "Record count is incorrect");
        Assert.assertEquals(webServiceClient.getRecordCount(SOMETABLE_PATTERN1_TABLE2.replace('.', '_'), 0, System
                .currentTimeMillis()), 25, "Record count is incorrect");
        Assert.assertEquals(webServiceClient.getRecordCount(PREFIX_TABLE_1.replace('.', '_'), 0, System
                .currentTimeMillis()), 25, "Record count is incorrect");
        Assert.assertEquals(webServiceClient.getRecordCount(PREFIX_TABLE_2.replace('.', '_'), 0, System
                .currentTimeMillis()), 25, "Record count is incorrect");
        Assert.assertEquals(webServiceClient.getRecordCount(DAS_PREFIX_TABLE_1.replace('.', '_'), 0, System
                .currentTimeMillis()), 25, "Record count is incorrect");
        Assert.assertEquals(webServiceClient.getRecordCount(RANDOM_TABLE_1.replace('.', '_'), 0, System
                .currentTimeMillis()), 25, "Record count is incorrect");
        Assert.assertEquals(webServiceClient.getRecordCount(RANDOM_TABLE_2.replace('.', '_'), 0, System
                .currentTimeMillis()), 25, "Record count is incorrect");

        String artifactsLocation = FrameworkPathUtil.getSystemResourceLocation() + File.separator + "gloablepurging" +
                                   File.separator + "analytics-dataservice-config.xml";
        String dataserviceConfigLocation =
                FrameworkPathUtil.getCarbonHome() + File.separator + "repository" + File.separator + "conf" + File
                        .separator + "analytics" + File.separator + "analytics-dataservice-config.xml";
        serverManager = new ServerConfigurationManager(dasServer);
        File sourceFile = new File(artifactsLocation);
        File targetFile = new File(dataserviceConfigLocation);
        serverManager.applyConfigurationWithoutRestart(sourceFile, targetFile, true);
        serverManager.restartForcefully();
        Thread.sleep(150000);
        webServiceClient = new AnalyticsWebServiceClient(backendURL, getSessionCookie());
        Assert.assertEquals(webServiceClient.getRecordCount(SOMETABLE_PATTERN1_TABLE1.replace('.', '_'), 0, System
                .currentTimeMillis()), 0, "Record count is incorrect");
        Assert.assertEquals(webServiceClient.getRecordCount(SOMETABLE_PATTERN1_TABLE2.replace('.', '_'), 0, System
                .currentTimeMillis()), 0, "Record count is incorrect");
        Assert.assertEquals(webServiceClient.getRecordCount(PREFIX_TABLE_1.replace('.', '_'), 0, System
                .currentTimeMillis()), 0, "Record count is incorrect");
        Assert.assertEquals(webServiceClient.getRecordCount(PREFIX_TABLE_2.replace('.', '_'), 0, System
                .currentTimeMillis()), 0, "Record count is incorrect");
        Assert.assertEquals(webServiceClient.getRecordCount(DAS_PREFIX_TABLE_1.replace('.', '_'), 0, System
                .currentTimeMillis()), 25, "Record count is incorrect");
        Assert.assertEquals(webServiceClient.getRecordCount(RANDOM_TABLE_1.replace('.', '_'), 0, System
                .currentTimeMillis()), 25, "Record count is incorrect");
        Assert.assertEquals(webServiceClient.getRecordCount(RANDOM_TABLE_2.replace('.', '_'), 0, System
                .currentTimeMillis()), 25, "Record count is incorrect");
    }

    private List<EventBean> getEventBeans(StreamDefinitionBean streamDefinitionBean) {
        List<EventBean> eventBeans = new ArrayList<>(25);
        for (int i = 0; i < 25; i++) {
            EventBean eventBean = new EventBean();
            eventBean.setStreamName(streamDefinitionBean.getName());
            eventBean.setStreamVersion(STREAM_VERSION_1);
            RecordValueEntryBean[] payloadData = new RecordValueEntryBean[2];
            RecordValueEntryBean uuid = new RecordValueEntryBean();
            uuid.setFieldName("uuid");
            uuid.setType("LONG");
            uuid.setLongValue(i);
            payloadData[0] = uuid;
            RecordValueEntryBean name = new RecordValueEntryBean();
            name.setFieldName("name");
            name.setType("STRING");
            name.setStringValue(String.valueOf(i));
            payloadData[1] = name;
            eventBean.setPayloadData(payloadData);
            eventBeans.add(eventBean);
        }
        return eventBeans;
    }

    @AfterTest(alwaysRun = true)
    public void startRestoreAPIMConfigureXml() throws Exception {
        serverManager.restoreToLastConfiguration();
        serverManager.restartForcefully();
    }

    private StreamDefinitionBean getEventStreamBeanTable(String tableName) {
        StreamDefinitionBean definitionBean = new StreamDefinitionBean();
        definitionBean.setName(tableName);
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

    private AnalyticsTable getAnalyticsTable(String tableName) {
        AnalyticsTable table = new AnalyticsTable();
        table.setPersist(true);
        table.setTableName(tableName);
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
