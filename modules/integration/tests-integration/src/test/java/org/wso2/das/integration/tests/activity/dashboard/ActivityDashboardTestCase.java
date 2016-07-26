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
package org.wso2.das.integration.tests.activity.dashboard;

import org.apache.axiom.attachments.ByteArrayDataSource;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.activitydashboard.commons.InvalidExpressionNodeException;
import org.wso2.carbon.analytics.activitydashboard.commons.Operation;
import org.wso2.carbon.analytics.activitydashboard.commons.Query;
import org.wso2.carbon.analytics.activitydashboard.commons.SearchExpressionTree;
import org.wso2.carbon.analytics.activitydashboard.stub.ActivityDashboardAdminServiceActivityDashboardExceptionException;
import org.wso2.carbon.analytics.activitydashboard.stub.ActivityDashboardAdminServiceStub;
import org.wso2.carbon.analytics.activitydashboard.stub.bean.ActivitySearchRequest;
import org.wso2.carbon.analytics.activitydashboard.stub.bean.RecordBean;
import org.wso2.carbon.analytics.activitydashboard.stub.bean.RecordId;
import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.analytics.api.CarbonAnalyticsAPI;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.das.integration.common.clients.AnalyticsWebServiceClient;
import org.wso2.das.integration.common.clients.EventReceiverClient;
import org.wso2.das.integration.common.clients.EventStreamPersistenceClient;
import org.wso2.das.integration.common.utils.DASIntegrationTest;
import org.wso2.das.integration.common.utils.Utils;

import javax.activation.DataHandler;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Activity monitoring related tests.
 */
public class ActivityDashboardTestCase extends DASIntegrationTest {
    private static final String ACTIVITY_DASHBOARD_SERVICE = "ActivityDashboardAdminService";
    private ActivityDashboardAdminServiceStub activityDashboardStub;
    private AnalyticsDataAPI analyticsDataAPI;
    private AnalyticsWebServiceClient webServiceClient;
    private EventStreamPersistenceClient persistenceClient;
    private EventReceiverClient eventReceiverClient;

    @BeforeClass (alwaysRun = true)
    protected void init() throws Exception {
        super.init();
        String apiConf = new File(this.getClass().getClassLoader().getResource(
                "dasconfig" + File.separator + "api" + File.separator + "analytics-data-config.xml").toURI()).getAbsolutePath();
        this.analyticsDataAPI = new CarbonAnalyticsAPI(apiConf);
        String session = getSessionCookie();
        this.webServiceClient = new AnalyticsWebServiceClient(this.backendURL, session);
        this.persistenceClient = new EventStreamPersistenceClient(this.backendURL, session);
        this.eventReceiverClient = new EventReceiverClient(this.backendURL, session);
        this.initializeActivityDashboardStub();
        deployStreamDefinition();
        deployEventSink();
        deployEventReceivers();
        this.cleanupData();
        publishActivities();
    }
    
    private void purgeTable(int tenantId, String tableName) throws AnalyticsException {
        if (this.analyticsDataAPI.tableExists(tenantId, tableName)) {
            this.analyticsDataAPI.delete(tenantId, tableName, Long.MIN_VALUE, Long.MAX_VALUE);
        }
    }
    
    @AfterClass (alwaysRun = true)
    protected void cleanup() throws AnalyticsException, RemoteException {
        this.eventReceiverClient.undeployEventReceiver("BAMActivityWSO2Event");
        this.eventReceiverClient.undeployEventReceiver("DASActivityWSO2Event");
        this.eventReceiverClient.undeployEventReceiver("CEPActivityWSO2Event");
        this.cleanupData();
    }
    
    private void cleanupData() throws AnalyticsException {
        this.purgeTable(MultitenantConstants.SUPER_TENANT_ID, "ORG_WSO2_BAM_ACTIVITY_MONITORING");
        this.purgeTable(MultitenantConstants.SUPER_TENANT_ID, "ORG_WSO2_CEP_ACTIVITY_MONITORING");
        this.purgeTable(MultitenantConstants.SUPER_TENANT_ID, "ORG_WSO2_DAS_ACTIVITY_MONITORING");
    }

