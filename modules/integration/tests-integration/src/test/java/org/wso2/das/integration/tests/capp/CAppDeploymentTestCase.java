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
package org.wso2.das.integration.tests.capp;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.analytics.api.CarbonAnalyticsAPI;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.spark.admin.stub.AnalyticsProcessorAdminServiceAnalyticsProcessorAdminExceptionException;
import org.wso2.carbon.analytics.spark.admin.stub.AnalyticsProcessorAdminServiceStub;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.das.integration.common.utils.DASIntegrationTest;
import org.wso2.das.integration.tests.activity.dashboard.ActivityDataPublisher;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class CAppDeploymentTestCase extends DASIntegrationTest {
    private static final String ANALYTICS_SERVICE_NAME = "AnalyticsProcessorAdminService";

    private AnalyticsProcessorAdminServiceStub analyticsProcessorStub;
    private AnalyticsDataAPI analyticsAPI;

    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        super.init();
        initAnalyticsAPI();
        initProcessorStub();
        copyCarFile();
        try {
            Thread.sleep(20000);
        } catch (Exception ignored) {
        }
    }

    private void copyCarFile() throws IOException {
        String carFile = FrameworkPathUtil.getSystemResourceLocation() +
                "capp" + File.separator + "DASTestCApp.car";
        String carbonAppsDir = FrameworkPathUtil.getCarbonHome() + File.separator + "repository"
                + File.separator + "deployment" + File.separator + "server" + File.separator + "carbonapps" + File.separator;
        FileManager.copyResourceToFileSystem(carFile, carbonAppsDir, "DASTestCApp.car");
    }

    private void initAnalyticsAPI() throws URISyntaxException {
        String apiConf =
                new File(this.getClass().getClassLoader().
                        getResource("dasconfig" + File.separator + "api" + File.separator + "analytics-data-config.xml").toURI())
                        .getAbsolutePath();
        this.analyticsAPI = new CarbonAnalyticsAPI(apiConf);
    }

    private void initProcessorStub() throws Exception {
        ConfigurationContext configContext = ConfigurationContextFactory.
                createConfigurationContextFromFileSystem(null);
        String loggedInSessionCookie = getSessionCookie();
        analyticsProcessorStub = new AnalyticsProcessorAdminServiceStub(configContext,
                backendURL + ANALYTICS_SERVICE_NAME);
        ServiceClient client = analyticsProcessorStub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                loggedInSessionCookie);
    }

    @Test(groups = "wso2.bam", description = "checking the script deployment")
    public void testSparkScriptDeployment() throws RemoteException, AnalyticsProcessorAdminServiceAnalyticsProcessorAdminExceptionException {
        AnalyticsProcessorAdminServiceStub.AnalyticsScriptDto[] scriptDtos = analyticsProcessorStub.getAllScripts();
        Assert.assertTrue(scriptDtos != null, "Empty scripts returned, therefore the scripts wasn't deployed as expected from the car file!");
        for (AnalyticsProcessorAdminServiceStub.AnalyticsScriptDto analyticsScriptDto : scriptDtos) {
            if (analyticsScriptDto.getName().equals("sample_script")) {
                return;
            }
        }
        Assert.fail("No scripts found with name : sample_script");
    }

    @Test(groups = "wso2.bam", description = "checking eventstore deployment", dependsOnMethods = "testSparkScriptDeployment")
    public void testEventStoreDeployment() throws RemoteException, AnalyticsProcessorAdminServiceAnalyticsProcessorAdminExceptionException,
            AnalyticsException, DataEndpointException, DataEndpointConfigurationException, URISyntaxException, DataEndpointAuthenticationException, DataEndpointAgentConfigurationException, TransportException {
        Assert.assertTrue(this.analyticsAPI.tableExists(-1234, "ORG_WSO2_TEST"), "Table wasn't created for the events store : ORG_WSO2_TEST");

        ActivityDataPublisher dataPublisher = new ActivityDataPublisher("tcp://localhost:8311");
        List<String> activityIds = new ArrayList<>();
        activityIds.add("6cecbb16-6b89-46f3-bd2f-fd9f7ac447b6");
        activityIds.add("7cecbb16-6b89-46f3-bd2f-fd9f7ac447b6");
        activityIds.add("8cecbb16-6b89-46f3-bd2f-fd9f7ac447b6");
        activityIds.add("9cecbb16-6b89-46f3-bd2f-fd9f7ac447b6");
        activityIds.add("0cecbb16-6b89-46f3-bd2f-fd9f7ac447b6");

        dataPublisher.publish("org.wso2.test", "1.0.0", activityIds);
        dataPublisher.shutdown();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ignored) {
        }
        long count = this.analyticsAPI.getRecordCount(-1234, "ORG_WSO2_TEST", Long.MIN_VALUE, Long.MAX_VALUE);
        if (count != -1) {
            Assert.assertEquals(count, 100);
        }

    }

    @Test(groups = "wso2.bam", description = "checking car file undeployment", dependsOnMethods = "testEventStoreDeployment")
    public void testUndeployment() {
        String carbonApps = FrameworkPathUtil.getCarbonHome() + File.separator + "repository"
                + File.separator + "deployment" + File.separator + "server" + File.separator + "carbonapps" + File.separator + "DASTestCApp.car";
        FileManager.deleteFile(carbonApps);
        try {
            Thread.sleep(20000);
        } catch (InterruptedException ignored) {
        }
    }

    @Test(groups = "wso2.bam", description = "checking car file undeployment of spark script", dependsOnMethods = "testUndeployment")
    public void checkSparkScriptUndeployment() throws RemoteException, AnalyticsProcessorAdminServiceAnalyticsProcessorAdminExceptionException {
        AnalyticsProcessorAdminServiceStub.AnalyticsScriptDto[] scriptDtos = analyticsProcessorStub.getAllScripts();
        if (scriptDtos != null) {
            for (AnalyticsProcessorAdminServiceStub.AnalyticsScriptDto analyticsScriptDto : scriptDtos) {
                if (analyticsScriptDto.getName().equals("sample_script")) {
                    Assert.fail("Analytics script wasn't removed, and it still exists in the list of scripts : sample_script");
                }
            }
        }
    }

    @Test(groups = "wso2.bam", description = "checking car file undeployment of spark script", dependsOnMethods = "checkSparkScriptUndeployment")
    public void checkEventStoreUndeployment() throws RemoteException, AnalyticsProcessorAdminServiceAnalyticsProcessorAdminExceptionException, DataEndpointException, DataEndpointConfigurationException, URISyntaxException, DataEndpointAuthenticationException, DataEndpointAgentConfigurationException, TransportException, AnalyticsException {
        ActivityDataPublisher dataPublisher = new ActivityDataPublisher("tcp://localhost:8311");
        List<String> activityIds = new ArrayList<>();
        activityIds.add("6cecbb16-6b89-46f3-bd2f-fd9f7ac447b6");
        activityIds.add("7cecbb16-6b89-46f3-bd2f-fd9f7ac447b6");
        activityIds.add("8cecbb16-6b89-46f3-bd2f-fd9f7ac447b6");
        activityIds.add("9cecbb16-6b89-46f3-bd2f-fd9f7ac447b6");
        activityIds.add("0cecbb16-6b89-46f3-bd2f-fd9f7ac447b6");

        dataPublisher.publish("org.wso2.test", "1.0.0", activityIds);
        dataPublisher.shutdown();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ignored) {
        }
        long count = this.analyticsAPI.getRecordCount(-1234, "ORG_WSO2_TEST", Long.MIN_VALUE, Long.MAX_VALUE);
        if (count != -1) {
            Assert.assertEquals(count, 100);
        }
    }
}
