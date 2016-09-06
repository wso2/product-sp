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
package org.wso2.das.integration.tests.analytics.execution;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.analytics.api.CarbonAnalyticsAPI;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataServiceUtils;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.spark.admin.stub.AnalyticsProcessorAdminServiceAnalyticsProcessorAdminExceptionException;
import org.wso2.carbon.analytics.spark.admin.stub.AnalyticsProcessorAdminServiceStub;
import org.wso2.carbon.analytics.spark.admin.stub.AnalyticsProcessorAdminServiceStub.AnalyticsQueryResultDto;
import org.wso2.carbon.analytics.spark.admin.stub.AnalyticsProcessorAdminServiceStub.AnalyticsRowResultDto;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.das.integration.common.utils.DASIntegrationTest;
import org.wso2.das.integration.common.utils.TestConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.*;

public class AnalyticsScriptTestCase extends DASIntegrationTest {
    private static final Log log = LogFactory.getLog(AnalyticsScriptTestCase.class);

    private static final String TABLE_NAME = "ANALYTICS_SCRIPTS_TEST";
    private static final String GTA_STATS_TABLE = "Stats";
    private static final String GTA_STATS_SUMMARY_TABLE = "StatsSummary";
    private static final String SCRIPT_RESOURCE_DIR = "analytics" + File.separator + "scripts";
    private static final String ANALYTICS_SERVICE_NAME = "AnalyticsProcessorAdminService";
    private static final String ANALYTICS_SCRIPT_WITH_TASK = "AddNewScriptTestWithTask";
    private static final String ANALYTICS_SCRIPT_WITHOUT_TASK  = "AddNewScriptTestWithouTask";
    private static final String UDAF_TEST_TABLE  = "udafTest";


    private AnalyticsProcessorAdminServiceStub analyticsStub;
    
