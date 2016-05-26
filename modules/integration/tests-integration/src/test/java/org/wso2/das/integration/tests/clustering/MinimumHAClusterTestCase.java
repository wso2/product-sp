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
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.spark.admin.stub.AnalyticsProcessorAdminServiceStub;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.das.integration.common.utils.DASClusteredTestServerManager;
import org.wso2.das.integration.common.utils.FileReplacementInformation;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.das.integration.tests.clustering.DASClusteredTestServerManagerConstants.ANALYTICS_DATASOURCES_PATH;
import static org.wso2.das.integration.tests.clustering.DASClusteredTestServerManagerConstants.AXIS2_XML_PATH;
import static org.wso2.das.integration.tests.clustering.DASClusteredTestServerManagerConstants.CARBON_XML_PATH;
import static org.wso2.das.integration.tests.clustering.DASClusteredTestServerManagerConstants.MASTER_DATASOURCES_PATH;
import static org.wso2.das.integration.tests.clustering.DASClusteredTestServerManagerConstants.REGISTRY_XML_PATH;
import static org.wso2.das.integration.tests.clustering.DASClusteredTestServerManagerConstants.SPARK_DEFAULTS_CONF_PATH;

/**
 * This class runs the analytics script test case in a clustered environment
 */
public class MinimumHAClusterTestCase {
    private static final Log log = LogFactory.getLog(MinimumHAClusterTestCase.class);

    private static final String TABLE_NAME = "ANALYTICS_SCRIPTS_TEST";
    private static final String TABLE_NAME2 = "ANALYTICS_SCRIPTS_TEST2";
    private static final String SCRIPT_RESOURCE_DIR = "clustering" + File.separator + "scripts";
    private static final String CONFIG_RESOURCE_DIR = "clustering" + File.separator + "config";
    private static final String ANALYTICS_SERVICE_NAME = "AnalyticsProcessorAdminService";
    private static final String HA_CLUSTER_GROUP_NAME = "DAS";
    private static final int HA_CLUSTER_PORT_OFFSET = 900;

    private Map<String, DASClusteredTestServerManager> dasServerManagers = new HashMap<>();
    private Map<String, AnalyticsProcessorAdminServiceStub> analyticsStubs = new HashMap<>();

    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        LinkedHashMap<String, Integer> instances = new LinkedHashMap<>(2);
        instances.put("master1", HA_CLUSTER_PORT_OFFSET);
        instances.put("master2", HA_CLUSTER_PORT_OFFSET + 1);

