/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.das.integration.tests.clustering;

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
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.spark.admin.stub.AnalyticsProcessorAdminServiceAnalyticsProcessorAdminExceptionException;
import org.wso2.carbon.analytics.spark.admin.stub.AnalyticsProcessorAdminServiceStub;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.das.integration.common.utils.DASClusteredTestServerManager;
import org.wso2.das.integration.common.utils.FileReplacementInformation;
import org.wso2.das.integration.common.utils.TestConstants;
import org.wso2.das.integration.tests.analytics.execution.AnalyticsScriptTestCase;

import javax.xml.xpath.XPathExpressionException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.das.integration.tests.clustering.DASClusteredTestServerManagerConstants.ANALYTICS_DATASOURCES_PATH;
import static org.wso2.das.integration.tests.clustering.DASClusteredTestServerManagerConstants.AXIS2_XML_PATH;
import static org.wso2.das.integration.tests.clustering.DASClusteredTestServerManagerConstants.CARBON_XML_PATH;
import static org.wso2.das.integration.tests.clustering.DASClusteredTestServerManagerConstants.SPARK_DEFAULTS_CONF_PATH;

/**
 * This class runs the analytics script test case in a clustered environment
 */
public class MinimumHAClusterTestCase {
    private static final Log log = LogFactory.getLog(MinimumHAClusterTestCase.class);

    private static final String TABLE_NAME = "ANALYTICS_SCRIPTS_TEST";
    private static final String SCRIPT_RESOURCE_DIR = "analytics" + File.separator + "scripts";
    private static final String CONFIG_RESOURCE_DIR = "clustering" + File.separator + "config";
    private static final String ANALYTICS_SERVICE_NAME = "AnalyticsProcessorAdminService";
    private static final String ANALYTICS_SCRIPT_WITH_TASK = "AddNewScriptTestWithTask";
    private static final String ANALYTICS_SCRIPT_WITHOUT_TASK = "AddNewScriptTestWithouTask";
    private static final String HA_CLUSTER_GROUP_NAME = "DAS-HA";
    private static final int HA_CLUSTER_PORT_OFFSET = 800;

    private Map<String, DASClusteredTestServerManager> dasServerManagers = new HashMap<>();

    private AnalyticsProcessorAdminServiceStub analyticsStub;

    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        String initialCarbonHome = "";

        // start the first server in order to get the initial carbon_home which will be used to create the H2 db
        AutomationContext context = new AutomationContext(HA_CLUSTER_GROUP_NAME, "master1", TestUserMode.SUPER_TENANT_ADMIN);
        DASClusteredTestServerManager serverManager =
                new DASClusteredTestServerManager(context, createFileReplacementInformationList(initialCarbonHome, HA_CLUSTER_PORT_OFFSET + 0));
        initialCarbonHome = serverManager.startServer();
        dasServerManagers.put("master1", serverManager);

        //add the second server to the server list
        AutomationContext context2 = new AutomationContext(HA_CLUSTER_GROUP_NAME, "master2", TestUserMode.SUPER_TENANT_ADMIN);
        DASClusteredTestServerManager serverManager2 =
                new DASClusteredTestServerManager(context, createFileReplacementInformationList(initialCarbonHome, HA_CLUSTER_PORT_OFFSET + 1));
        dasServerManagers.put("master1", serverManager2);