    private AnalyticsDataAPI analyticsDataAPI;

    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        super.init();
        String apiConf = new File(this.getClass().getClassLoader().getResource(
                "dasconfig" + File.separator + "api" + File.separator + "analytics-data-config.xml").toURI()).getAbsolutePath();
        this.analyticsDataAPI = new CarbonAnalyticsAPI(apiConf);
        initializeSampleData();
        initializeStub();
        deleteIfExists(ANALYTICS_SCRIPT_WITH_TASK);
        deleteIfExists(ANALYTICS_SCRIPT_WITHOUT_TASK);
    }
    
    private void initializeSampleData() throws Exception {
        log.info("Deleting table: " + TABLE_NAME + " for Analytics Scripts TestCase (if exists)");
        this.analyticsDataAPI.deleteTable(MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME);
        //Creating sample tables used to test scripts.
        log.info("Creating table: " + TABLE_NAME + " for Analytics Scripts TestCase");
        this.analyticsDataAPI.createTable(MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME);
        //Push some events to the table
        log.info("Inserting some events for the table : " + TABLE_NAME);
        List<Record> recordList = new ArrayList<>();
        Map<String, Object> recordValues = new HashMap<>();
        recordValues.put("server_name", "DAS-123");
        recordValues.put("ip", "192.168.2.1");
        recordValues.put("tenant", -1234);
        recordValues.put("sequence", 104050000L);
        recordValues.put("summary", "Joey asks, how you doing?");
        for (int i = 0; i < 10; i++) {
            Record record = new Record("id" + i, MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME, recordValues);
            recordList.add(record);
        }
        this.analyticsDataAPI.put(recordList);
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
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                loggedInSessionCookie);
    }

    private void deleteIfExists(String scriptName) throws RemoteException,
            AnalyticsProcessorAdminServiceAnalyticsProcessorAdminExceptionException {
        AnalyticsProcessorAdminServiceStub.AnalyticsScriptDto[] scriptDtos = analyticsStub.getAllScripts();
        if (scriptDtos != null){
            for (AnalyticsProcessorAdminServiceStub.AnalyticsScriptDto scriptDto: scriptDtos){
                if (scriptDto.getName().equalsIgnoreCase(scriptName)){
                    analyticsStub.deleteScript(scriptDto.getName());
                }
            }
        }
    }

    @Test(groups = "wso2.bam", description = "Adding script without any task configured")
    public void addNewScriptWithoutTask() throws Exception {
        String scriptContent = getResourceContent(AnalyticsScriptTestCase.class,
                getAnalyticsScriptResourcePath("TestScript.ql"));
        analyticsStub.saveScript(ANALYTICS_SCRIPT_WITHOUT_TASK, scriptContent, null);
    }

    @Test(groups = "wso2.bam", description = "Adding script with task")
    public void addNewScript() throws Exception {
        String scriptContent = getResourceContent(AnalyticsScriptTestCase.class,
                getAnalyticsScriptResourcePath("InsertTableScript.ql"));
        String cronExp = "0 * * * * ?";
        analyticsStub.saveScript(ANALYTICS_SCRIPT_WITH_TASK, scriptContent, cronExp);

        /**
         * Sleep until the task is triggered and the table get created as mentioned in the script.
         */
        try {
            Thread.sleep(80000);
        } catch (InterruptedException ignored) {
        }

        boolean tableCreated = tableExists("ANALYTICS_SCRIPTS_INSERT_TEST");
        Assert.assertTrue(tableCreated, "Table ANALYTICS_SCRIPTS_INSERT_TEST wasn't " +
                "created according to the script, hence the task wasn't executed as expected");
    }

    private boolean tableExists(String tableName) throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", TestConstants.CONTENT_TYPE_JSON);
        headers.put("Accept", TestConstants.CONTENT_TYPE_JSON);
        headers.put("Authorization", TestConstants.BASE64_ADMIN_ADMIN);
        HttpResponse response = doGet(TestConstants.ANALYTICS_ENDPOINT_URL
                + TestConstants.TABLE_EXISTS + tableName, headers);
        log.info("Response: " + response.getData());
        return response.getResponseCode() == 200;
    }

    @Test(groups = "wso2.bam", description = "Updating scriptContent script without any task configured",
            dependsOnMethods = "addNewScriptWithoutTask")
    public void updateScriptContent() throws Exception {
        String scriptContent = getResourceContent(AnalyticsScriptTestCase.class,
                getAnalyticsScriptResourcePath("UpdateScript.ql"));
        analyticsStub.updateScriptContent(ANALYTICS_SCRIPT_WITHOUT_TASK, scriptContent);
        checkScript(ANALYTICS_SCRIPT_WITHOUT_TASK, scriptContent, null);
    }

    @Test(groups = "wso2.bam", description = "Updating task configured",
            dependsOnMethods = "updateScriptContent")
    public void updateScriptTask() throws Exception {
        String scriptContent = getResourceContent(AnalyticsScriptTestCase.class,
                getAnalyticsScriptResourcePath("UpdateScript.ql"));
        String updateTask = "0 0 12 * * ?";
        analyticsStub.updateScriptTask(ANALYTICS_SCRIPT_WITHOUT_TASK, updateTask);
        checkScript(ANALYTICS_SCRIPT_WITHOUT_TASK, scriptContent, updateTask);
    }

    @Test(groups = "wso2.bam", description = "Updating task configured",
            dependsOnMethods = "updateScriptContent")
    public void deleteScriptTask() throws Exception {
        String scriptContent = getResourceContent(AnalyticsScriptTestCase.class,
                getAnalyticsScriptResourcePath("UpdateScript.ql"));
        analyticsStub.updateScriptTask(ANALYTICS_SCRIPT_WITHOUT_TASK, null);
        checkScript(ANALYTICS_SCRIPT_WITHOUT_TASK, scriptContent, null);
    }

    @Test(groups = "wso2.bam", description = "Get the script and check whether it's configurations are stored as expected",
            dependsOnMethods = "addNewScript")
    public void getScript() throws Exception {
        String actualContent = getResourceContent(AnalyticsScriptTestCase.class,
                getAnalyticsScriptResourcePath("InsertTableScript.ql"));
        checkScript(ANALYTICS_SCRIPT_WITH_TASK, actualContent, "0 * * * * ?");
    }

    private void checkScript(String name, String expectedContent, String expectedCron) throws Exception {
        AnalyticsProcessorAdminServiceStub.AnalyticsScriptDto scriptDto = analyticsStub.getScript(name);
        if (expectedCron == null || expectedCron.trim().isEmpty()) {
            Assert.assertTrue(scriptDto.getCronExpression() == null || scriptDto.getCronExpression().trim().isEmpty(),
                    "Task was scheduled where it's expected to not to have a task");
        } else {
            Assert.assertTrue(scriptDto.getCronExpression() != null && !scriptDto.getCronExpression().trim().isEmpty(),
                    "Task wasn't scheduled where it's expected to have a task");
        }

        Assert.assertTrue(scriptDto.getScriptContent() != null && !scriptDto.getScriptContent().trim().isEmpty(), "Stored script is empty");
        Assert.assertTrue(scriptDto.getScriptContent().trim().equals(expectedContent.trim()),
                "The script which was stored and retrieved have different content");
    }

    @Test(groups = "wso2.bam", description = "Deleting the analytics script",
            dependsOnMethods = "getScript")
    public void deleteScript() throws Exception {
        analyticsStub.deleteScript(ANALYTICS_SCRIPT_WITH_TASK);
        AnalyticsProcessorAdminServiceStub.AnalyticsScriptDto[] scripts = analyticsStub.getAllScripts();
        if (scripts != null) {
            for (AnalyticsProcessorAdminServiceStub.AnalyticsScriptDto script : scripts) {
                if (script != null) {
                    Assert.assertFalse(script.getName().equals("AddNewScriptTestWithTask"),
                            "Delete script failed! The script is still exists!");
                }
            }
        }
    }

    @Test(groups = "wso2.bam", description = "Executing the script", dependsOnMethods = "deleteScriptTask")
    public void executeScript() throws Exception{
        analyticsStub.executeScript(ANALYTICS_SCRIPT_WITHOUT_TASK);
    }

    @Test(groups = "wso2.bam", description = "Executing the script content", dependsOnMethods = "executeScript")
    public void executeScriptContent() throws Exception {
        String scriptContent = getResourceContent(AnalyticsScriptTestCase.class,
                getAnalyticsScriptResourcePath("TestScript.ql"));
        analyticsStub.execute(scriptContent);
    }
    
    @Test(groups = "wso2.bam", description = "Executing script content with Global Tenant Access", dependsOnMethods = "executeScriptContent")
    public void executeScriptGlobalTenantAccess() throws Exception {
        log.info("Deleting table: " + GTA_STATS_TABLE + ", " + GTA_STATS_SUMMARY_TABLE + " for GTA TestCase (if exists)");
        this.analyticsDataAPI.deleteTable(MultitenantConstants.SUPER_TENANT_ID, GTA_STATS_TABLE);
        this.analyticsDataAPI.deleteTable(1, GTA_STATS_TABLE);
        this.analyticsDataAPI.deleteTable(2, GTA_STATS_TABLE);
        this.analyticsDataAPI.deleteTable(MultitenantConstants.SUPER_TENANT_ID, GTA_STATS_SUMMARY_TABLE);
        this.analyticsDataAPI.deleteTable(1, GTA_STATS_SUMMARY_TABLE);
        this.analyticsDataAPI.deleteTable(2, GTA_STATS_SUMMARY_TABLE);
        
        String scriptContent = getResourceContent(AnalyticsScriptTestCase.class, getAnalyticsScriptResourcePath("GTATestScript.ql"));
        AnalyticsQueryResultDto[] resultArr = this.analyticsStub.execute(scriptContent);
        AnalyticsQueryResultDto result = resultArr[resultArr.length - 1];
        
        List<Record> resp1 = AnalyticsDataServiceUtils.listRecords(this.analyticsDataAPI, this.analyticsDataAPI.get(1, GTA_STATS_TABLE, 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        List<Record> resp2 = AnalyticsDataServiceUtils.listRecords(this.analyticsDataAPI, this.analyticsDataAPI.get(2, GTA_STATS_TABLE, 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        List<Record> resp3 = AnalyticsDataServiceUtils.listRecords(this.analyticsDataAPI, this.analyticsDataAPI.get(1, GTA_STATS_SUMMARY_TABLE, 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        List<Record> resp4 = AnalyticsDataServiceUtils.listRecords(this.analyticsDataAPI, this.analyticsDataAPI.get(2, GTA_STATS_SUMMARY_TABLE, 1, null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1));
        Assert.assertEquals(resp1.size(), 4);
        Assert.assertEquals(resp2.size(), 4);
        Assert.assertEquals(resp3.size(), 2);
        Assert.assertEquals(resp4.size(), 2);
        
        Assert.assertEquals(new HashSet<>(Arrays.asList(result.getColumnNames())), new HashSet<>(Arrays.asList("name", "cnt", "_tenantId")));
        AnalyticsRowResultDto[] rows = result.getRowsResults();
        Assert.assertEquals(rows.length, 4);
        
        log.info("Deleting table: " + GTA_STATS_TABLE + ", " + GTA_STATS_SUMMARY_TABLE + " for GTA TestCase (cleanup)");
        this.analyticsDataAPI.deleteTable(MultitenantConstants.SUPER_TENANT_ID, GTA_STATS_TABLE);
        this.analyticsDataAPI.deleteTable(1, GTA_STATS_TABLE);
        this.analyticsDataAPI.deleteTable(2, GTA_STATS_TABLE);
        this.analyticsDataAPI.deleteTable(MultitenantConstants.SUPER_TENANT_ID, GTA_STATS_SUMMARY_TABLE);
        this.analyticsDataAPI.deleteTable(1, GTA_STATS_SUMMARY_TABLE);
        this.analyticsDataAPI.deleteTable(2, GTA_STATS_SUMMARY_TABLE);
    }

    @Test(groups = "wso2.bam", description = "Executing script content with Spark UDAFs", dependsOnMethods = "executeScriptContent")
    public void executeScriptSparkUDAF() throws Exception {
        log.info("Deleting table: " + UDAF_TEST_TABLE + " for UDAF TestCase (if exists)");
        this.analyticsDataAPI.deleteTable(MultitenantConstants.SUPER_TENANT_ID, UDAF_TEST_TABLE);

        String scriptContent = getResourceContent(AnalyticsScriptTestCase.class, getAnalyticsScriptResourcePath("SparkUDAFScript.ql"));
        AnalyticsQueryResultDto[] resultArr = this.analyticsStub.execute(scriptContent);
        AnalyticsQueryResultDto analyticsResult = resultArr[resultArr.length - 1];
        Assert.assertEquals(new HashSet<>(Arrays.asList(analyticsResult.getColumnNames())), new HashSet<>(Collections.singletonList("geomMean")));
        AnalyticsRowResultDto[] rows = analyticsResult.getRowsResults();
        Assert.assertEquals(rows.length, 1);
        String[] results = rows[rows.length - 1].getColumnValues();
        Assert.assertEquals(Math.round(Double.parseDouble(results[results.length - 1])), 8L);

        log.info("Deleting table: " + UDAF_TEST_TABLE + " for UDAF TestCase (if exists)");
        this.analyticsDataAPI.deleteTable(MultitenantConstants.SUPER_TENANT_ID, UDAF_TEST_TABLE);
    }

    private String getAnalyticsScriptResourcePath(String scriptName){
        return SCRIPT_RESOURCE_DIR + File.separator + scriptName;
    }

    //use this method since HttpRequestUtils.doGet does not support HTTPS.
    private static HttpResponse doGet(String endpoint, Map<String, String> headers) throws
                                                                                   IOException {
        HttpResponse httpResponse;
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setDoOutput(true);
        conn.setReadTimeout(30000);
        //setting headers
        if (headers != null && headers.size() > 0) {
            for (String key : headers.keySet()) {
                if (key != null) {
                    conn.setRequestProperty(key, headers.get(key));
                }
            }
            for (String key : headers.keySet()) {
                conn.setRequestProperty(key, headers.get(key));
            }
        }
        conn.connect();
        // Get the response
        StringBuilder sb = new StringBuilder();
        BufferedReader rd = null;
        try {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            httpResponse = new HttpResponse(sb.toString(), conn.getResponseCode());
            httpResponse.setResponseMessage(conn.getResponseMessage());
        } catch (IOException ignored) {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            httpResponse = new HttpResponse(sb.toString(), conn.getResponseCode());
            httpResponse.setResponseMessage(conn.getResponseMessage());
        } finally {
            if (rd != null) {
                rd.close();
            }
        }
        return httpResponse;
    }
}