    @Test(groups = "wso2.bam", description = "Getting the current list of tables")
    public void getActivityTables() throws RemoteException, ActivityDashboardAdminServiceActivityDashboardExceptionException {
        String[] tables = this.activityDashboardStub.getAllTables();
        Assert.assertTrue(tables != null, "Atleast there should be activity dashboard test tables existing, but empty results returned");
        Assert.assertTrue(tableExists(tables, "ORG_WSO2_BAM_ACTIVITY_MONITORING"), "ORG_WSO2_BAM_ACTIVITY_MONITORING is not existing!");
        Assert.assertTrue(tableExists(tables, "ORG_WSO2_CEP_ACTIVITY_MONITORING"), "ORG_WSO2_CEP_ACTIVITY_MONITORING is not existing!");
        Assert.assertTrue(tableExists(tables, "ORG_WSO2_DAS_ACTIVITY_MONITORING"), "ORG_WSO2_DAS_ACTIVITY_MONITORING is not existing!");
    }

    @Test(groups = "wso2.bam", description = "searchSingleTable", dependsOnMethods = "getActivityTables")
    public void searchActivitiesSingleTable() throws IOException,
            ActivityDashboardAdminServiceActivityDashboardExceptionException, InvalidExpressionNodeException {
        ActivitySearchRequest searchRequest = new ActivitySearchRequest();
        searchRequest.setFromTime(Long.MIN_VALUE);
        searchRequest.setToTime(Long.MAX_VALUE);

        SearchExpressionTree searchExpressionTree = new SearchExpressionTree();
        Query query = new Query("0", "ORG_WSO2_BAM_ACTIVITY_MONITORING", "meta_remote_host:\"localhost\" AND meta_http_method :\"POST\"");
        searchExpressionTree.setRoot(query);

        searchRequest.setSearchTreeExpression(new DataHandler(
                new ByteArrayDataSource(serializeObject(searchExpressionTree))));
        String[] activities = activityDashboardStub.getActivities(searchRequest);
        Assert.assertTrue(activities != null, "Expected activities size is 5, but found null");
        Assert.assertEquals(activities.length, 5);
    }

    @Test(groups = "wso2.bam", description = "searchSingleTableWithEmptyQuery", dependsOnMethods = "searchActivitiesSingleTable")
    public void searchActivitiesSingleTableWithEmptyQuery() throws IOException,
            ActivityDashboardAdminServiceActivityDashboardExceptionException, InvalidExpressionNodeException {
        ActivitySearchRequest searchRequest = new ActivitySearchRequest();
        searchRequest.setFromTime(Long.MIN_VALUE);
        searchRequest.setToTime(Long.MAX_VALUE);

        SearchExpressionTree searchExpressionTree = new SearchExpressionTree();
        Query query = new Query("0", "ORG_WSO2_BAM_ACTIVITY_MONITORING", "meta_remote_host:\"localhost\" AND meta_http_method :\"POST\"");
        searchExpressionTree.setRoot(query);

        searchRequest.setSearchTreeExpression(new DataHandler(
                new ByteArrayDataSource(serializeObject(searchExpressionTree))));
        String[] activities = activityDashboardStub.getActivities(searchRequest);
        Assert.assertTrue(activities != null, "Expected activities size is 5, but found null");
        Assert.assertEquals(activities.length, 5);
    }

    @Test(groups = "wso2.bam", description = "get the records for the given activity id", dependsOnMethods = "searchActivitiesSingleTableWithEmptyQuery")
    public void getRecordIds() throws RemoteException, ActivityDashboardAdminServiceActivityDashboardExceptionException {
        RecordId[] recordIds = activityDashboardStub.getRecordIds("1cecbb16-6b89-46f3-bd2f-fd9f7ac447b6",
                new String[]{"ORG_WSO2_BAM_ACTIVITY_MONITORING"});
        Assert.assertTrue(recordIds != null, "Record id's returned for an activity id cannot be null!");
        RecordBean recordBean = activityDashboardStub.getRecord(recordIds[0]);
        Assert.assertTrue(recordBean != null, "Returned record for the record id is null!");
    }