        initializeSampleData();
        initializeStub();
        deleteIfExists(ANALYTICS_SCRIPT_WITH_TASK);
        deleteIfExists(ANALYTICS_SCRIPT_WITHOUT_TASK);
    }

    public void startServer(String instanceName)
            throws XPathExpressionException, AutomationFrameworkException, IOException {
        log.info("Starting server in the instance : "  + instanceName);
        dasServerManagers.get(instanceName).startServer();
    }

    public void stopServer(String instanceName)
            throws XPathExpressionException, AutomationFrameworkException, IOException {
        log.info("Starting server in the instance : "  + instanceName);
        dasServerManagers.get(instanceName).stopServer();
    }



    @Test(groups = "wso2.das.clustering", description = "starting master2")
    public void cluster() throws Exception {
        startServer("master2");
        runScriptTest("master1");
    }

    private void runScriptTest(String activeMasterInstanceName) {
        log.info("Running the test in the instance : " + activeMasterInstanceName);
        try {
            Thread.sleep(3600000);
        } catch (InterruptedException e) {
            log.error(e);
        }
        // todo: fill this
    }

    private List<FileReplacementInformation> createFileReplacementInformationList(String initialCarbonHome,
                                                                                  final int portOffset) {
        List<FileReplacementInformation> fileReplacementInformationList = new ArrayList<>();

        // for analytics-datasources.xml
        fileReplacementInformationList.add(new FileReplacementInformation(
                getClusteringConfigResourceURL("analytics-datasources.xml"), ANALYTICS_DATASOURCES_PATH, initialCarbonHome
        ) {
            @Override
            public Map<String, String> getPlaceHolderMap(String initialCarbonHome, String localhostIP) {
                Map<String, String> placeHolder = new HashMap<>();
                placeHolder.put("[[[carbonHome]]]", initialCarbonHome);
                return placeHolder;
            }
        });

        // for spark-defaults.conf
        fileReplacementInformationList.add(new FileReplacementInformation(
                getClusteringConfigResourceURL("spark-defaults.conf"), SPARK_DEFAULTS_CONF_PATH, initialCarbonHome
        ) {
            @Override
            public Map<String, String> getPlaceHolderMap(String initialCarbonHome, String localhostIP) {
                Map<String, String> placeHolder = new HashMap<>();
                placeHolder.put("[[[masterCount]]]", "2");
                return placeHolder;
            }
        });

        // for carbon.xml
        fileReplacementInformationList.add(new FileReplacementInformation(
                getClusteringConfigResourceURL("carbon.xml"), CARBON_XML_PATH, initialCarbonHome
        ) {
            @Override
            public Map<String, String> getPlaceHolderMap(String initialCarbonHome, String localhostIP) {
                Map<String, String> placeHolder = new HashMap<>();
                placeHolder.put("[[[portOffset]]]", String.valueOf(portOffset));
                return placeHolder;
            }
        });

        //for axis2.xml
        fileReplacementInformationList.add(new FileReplacementInformation(
                getClusteringConfigResourceURL("axis2.xml"), AXIS2_XML_PATH, initialCarbonHome
        ) {
            @Override
            public Map<String, String> getPlaceHolderMap(String initialCarbonHome, String localhostIP) {
                Map<String, String> placeHolder = new HashMap<>();
                placeHolder.put("[[[clusteringEnabled]]]", "true");
                placeHolder.put("[[[membershipScheme]]]", "wka");
                placeHolder.put("[[[localMemberHost]]]", localhostIP);
                placeHolder.put("[[[localMemberPort]]]", String.valueOf(4000 + portOffset));
                placeHolder.put("[[[members]]]", getMembersXMLElment(localhostIP, 2));
                return placeHolder;
            }
        });

        return fileReplacementInformationList;
    }


    private void initializeSampleData() throws Exception {

/*        String apiConf =
                new File(this.getClass().getClassLoader().
                        getResource("dasconfig" + File.separator + "api" + File.separator + "analytics-data-config.xml").toURI())
                        .getAbsolutePath();
        AnalyticsDataAPI analyticsDataAPI = new CarbonAnalyticsAPI(apiConf);
        //Creating sample tables used to test scripts.
        log.info("Creating table :" + TABLE_NAME + " for Analytics Scripts TestCase");
        analyticsDataAPI.createTable(MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME);
        //Push some events to the table
        log.info("Inserting some events for the table : " + TABLE_NAME);
        List<Record> recordList = new ArrayList<>();
        Map<String, Object> recordValues = new HashMap<>();
        recordValues.put("server_name", "DAS-123");
        recordValues.put("ip", "192.168.2.1");
        recordValues.put("tenant", "-1234");
        recordValues.put("sequence", "104050000");
        recordValues.put("summary", "Joey asks, how you doing?");

        for (int i = 0; i < 10; i++) {
            Record record = new Record("id" + i, MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME, recordValues);
            recordList.add(record);
        }
        analyticsDataAPI.put(recordList);*/
    }

    private void initializeStub() throws Exception {
/*        ConfigurationContext configContext = ConfigurationContextFactory.
                createConfigurationContextFromFileSystem(null);
        String loggedInSessionCookie = getSessionCookie();
        analyticsStub = new AnalyticsProcessorAdminServiceStub(configContext,
                                                               backendURL + "/services/" + ANALYTICS_SERVICE_NAME);
        ServiceClient client = analyticsStub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                           loggedInSessionCookie);*/
    }

    protected String getSessionCookie(String instanceName) throws Exception {
        return new LoginLogoutClient(dasServerManagers.get(instanceName).getContext()).login();
    }

    private void deleteIfExists(String scriptName) throws RemoteException,
                                                          AnalyticsProcessorAdminServiceAnalyticsProcessorAdminExceptionException {
/*        AnalyticsProcessorAdminServiceStub.AnalyticsScriptDto[] scriptDtos = analyticsStub.getAllScripts();
        if (scriptDtos != null) {
            for (AnalyticsProcessorAdminServiceStub.AnalyticsScriptDto scriptDto : scriptDtos) {
                if (scriptDto.getName().equalsIgnoreCase(scriptName)) {
                    analyticsStub.deleteScript(scriptDto.getName());
                }
            }
        }*/
    }

