/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.das.integration.tests.sessionTimeoutExceptionTest;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.spark.admin.stub.AnalyticsProcessorAdminServiceAnalyticsProcessorAdminExceptionException;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.extensions.servers.carbonserver.MultipleServersManager;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.integration.common.admin.client.LogViewerClient;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.logging.view.stub.LogViewerLogViewerException;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.das.integration.common.utils.DASIntegrationTest;
import org.wso2.das.integration.tests.activity.dashboard.ActivityDataPublisher;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionTimeoutExceptionTest extends DASIntegrationTest {

    protected LogViewerClient logViewerClient;
    private MultipleServersManager manager = new MultipleServersManager();
    private Map<String, String> startupParameterMap1 = new HashMap<String, String>();
    private AutomationContext context;

    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        super.init();
        copyLog4jPropertiesFile();
        String session = getSessionCookie();
        logViewerClient = new LogViewerClient(backendURL, session);
        startServers();
        Thread.sleep(20000);
    }

    private void copyLog4jPropertiesFile() throws IOException, XPathExpressionException, AutomationUtilException {
        String remoteAPIConfResource = FrameworkPathUtil.getSystemResourceLocation() +
                "sessiontimeoutlogconfig" + File.separator + "log4j.properties";
        String analyticsAPIConf = FrameworkPathUtil.getCarbonHome() + File.separator + "repository"
                + File.separator + "conf" + File.separator;
        FileManager.copyResourceToFileSystem(remoteAPIConfResource, analyticsAPIConf, "log4j.properties");
        ServerConfigurationManager serverConfigurationManager =
                new ServerConfigurationManager(dasServer);
        serverConfigurationManager.restartGracefully();
    }

    private void copyDatabridgeConfigFile(String carbonHome) throws IOException, XPathExpressionException, AutomationUtilException {
        String remoteAPIConfResource = FrameworkPathUtil.getSystemResourceLocation() +
                "databridgeconfig" + File.separator + "data-bridge-config.xml";
        String analyticsAPIConf = carbonHome + File.separator + "repository"
                + File.separator + "conf" + File.separator + "data-bridge" + File.separator;
        FileManager.copyResourceToFileSystem(remoteAPIConfResource, analyticsAPIConf, "data-bridge-config.xml");
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void startServers() throws Exception {
        context = new AutomationContext();
        startupParameterMap1.put("-DportOffset", "30");
        CarbonTestServerManager server1 = new CarbonTestServerManager(context, System.getProperty("carbon.zip"),
                startupParameterMap1);
        manager.startServers(server1);
        Thread.sleep(60000l);
        copyDatabridgeConfigFile(server1.getCarbonHome());
        server1.restartGracefully();
        Thread.sleep(30000l);
        copyCarFile(server1.getCarbonHome());
    }

    private void copyCarFile(String carbonHome) throws IOException {
        String carFile = FrameworkPathUtil.getSystemResourceLocation() +
                "capp" + File.separator + "DASTestCApp.car";
        String carbonAppsDir = carbonHome + File.separator + "repository"
                + File.separator + "deployment" + File.separator + "server" + File.separator + "carbonapps" + File.separator;
        FileManager.copyResourceToFileSystem(carFile, carbonAppsDir, "DASTestCApp.car");
    }

    @AfterClass(alwaysRun = true)
    public void cleanupProcess() throws AutomationFrameworkException, IOException, XPathExpressionException, AutomationUtilException {
        manager.stopAllServers();
        String remoteAPIConfResource = FrameworkPathUtil.getSystemResourceLocation() +
                "config" + File.separator + "log4j.properties";
        String analyticsAPIConf = FrameworkPathUtil.getCarbonHome() + File.separator + "repository"
                + File.separator + "conf" + File.separator;
        FileManager.copyResourceToFileSystem(remoteAPIConfResource, analyticsAPIConf, "log4j.properties");
        ServerConfigurationManager serverConfigurationManager =
                new ServerConfigurationManager(dasServer);
        serverConfigurationManager.restartGracefully();
    }

    @Test(description = "publishing events to the stream")
    public void testSessionTimeout() throws RemoteException, AnalyticsProcessorAdminServiceAnalyticsProcessorAdminExceptionException,
            AnalyticsException, DataEndpointException, DataEndpointConfigurationException, URISyntaxException, DataEndpointAuthenticationException, DataEndpointAgentConfigurationException, TransportException, InterruptedException, LogViewerLogViewerException {

        ActivityDataPublisher dataPublisher = new ActivityDataPublisher("tcp://localhost:9641","binary");
        List<String> activityIds = new ArrayList<>();
        activityIds.add("6cecbb16-6b89-46f3-bd2f-fd9f7ac447b6");
        activityIds.add("7cecbb16-6b89-46f3-bd2f-fd9f7ac447b6");
        activityIds.add("8cecbb16-6b89-46f3-bd2f-fd9f7ac447b6");
        activityIds.add("9cecbb16-6b89-46f3-bd2f-fd9f7ac447b6");
        activityIds.add("0cecbb16-6b89-46f3-bd2f-fd9f7ac447b6");

        logViewerClient.clearLogs();
        dataPublisher.publish("org.wso2.test", "1.0.0", activityIds,1000);
        Thread.sleep(1000);
        boolean exceptionThrown = isExceptionPrinted("Error while trying to publish events to data receiver :", 10 ,1000);

        Assert.assertFalse(exceptionThrown, "Session timeout exception is not handled");
        dataPublisher.shutdown();
    }

    protected boolean isExceptionPrinted(String message, int maxRetries, long sleepTime) throws RemoteException, LogViewerLogViewerException, InterruptedException {
        boolean exceptionPrinted = false;
        int j = 0;
        while (j < maxRetries) {
            Thread.sleep(sleepTime);
            LogEvent[] logs = logViewerClient.getAllRemoteSystemLogs();

            if(logs != null){
                for (int i = 0; i < (logs.length); i++) {
                    if (logs[i].getMessage().contains(message)) {
                        exceptionPrinted = true;
                        break;
                    }
                }
            }
            if(exceptionPrinted){
                break;
            }
            j++;
        }
        return exceptionPrinted;
    }
}
