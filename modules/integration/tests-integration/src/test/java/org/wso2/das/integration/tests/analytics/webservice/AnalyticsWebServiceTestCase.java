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

package org.wso2.das.integration.tests.analytics.webservice;

import com.google.gson.Gson;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.analytics.api.CarbonAnalyticsAPI;
import org.wso2.carbon.analytics.stream.persistence.stub.dto.AnalyticsTable;
import org.wso2.carbon.analytics.stream.persistence.stub.dto.AnalyticsTableRecord;
import org.wso2.carbon.analytics.webservice.stub.beans.AnalyticsAggregateField;
import org.wso2.carbon.analytics.webservice.stub.beans.AnalyticsAggregateRequest;
import org.wso2.carbon.analytics.webservice.stub.beans.AnalyticsDrillDownRequestBean;
import org.wso2.carbon.analytics.webservice.stub.beans.AnalyticsSchemaBean;
import org.wso2.carbon.analytics.webservice.stub.beans.CategoryDrillDownRequestBean;
import org.wso2.carbon.analytics.webservice.stub.beans.CategoryPathBean;
import org.wso2.carbon.analytics.webservice.stub.beans.EventBean;
import org.wso2.carbon.analytics.webservice.stub.beans.RecordBean;
import org.wso2.carbon.analytics.webservice.stub.beans.RecordValueEntryBean;
import org.wso2.carbon.analytics.webservice.stub.beans.SortByFieldBean;
import org.wso2.carbon.analytics.webservice.stub.beans.StreamDefAttributeBean;
import org.wso2.carbon.analytics.webservice.stub.beans.StreamDefinitionBean;
import org.wso2.carbon.analytics.webservice.stub.beans.SubCategoriesBean;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AnalyticsWebServiceTestCase extends DASIntegrationTest {

    private static final String TABLE1 = "integration.test.analytics.webservice.table1";
    private static final String TABLE2 = "integration.test.analytics.webservice.table2";
    private static final String STREAM_VERSION_1 = "1.0.0";
    private Log log = LogFactory.getLog(AnalyticsWebServiceTestCase.class);
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

    @Test(groups = "wso2.das", description = "Check stream exception situation", dependsOnMethods =
            "addStreamDefinition", expectedExceptions = AxisFault.class)
    public void addSameStreamDefWithDifferentAttribute() throws Exception {
        StreamDefinitionBean streamDefinitionBean = getEventStreamBeanTable1Version1DifferentAttribute();
        webServiceClient.addStreamDefinition(streamDefinitionBean);
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

    @Test(groups = "wso2.das", description = "Check table exist", dependsOnMethods = "getTableSchema")
    public void tableExists() throws Exception {
        Assert.assertTrue(webServiceClient.tableExists(TABLE1.replace('.', '_')), "Returns false for existing table");
        Assert.assertFalse(webServiceClient.tableExists("nonExistTable"), "Returns true for non existing table");
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

    @Test(groups = "wso2.das", description = "Get record count", dependsOnMethods = "listTables")
    public void getRecordCount() throws Exception {
        deployEventReceivers();
        Thread.sleep(15000);
        List<Event> events = new ArrayList<>(100);
        for (int i = 0; i < 100; i++) {
            Event event = new Event(null, System.currentTimeMillis(),
                    new Object[0], new Object[0], new Object[]{(long) i, String.valueOf(i)});
            events.add(event);
        }
        publishEvents(events);
        Thread.sleep(2000);
        long count = webServiceClient.getRecordCount(TABLE1.replace('.', '_'), 0, System.currentTimeMillis());
        if (count != -1) {
            Assert.assertEquals(count, 100, "Record count is invalid");
        }
    }

    @Test(groups = "wso2.das", description = "Range operations", dependsOnMethods = "paginationSearch")
    public void range() throws Exception {
        RecordBean[] byRange = webServiceClient.getByRange(TABLE1.replace('.', '_'), new String[]{"uuid"}, 0, System
                .currentTimeMillis(), 0, 200);
        Assert.assertNotNull(byRange, "Returns null array");
        Assert.assertEquals(byRange.length, 100, "Not contains 100 records");
        List<String> recordIds = new ArrayList<>(100);
        for (RecordBean recordBean : byRange) {
            recordIds.add(recordBean.getId());
        }
        RecordBean[] byId = webServiceClient.getById(TABLE1.replace('.', '_'), new String[]{"uuid"}, recordIds.toArray(new String[100]));
        Assert.assertNotNull(byId, "Returns null array");
        Assert.assertEquals(byId.length, 100, "Not contains 100 records");
        webServiceClient.deleteByIds(TABLE1.replace('.', '_'), recordIds.toArray(new String[100]));
        Thread.sleep(40000);
        byRange = webServiceClient.getByRange(TABLE1.replace('.', '_'), new String[]{"uuid"}, 0, System.currentTimeMillis(), 0, 200);
        Assert.assertEquals(byRange.length, 0);
    }

    @Test(groups = "wso2.das", description = "Pagination search", dependsOnMethods = "getRecordCount")
    public void paginationSearch() throws Exception {
        if (webServiceClient.isPaginationSupported(TABLE1.replace('.', '_'))) {
            Set<String> ids = new HashSet<>(100);
            long time = System.currentTimeMillis();
            for (int i = 0; i < 4; i++) {
                RecordBean[] byRange = webServiceClient.getByRange(TABLE1.replace('.', '_'), new String[]{"uuid"}, 0,
                        time, i * 25, 25);
                Assert.assertEquals(byRange.length, 25, "Pagination result count is wrong");
                for (RecordBean recordBean : byRange) {
                    ids.add(recordBean.getId());
                }
            }
            Assert.assertEquals(ids.size(), 100, "Doesn't contain all the records doing the pagination");
        }
    }

    @Test(groups = "wso2.das", description = "search operations", dependsOnMethods = "range")
    public void search() throws Exception {
        List<Event> events = new ArrayList<>(100);
        for (int i = 0; i < 100; i++) {
            Event event = new Event(null, System.currentTimeMillis(),
                    new Object[0], new Object[0], new Object[]{(long) i, String.valueOf(i)});
            events.add(event);
        }
        publishEvents(events);
        Thread.sleep(5000);
        RecordBean[] search = webServiceClient.search(TABLE1.replace('.', '_'), "uuid:1", 0, 10);
        log.info("Response : " + new Gson().toJson(search));
        Assert.assertNotNull(search, "Returning null array");
        Assert.assertEquals(search.length, 1, "Result doesn't contain one record");
        Assert.assertEquals(webServiceClient.searchCount(TABLE1.replace('.', '_'), "uuid:1"), 1, "Search count is " +
                "wrong");
    }

    @Test(groups = "wso2.das", description = "drillDown operations", dependsOnMethods = "search")
    public void drillDownSearch() throws Exception {

        AnalyticsDrillDownRequestBean bean = new AnalyticsDrillDownRequestBean();
        bean.setTableName(TABLE1.replace(".","_"));
        CategoryPathBean categoryPathBean = new CategoryPathBean();
        categoryPathBean.setFieldName("name");
        categoryPathBean.setPath(new String[]{});
        bean.setCategoryPaths(new CategoryPathBean[]{categoryPathBean});
        bean.setQuery("*:*");
        bean.setRecordStart(0);
        bean.setRecordCount(1000);
        RecordBean[] search = webServiceClient.drillDownSearch(bean);
        Assert.assertNotNull(search, "Returning null array");
        Assert.assertEquals(webServiceClient.drillDownSearchCount(bean), (double) search.length, "Search count is " +
                                                                                                 "wrong");
    }

    @Test(groups = "wso2.das", description = "drilldownCategories operations", dependsOnMethods = "drillDownSearch")
    public void drillDownCategories() throws Exception {

        CategoryDrillDownRequestBean bean = new CategoryDrillDownRequestBean();
        bean.setTableName(TABLE1.replace(".", "_"));
        bean.setQuery("*:*");
        bean.setFieldName("name");
        bean.setPath(new String[]{});
        bean.setStart(0);
        bean.setCount(1000);
        bean.setScoreFunction("uuid");
        SubCategoriesBean result = webServiceClient.drillDownCategories(bean);
        Assert.assertNotNull(result, "Returning null array");
        Assert.assertEquals(result.getCategoryCount(), 99, "category count is " +
                                                           "wrong");
        Assert.assertEquals(result.getCategories().length, 99, "category count is wrong");
        Assert.assertEquals(result.getCategories()[0].getCategoryName(), "99", "category name is wrong");
        Assert.assertEquals(result.getCategories()[98].getCategoryName(), "1", "category name is wrong");
    }

    @Test(groups = "wso2.das", description = "aggregates operations", dependsOnMethods = "drillDownCategories")
    public void performAggregations() throws Exception {

        AnalyticsAggregateRequest bean = new AnalyticsAggregateRequest();
        bean.setTableName(TABLE1.replace(".", "_"));
        bean.setQuery("*:*");
        bean.setAggregateLevel(0);
        bean.setNoOfRecords(10000);
        bean.setParentPath(new String[]{});
        bean.setGroupByField("name");
        AnalyticsAggregateField field = new AnalyticsAggregateField();
        field.setFields(new String[]{"uuid"});
        field.setAggregate("SUM");
        field.setAlias("sum");
        bean.setFields(new AnalyticsAggregateField[]{field});
        RecordBean[] result = webServiceClient.searchWithAggregates(bean);
        Assert.assertNotNull(result, "Returning null array");
        Assert.assertEquals(result.length, 100, "aggregate count is " +
                                                           "wrong");
    }

    @Test(groups = "wso2.das", description = "search with sorting operations", dependsOnMethods = "performAggregations")
    public void searchWithSorting() throws Exception {
        SortByFieldBean bean1 = new SortByFieldBean();
        SortByFieldBean bean2 = new SortByFieldBean();
        bean1.setFieldName("uuid");
        bean1.setSortType("DESC");
        bean2.setFieldName("name");
        bean2.setSortType("ASC");
        RecordBean[] search = webServiceClient.search(TABLE1.replace('.', '_'), "*:*", 0, 100, new String[]{"name", "uuid"},
                                                      new SortByFieldBean[]{bean1, bean2});
        Assert.assertNotNull(search, "Returning null array");
        Assert.assertEquals(search.length, 100, "Result doesn't contain one record");
        RecordBean recordBean1 = search[0];
        RecordBean recordBean2 = search[99];
        for (RecordValueEntryBean recordValueEntryBean : recordBean1.getValues()) {
            if (recordValueEntryBean.getFieldName().equals("uuid")) {
                Assert.assertTrue(recordValueEntryBean.getLongValue() == 99);
            }
        }
        for (RecordValueEntryBean recordValueEntryBean : recordBean2.getValues()) {
            if (recordValueEntryBean.getFieldName().equals("uuid")) {
                Assert.assertTrue(recordValueEntryBean.getLongValue() == 0);
            }
        }
    }

    @Test(groups = "wso2.das", description = "drillDown with sorting operations", dependsOnMethods = "searchWithSorting")
    public void drillDownSearchWithSorting() throws Exception {

        AnalyticsDrillDownRequestBean bean = new AnalyticsDrillDownRequestBean();
        SortByFieldBean byFieldBean = new SortByFieldBean();
        byFieldBean.setFieldName("name");
        byFieldBean.setSortType("ASC");
        bean.setTableName(TABLE1.replace(".","_"));
        CategoryPathBean categoryPathBean = new CategoryPathBean();
        categoryPathBean.setFieldName("name");
        categoryPathBean.setPath(new String[]{});
        bean.setCategoryPaths(new CategoryPathBean[]{categoryPathBean});
        bean.setQuery("*:*");
        bean.setRecordStart(0);
        bean.setRecordCount(1000);
        bean.setSortByFields(new SortByFieldBean[]{byFieldBean});
        RecordBean[] search = webServiceClient.drillDownSearch(bean);
        Assert.assertNotNull(search, "Returning null array");
        Assert.assertEquals(webServiceClient.drillDownSearchCount(bean), (double) search.length, "Search count is " +
                                                                                                 "wrong");
        webServiceClient.clearIndices(TABLE1.replace('.', '_'));
        Thread.sleep(5000);
        Assert.assertEquals(webServiceClient.searchCount(TABLE1.replace('.', '_'), "uuid:1"), 0, "Clear indexing not " +
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
        uuid.setScoreParam(true);
        records[0] = uuid;
        AnalyticsTableRecord name = new AnalyticsTableRecord();
        name.setPersist(true);
        name.setPrimaryKey(false);
        name.setIndexed(false);
        name.setColumnName("name");
        name.setColumnType("STRING");
        name.setScoreParam(false);
        name.setFacet(true);
        records[1] = name;
        AnalyticsTableRecord age = new AnalyticsTableRecord();
        age.setPersist(true);
        age.setPrimaryKey(false);
        age.setIndexed(false);
        age.setColumnName("_age");
        age.setColumnType("INTEGER");
        age.setScoreParam(true);
        age.setFacet(true);
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

    private StreamDefinitionBean getEventStreamBeanTable1Version1DifferentAttribute() {
        StreamDefinitionBean definitionBean = new StreamDefinitionBean();
        definitionBean.setName(TABLE1);
        definitionBean.setVersion(STREAM_VERSION_1);
        StreamDefAttributeBean[] attributeBeans = new StreamDefAttributeBean[2];
        StreamDefAttributeBean uuid = new StreamDefAttributeBean();
        uuid.setName("uuid");
        uuid.setType("STRING");
        attributeBeans[0] = uuid;
        StreamDefAttributeBean name = new StreamDefAttributeBean();
        name.setName("name");
        name.setType("LONG");
        attributeBeans[1] = name;
        definitionBean.setPayloadData(attributeBeans);
        return definitionBean;
    }

    private void publishEvent(long id, String name) throws Exception {
        Event event = new Event(null, System.currentTimeMillis(), new Object[0], new Object[0], new Object[]{id, name});
        dataPublisherClient = new DataPublisherClient();
        dataPublisherClient.publish(TABLE1, STREAM_VERSION_1, event);
        Thread.sleep(10000);
        dataPublisherClient.shutdown();
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