    @Test(groups = "wso2.bam", description = "search activities with AND operation", dependsOnMethods = "getRecordIds")
    public void andOperationActivitySearch() throws IOException, ActivityDashboardAdminServiceActivityDashboardExceptionException,
            InvalidExpressionNodeException {
        ActivitySearchRequest searchRequest = new ActivitySearchRequest();
        searchRequest.setFromTime(Long.MIN_VALUE);
        searchRequest.setToTime(Long.MAX_VALUE);

        SearchExpressionTree searchExpressionTree = new SearchExpressionTree();
        Operation operation = new Operation("0", Operation.Operator.AND);

        Operation operation1 = new Operation("1", Operation.Operator.AND);
        operation1.setLeftExpression(new Query("1.1", "ORG_WSO2_BAM_ACTIVITY_MONITORING", "meta_remote_host:\"localhost\" AND meta_http_method :\"POST\""));
        operation1.setRightExpression(new Query("1.2", "ORG_WSO2_CEP_ACTIVITY_MONITORING", "meta_remote_host:\"localhost\" AND meta_http_method :\"POST\""));
        operation.setLeftExpression(operation1);

        Query query = new Query("2", "ORG_WSO2_DAS_ACTIVITY_MONITORING", "meta_remote_host:\"localhost\" AND meta_http_method :\"POST\"");
        operation.setRightExpression(query);

        searchExpressionTree.setRoot(operation);
        searchRequest.setSearchTreeExpression(new DataHandler(
                new ByteArrayDataSource(serializeObject(searchExpressionTree))));

        String[] activities = activityDashboardStub.getActivities(searchRequest);
        Assert.assertTrue(activities != null, "Expected activities size is 1, but found null");
        Assert.assertEquals(activities.length, 1);
    }

    @Test(groups = "wso2.bam", description = "search activities with OR operation", dependsOnMethods = "andOperationActivitySearch")
    public void orOperationActivitySearch() throws IOException, ActivityDashboardAdminServiceActivityDashboardExceptionException,
            InvalidExpressionNodeException {
        ActivitySearchRequest searchRequest = new ActivitySearchRequest();
        searchRequest.setFromTime(Long.MIN_VALUE);
        searchRequest.setToTime(Long.MAX_VALUE);

        SearchExpressionTree searchExpressionTree = new SearchExpressionTree();
        Operation operation = new Operation("0", Operation.Operator.OR);

        Operation operation1 = new Operation("1", Operation.Operator.OR);
        operation1.setLeftExpression(new Query("1.1", "ORG_WSO2_BAM_ACTIVITY_MONITORING", "meta_remote_host:\"localhost\" AND meta_http_method :\"POST\""));
        operation1.setRightExpression(new Query("1.2", "ORG_WSO2_CEP_ACTIVITY_MONITORING", "meta_remote_host:\"localhost\" AND meta_http_method :\"POST\""));
        operation.setLeftExpression(operation1);

        Query query = new Query("2", "ORG_WSO2_DAS_ACTIVITY_MONITORING", "meta_remote_host:\"localhost\" AND meta_http_method :\"POST\"");
        operation.setRightExpression(query);
        searchExpressionTree.setRoot(operation);

        searchRequest.setSearchTreeExpression(new DataHandler(
                new ByteArrayDataSource(serializeObject(searchExpressionTree))));
        String[] activities = activityDashboardStub.getActivities(searchRequest);
        Assert.assertTrue(activities != null, "Expected activities size is 5, but found null");
        Assert.assertEquals(activities.length, 5);
    }