/*    @Test(groups = "wso2.bam", description = "Adding script without any task configured")
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

        *//**
         * Sleep until the task is triggered and the table get created as mentioned in the script.
         *//*
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
    public void executeScript() throws Exception {
        analyticsStub.executeScript(ANALYTICS_SCRIPT_WITHOUT_TASK);
    }

    @Test(groups = "wso2.bam", description = "Executing the script content", dependsOnMethods = "executeScript")
    public void executeScriptContent() throws Exception {
        String scriptContent = getResourceContent(AnalyticsScriptTestCase.class,
                                                  getAnalyticsScriptResourcePath("TestScript.ql"));
        analyticsStub.execute(scriptContent);
    }*/

/*    private String getAnalyticsScriptResourcePath(String scriptName) {
        return SCRIPT_RESOURCE_DIR + File.separator + scriptName;
    }*/

    private URL getClusteringConfigResourceURL(String configFile) {
        return this.getClass().getClassLoader().getResource(CONFIG_RESOURCE_DIR + File.separator + configFile);
    }

    //use this method since HttpRequestUtils.doGet does not support HTTPS.
/*
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
*/


    private String getMembersXMLElment(String localhostIP, int memberCount) {
        String xmlElm = "";
        for (int i = 0; i < memberCount; i++) {
            xmlElm = xmlElm + "<member>\n" +
                     "<hostName>" + localhostIP + "</hostName>\n" +
                     "<port>" + String.valueOf(4000 + i) + "</port>\n";
            xmlElm = xmlElm + "</member>\n";
        }
        return xmlElm;
    }

/*    private String getResourceContent(Class testClass, String resourcePath) throws Exception {
        String content = "";
        URL url = testClass.getClassLoader().getResource(resourcePath);
        if (url != null) {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(
                    new File(url.toURI()).getAbsolutePath()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content += line;
            }
            return content;
        } else {
            throw new Exception("No resource found in the given path : " + resourcePath);
        }
    }*/

}

class DASClusteredTestServerManagerConstants {
    static final String ANALYTICS_DATASOURCES_PATH = "repository" + File.separator + "conf" + File.separator
                                                            + "datasources" + File.separator + "clustering/config/analytics-datasources.xml";
    static final String SPARK_DEFAULTS_CONF_PATH = "repository" + File.separator + "conf" + File.separator +
                                                          "analytics" + File.separator + "spark" + File.separator +
                                                          "clustering/config/spark-defaults.conf";
    static final String CARBON_XML_PATH = "repository" + File.separator + "conf" + File.separator + "clustering/config/carbon.xml";
    static final String AXIS2_XML_PATH = "repository" + File.separator + "conf" + File.separator + "axis2" +
                                                File.separator + "axis2.xml";
}
