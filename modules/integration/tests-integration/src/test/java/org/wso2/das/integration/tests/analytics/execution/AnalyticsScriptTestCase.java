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

import com.google.gson.Gson;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.das.analytics.rest.beans.RecordBean;
import org.wso2.das.analytics.rest.beans.TableBean;
import org.wso2.das.integration.common.utils.BAMIntegrationTest;
import org.wso2.das.integration.common.utils.TestConstants;
import org.wso2.carbon.analytics.spark.admin.stub.AnalyticsProcessorAdminServiceStub;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsScriptTestCase extends BAMIntegrationTest {
    private static final Log log = LogFactory.getLog(AnalyticsScriptTestCase.class);

    private static final String TABLE_NAME = "ANALYTICS_SCRIPTS_TEST";
    private static final String SCRIPT_RESOURCE_DIR = "analytics" + File.separator + "scripts";
    private static final String ANALYTICS_SERVICE_NAME = "AnalyticsProcessorAdminService";
    private static final String ANALYTICS_SCRIPT_WITH_TASK = "AddNewScriptTestWithTask";
    private static final String ANALYTICS_SCRIPT_WITHOUT_TASK  = "AddNewScriptTestWithouTask";

    private AnalyticsProcessorAdminServiceStub analyticsStub;

    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        super.init();
        initializeSampleData();
        initializeStub();
    }

    private void initializeSampleData() throws Exception {
        Gson gson = new Gson();
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", TestConstants.CONTENT_TYPE_JSON);
        headers.put("Accept", TestConstants.CONTENT_TYPE_JSON);
        headers.put("Authorization", TestConstants.BASE64_ADMIN_ADMIN);

        //Creating sample tables used to test scripts.
        log.info("Creating table :" + TABLE_NAME + " for Analytics Scripts TestCase");
        URL restUrl = new URL(TestConstants.ANALYTICS_TABLES_ENDPOINT_URL);
        TableBean tableBean = new TableBean();
        tableBean.setTableName(TABLE_NAME);
        HttpResponse response = HttpRequestUtil.doPost(restUrl, gson.toJson(tableBean), headers);
        log.info("Response: " + response.getData());
        if (response.getResponseCode() != 201) {
            throw new Exception("Unexpected response returned :" + response.getResponseCode() +
                    " and message: " + response.getResponseMessage() +
                    ". Therefore the initialization of test is not successful!");
        }
        if (!response.getData().
                contains("Successfully created table: " + TABLE_NAME)) {
            throw new Exception("Unexpected response returned :" + response.getData() +
                    ". Therefore the initialization of test is not successful!");
        }
        //Push some events to the table
        log.info("Inserting some events for the table : " + TABLE_NAME);
        List<RecordBean> recordList = new ArrayList<>();
        Map<String, Object> recordValues = new HashMap<>();
        recordValues.put("server_name", "DAS-123");
        recordValues.put("ip", "192.168.2.1");
        recordValues.put("tenant", "-1234");
        recordValues.put("sequence", "104050000");
        recordValues.put("summary", "Joey asks, how you doing?");

        for (int i = 0; i < 10; i++) {
            RecordBean record = new RecordBean();
            record.setTableName(TABLE_NAME);
            record.setValues(recordValues);
            record.setId("id" + i);
            recordList.add(record);
        }
        restUrl = new URL(TestConstants.ANALYTICS_RECORDS_ENDPOINT_URL);
        response = HttpRequestUtil.doPost(restUrl, gson.toJson(recordList), headers);
        log.info("Response: " + response.getData());
        if (response.getResponseCode() != 200) {
            throw new Exception("Unexpected response returned :" + response.getResponseCode() +
                    " and message: " + response.getResponseMessage()
                    + ". Therefore the initialization of test is not successful!");
        }
        Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
        if (response.getData().
                contains("[]")) {
            throw new Exception("Unexpected response returned :" + response.getData() +
                    ". Therefore the initialization of test is not successful!");
        }
        Assert.assertTrue(response.getData().contains("id0"));
        Assert.assertTrue(response.getData().contains("id1"));
        Assert.assertTrue(response.getData().contains("id2"));
        Assert.assertTrue(response.getData().contains("id3"));
        Assert.assertTrue(response.getData().contains("id4"));
        Assert.assertTrue(response.getData().contains("id5"));
        Assert.assertTrue(response.getData().contains("id6"));
        Assert.assertTrue(response.getData().contains("id7"));
        Assert.assertTrue(response.getData().contains("id8"));
        Assert.assertTrue(response.getData().contains("id9"));
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
        Assert.assertTrue(tableCreated, "Table ANALYTICS_SCRIPTS_INSERT_TEST wasn't got " +
                "created according to the script, hence the task wasn't executed as expected");
    }

    private boolean tableExists(String tableName) throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", TestConstants.CONTENT_TYPE_JSON);
        headers.put("Accept", TestConstants.CONTENT_TYPE_JSON);
        headers.put("Authorization", TestConstants.BASE64_ADMIN_ADMIN);
        HttpResponse response = HttpRequestUtil.doGet(TestConstants.ANALYTICS_ENDPOINT_URL
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

    private String getAnalyticsScriptResourcePath(String scriptName){
        return SCRIPT_RESOURCE_DIR+File.separator+scriptName;
    }
}