        initializeServerManagersAndStubs(HA_CLUSTER_GROUP_NAME, instances, TestUserMode.SUPER_TENANT_ADMIN);
        initializeSampleData("master1");
    }

    /**
     * @param instanceNames instances
     * @return first instance's carbon home
     */
    private String initializeServerManagersAndStubs(String groupName, LinkedHashMap<String, Integer> instanceNames,
                                                    TestUserMode testUserMode)
            throws XPathExpressionException, IOException, AutomationFrameworkException, AutomationUtilException {
        String firstCarbonHome = "";

        int i = 0;
        for (String instanceName : instanceNames.keySet()) {
            int portOffset = instanceNames.get(instanceName);
            AutomationContext context = new AutomationContext(groupName, instanceName, testUserMode);
            DASClusteredTestServerManager serverManager = new DASClusteredTestServerManager(context, portOffset,
                                                                                            createFileReplacementInformationList(firstCarbonHome, portOffset));
            this.dasServerManagers.put(instanceName, serverManager);

            if (i == 0) {
                firstCarbonHome = this.startServer(instanceName);
            }
            i++;
        }
        return firstCarbonHome;
    }

    private String startServer(String instanceName)
            throws XPathExpressionException, AutomationFrameworkException, IOException, AutomationUtilException {
        log.info("#####################################  Starting server : " + instanceName);
        String carbonHome = this.dasServerManagers.get(instanceName).startServer();

        AutomationContext context = this.dasServerManagers.get(instanceName).getContext();
        ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null);
        String loggedInSessionCookie = new LoginLogoutClient(context).login();
        AnalyticsProcessorAdminServiceStub analyticsStub = new AnalyticsProcessorAdminServiceStub(configContext,
                                                                                                  context.getContextUrls().getBackEndUrl() + "/services/" + ANALYTICS_SERVICE_NAME);
        ServiceClient client = analyticsStub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                           loggedInSessionCookie);
        this.analyticsStubs.put(instanceName, analyticsStub);

        return carbonHome;
    }

    private void stopServer(String instanceName)
            throws XPathExpressionException, AutomationFrameworkException, IOException {
        log.info("##################################### Stopping server : " + instanceName);
        dasServerManagers.get(instanceName).stopServer();
    }


    @Test(groups = "wso2.das.clustering", description = "Starting master2")
    public void clusterInitialization() throws Exception {
        log.info("##################################### cluster Initialization");
        startServer("master2");
        runScriptTest("master1");
    }

    @Test(groups = "wso2.das.clustering", description = "Stopping master2", dependsOnMethods = "clusterInitialization")
    public void shutdownMaster2() throws Exception {
        log.info("##################################### shutdown Master2");
        stopServer("master2");
        runScriptTest("master1");
    }

    @Test(groups = "wso2.das.clustering", description = "Stopping master2", dependsOnMethods = "shutdownMaster2")
    public void restartMaster2() throws Exception {
        log.info("##################################### restart Master2");
        startServer("master2");
        runScriptTest("master1");
    }

    private void runScriptTest(String activeMasterInstanceName) throws Exception {
        log.info("##################################### Running script in :" + activeMasterInstanceName);
        executeScriptContent(activeMasterInstanceName, "TestScript.ql");
        checkResults(activeMasterInstanceName);
    }

    private void checkResults(String instanceName) throws URISyntaxException, AnalyticsException {
        String apiConf =
                new File(this.getClass().getClassLoader().
                        getResource("clustering" + File.separator + "dasconfig" + File.separator + "api" + File.separator
                                    + "analytics-data-config-" + instanceName + ".xml").toURI())
                        .getAbsolutePath();
        AnalyticsDataAPI analyticsDataAPI = new CarbonAnalyticsAPI(apiConf);

        Assert.assertTrue(analyticsDataAPI.tableExists(MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME),
                          "Table " + TABLE_NAME + " does not exists!");
        Assert.assertTrue(analyticsDataAPI.tableExists(MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME2),
                          "Table " + TABLE_NAME2 + " does not exists!");

        AnalyticsDataResponse response = analyticsDataAPI.get(MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME2, 1,
                                                              null, Long.MIN_VALUE, Long.MAX_VALUE, 0, -1);
        Assert.assertNotNull(response, "Response received is null");
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

        //for registry.xml
        fileReplacementInformationList.add(new FileReplacementInformation(
                getClusteringConfigResourceURL("registry.xml"), REGISTRY_XML_PATH, initialCarbonHome
        ) {
            @Override
            public Map<String, String> getPlaceHolderMap(String initialCarbonHome, String localhostIP) {
                return new HashMap<>();
            }
        });

        //for master-datasources.xml
        fileReplacementInformationList.add(new FileReplacementInformation(
                getClusteringConfigResourceURL("master-datasources.xml"), MASTER_DATASOURCES_PATH, initialCarbonHome
        ) {
            @Override
            public Map<String, String> getPlaceHolderMap(String initialCarbonHome, String localhostIP) {
                Map<String, String> placeHolder = new HashMap<>();
                placeHolder.put("[[[carbonHome]]]", initialCarbonHome);
                return placeHolder;
            }
        });


        return fileReplacementInformationList;
    }

    private void initializeSampleData(String instanceName) throws Exception {
        String apiConf =
                new File(this.getClass().getClassLoader().
                        getResource("clustering" + File.separator + "dasconfig" + File.separator + "api" + File.separator
                                    + "analytics-data-config-" + instanceName + ".xml").toURI())
                        .getAbsolutePath();
        AnalyticsDataAPI analyticsDataAPI = new CarbonAnalyticsAPI(apiConf);
        //Creating sample tables used to test scripts.
        log.info("Creating table :" + TABLE_NAME + " for Analytics Scripts TestCase");
        analyticsDataAPI.createTable(MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME);

        //Set schema to the table
        log.info("Set schema to the table : " + TABLE_NAME);
        List<ColumnDefinition> columnDefinitions = new ArrayList<>();
        columnDefinitions.add(new ColumnDefinition("server_name", AnalyticsSchema.ColumnType.STRING, true, false));
        columnDefinitions.add(new ColumnDefinition("ip", AnalyticsSchema.ColumnType.STRING, true, false));
        columnDefinitions.add(new ColumnDefinition("tenant", AnalyticsSchema.ColumnType.INTEGER, true, false));
        columnDefinitions.add(new ColumnDefinition("sequence", AnalyticsSchema.ColumnType.LONG, true, false));
        columnDefinitions.add(new ColumnDefinition("summary", AnalyticsSchema.ColumnType.STRING, true, false));

        analyticsDataAPI.setTableSchema(MultitenantConstants.SUPER_TENANT_ID, TABLE_NAME,
                                        new AnalyticsSchema(columnDefinitions, new ArrayList<String>(0)));

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
        analyticsDataAPI.put(recordList);
    }


    public void executeScriptContent(String instanceName, String scriptName) throws Exception {
        URL scriptResource = this.getClass().getClassLoader().getResource(getAnalyticsScriptResourcePath(scriptName));
        assert scriptResource != null;
        String scriptContent = FileManager.readFile(new File(scriptResource.toURI()));
        analyticsStubs.get(instanceName).execute(scriptContent);
    }

    private String getAnalyticsScriptResourcePath(String scriptName) {
        return SCRIPT_RESOURCE_DIR + File.separator + scriptName;
    }

    private String getAnalyticsConfigsResourcePath(String configFile) {
        return CONFIG_RESOURCE_DIR + File.separator + configFile;
    }

    private URL getClusteringConfigResourceURL(String configFile) {
        return this.getClass().getClassLoader().getResource(getAnalyticsConfigsResourcePath(configFile));
    }

    private String getMembersXMLElment(String localhostIP, int memberCount) {
        String xmlElm = "";
        for (int i = 0; i < memberCount; i++) {
            xmlElm = xmlElm + "<member>\n" +
                     "<hostName>" + localhostIP + "</hostName>\n" +
                     "<port>" + String.valueOf(4000 + HA_CLUSTER_PORT_OFFSET + i) + "</port>\n";
            xmlElm = xmlElm + "</member>\n";
        }
        return xmlElm;
    }

}

class DASClusteredTestServerManagerConstants {
    static final String ANALYTICS_DATASOURCES_PATH = "repository" + File.separator + "conf" + File.separator
                                                     + "datasources" + File.separator + "analytics-datasources.xml";
    static final String MASTER_DATASOURCES_PATH = "repository" + File.separator + "conf" + File.separator
                                                  + "datasources" + File.separator + "master-datasources.xml";
    static final String SPARK_DEFAULTS_CONF_PATH = "repository" + File.separator + "conf" + File.separator +
                                                   "analytics" + File.separator + "spark" + File.separator +
                                                   "spark-defaults.conf";
    static final String CARBON_XML_PATH = "repository" + File.separator + "conf" + File.separator + "carbon.xml";
    static final String REGISTRY_XML_PATH = "repository" + File.separator + "conf" + File.separator + "registry.xml";
    static final String AXIS2_XML_PATH = "repository" + File.separator + "conf" + File.separator + "axis2" +
                                         File.separator + "axis2.xml";
}