    @Test(groups = "wso2.bam", description = "search by providing the time range", dependsOnMethods = "orOperationActivitySearch")
    public void searchWithTimeRange() throws InvalidExpressionNodeException, IOException,
            ActivityDashboardAdminServiceActivityDashboardExceptionException {
        ActivitySearchRequest searchRequest = new ActivitySearchRequest();
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        calendar.set(Calendar.HOUR_OF_DAY, currentHour + 1);
        searchRequest.setToTime(calendar.getTimeInMillis());
        if (currentHour == 0) {
            //reducing two hours from to time
            searchRequest.setFromTime(calendar.getTimeInMillis() - 2*60*60*1000);
        }
        else {
            calendar.set(Calendar.HOUR_OF_DAY, currentHour - 1);
            searchRequest.setFromTime(calendar.getTimeInMillis());
        }
        SearchExpressionTree searchExpressionTree = new SearchExpressionTree();
        Query query = new Query("0", "ORG_WSO2_BAM_ACTIVITY_MONITORING", "meta_remote_host:\"localhost\" AND meta_http_method :\"POST\"");
        searchExpressionTree.setRoot(query);

        searchRequest.setSearchTreeExpression(new DataHandler(
                new ByteArrayDataSource(serializeObject(searchExpressionTree))));
        String[] activities = activityDashboardStub.getActivities(searchRequest);
        Assert.assertTrue(activities != null, "Expected activities size is 5, but found null");
        Assert.assertEquals(activities.length, 5);
    }


    private boolean tableExists(String[] tableNames, String tableName) {
        for (String table : tableNames) {
            if (table.equalsIgnoreCase(tableName)) {
                return true;
            }
        }
        return false;
    }

    String streamResourceDir = FrameworkPathUtil.getSystemResourceLocation() + "activity" + File.separator + "dashboard"
            + File.separator + "streams" + File.separator;

    private void deployStreamDefinition() throws IOException {
        String streamsLocation = FrameworkPathUtil.getCarbonHome() + File.separator + "repository" + File.separator
                + "deployment" + File.separator + "server" + File.separator + "eventstreams" + File.separator;
        FileManager.copyResourceToFileSystem(streamResourceDir + "org.wso2.bam.activity.monitoring_1.0.0.json",
                streamsLocation, "org.wso2.bam.activity.monitoring_1.0.0.json");
        FileManager.copyResourceToFileSystem(streamResourceDir + "org.wso2.cep.activity.monitoring_1.0.0.json",
                streamsLocation, "org.wso2.cep.activity.monitoring_1.0.0.json");
        FileManager.copyResourceToFileSystem(streamResourceDir + "org.wso2.das.activity.monitoring_1.0.0.json",
                streamsLocation, "org.wso2.das.activity.monitoring_1.0.0.json");
    }

    private void deployEventSink() throws Exception {
        String streamResourceDir = FrameworkPathUtil.getSystemResourceLocation() +
                "activity" + File.separator + "dashboard" + File.separator + "eventsink" + File.separator;
        String streamsLocation = FrameworkPathUtil.getCarbonHome() + File.separator + "repository"
                + File.separator + "deployment" + File.separator + "server" + File.separator + "eventsink" + File.separator;
        FileManager.copyResourceToFileSystem(streamResourceDir + "org_wso2_bam_activity_monitoring.xml"
                , streamsLocation, "org_wso2_bam_activity_monitoring.xml");
        FileManager.copyResourceToFileSystem(streamResourceDir + "org_wso2_cep_activity_monitoring.xml"
                , streamsLocation, "org_wso2_cep_activity_monitoring.xml");
        FileManager.copyResourceToFileSystem(streamResourceDir + "org_wso2_das_activity_monitoring.xml"
                , streamsLocation, "org_wso2_das_activity_monitoring.xml");
        Utils.checkAndWaitForStreamAndPersist(this.webServiceClient, this.persistenceClient, "org.wso2.das.activity.monitoring", "1.0.0");
        Utils.checkAndWaitForStreamAndPersist(this.webServiceClient, this.persistenceClient, "org.wso2.bam.activity.monitoring", "1.0.0");
        Utils.checkAndWaitForStreamAndPersist(this.webServiceClient, this.persistenceClient, "org.wso2.cep.activity.monitoring", "1.0.0");
    }

    private void deployEventReceivers() throws Exception {
        String streamResourceDir = "activity" + File.separator + "dashboard" + File.separator + 
                "eventreceivers" + File.separator;
        this.eventReceiverClient.addOrUpdateEventReceiver("BAMActivityWSO2Event", getResourceContent(
                ActivityDashboardTestCase.class, streamResourceDir +  "BAMActivityWSO2Event.xml"));
        this.eventReceiverClient.addOrUpdateEventReceiver("CEPActivityWSO2Event", getResourceContent(
                ActivityDashboardTestCase.class, streamResourceDir +  "CEPActivityWSO2Event.xml"));
        this.eventReceiverClient.addOrUpdateEventReceiver("DASActivityWSO2Event", getResourceContent(
                ActivityDashboardTestCase.class, streamResourceDir +  "DASActivityWSO2Event.xml"));
    }

    private void publishActivities() throws DataEndpointException, DataEndpointConfigurationException,
            URISyntaxException, DataEndpointAuthenticationException, DataEndpointAgentConfigurationException,
            TransportException, AnalyticsException {
        String url = "tcp://localhost:8311";
        List<String> activityIds = new ArrayList<>();
        activityIds.add("1cecbb16-6b89-46f3-bd2f-fd9f7ac447b6");
        activityIds.add("2cecbb16-6b89-46f3-bd2f-fd9f7ac447b6");
        activityIds.add("3cecbb16-6b89-46f3-bd2f-fd9f7ac447b6");
        activityIds.add("4cecbb16-6b89-46f3-bd2f-fd9f7ac447b6");
        activityIds.add("5cecbb16-6b89-46f3-bd2f-fd9f7ac447b6");

        ActivityDataPublisher activityDataPublisher = new ActivityDataPublisher(url);
        activityDataPublisher.publish("org.wso2.bam.activity.monitoring", "1.0.0", activityIds);

        activityIds.clear();
        activityIds.add("1cecbb16-6b89-46f3-bd2f-fd9f7ac447b6");
        activityIds.add("2cecbb16-6b89-46f3-bd2f-fd9f7ac447b6");
        activityIds.add("3cecbb16-6b89-46f3-bd2f-fd9f7ac447b6");
        activityDataPublisher.publish("org.wso2.cep.activity.monitoring", "1.0.0", activityIds);


        activityIds.clear();
        activityIds.add("3cecbb16-6b89-46f3-bd2f-fd9f7ac447b6");
        activityIds.add("4cecbb16-6b89-46f3-bd2f-fd9f7ac447b6");
        activityIds.add("5cecbb16-6b89-46f3-bd2f-fd9f7ac447b6");
        activityDataPublisher.publish("org.wso2.das.activity.monitoring", "1.0.0", activityIds);

        Utils.checkAndWaitForSearchQuerySize(this.webServiceClient, 
                GenericUtils.streamToTableName("org.wso2.bam.activity.monitoring"), "*:*", ActivityDataPublisher.EVENT_COUNT);
        Utils.checkAndWaitForSearchQuerySize(this.webServiceClient, 
                GenericUtils.streamToTableName("org.wso2.cep.activity.monitoring"), "*:*", ActivityDataPublisher.EVENT_COUNT);
        Utils.checkAndWaitForSearchQuerySize(this.webServiceClient, 
                GenericUtils.streamToTableName("org.wso2.das.activity.monitoring"), "*:*", ActivityDataPublisher.EVENT_COUNT);
        
        activityDataPublisher.shutdown();
    }

    private void initializeActivityDashboardStub() throws Exception {
        ConfigurationContext configContext = ConfigurationContextFactory.
                createConfigurationContextFromFileSystem(null);
        String loggedInSessionCookie = getSessionCookie();
        this.activityDashboardStub = new ActivityDashboardAdminServiceStub(configContext,
                backendURL + ACTIVITY_DASHBOARD_SERVICE);
        ServiceClient client = this.activityDashboardStub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                loggedInSessionCookie);
    }

    private byte[] serializeObject(Object obj) throws ActivityDashboardAdminServiceActivityDashboardExceptionException, IOException {
        return GenericUtils.serializeObject(obj);
    }
}
