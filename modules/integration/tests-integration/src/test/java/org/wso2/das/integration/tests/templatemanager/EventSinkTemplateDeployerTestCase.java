/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.das.integration.tests.templatemanager;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.eventsink.AnalyticsEventStore;
import org.wso2.carbon.analytics.spark.admin.stub.AnalyticsProcessorAdminServiceStub;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.event.execution.manager.admin.dto.configuration.xsd.ConfigurationParameterDTO;
import org.wso2.carbon.event.execution.manager.admin.dto.configuration.xsd.ScenarioConfigurationDTO;
import org.wso2.carbon.event.execution.manager.admin.dto.domain.xsd.DomainInfoDTO;
import org.wso2.carbon.event.execution.manager.admin.dto.domain.xsd.DomainParameterDTO;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.das.integration.common.clients.ExecutionManagerAdminServiceClient;
import org.wso2.das.integration.common.utils.DASIntegrationTest;
import org.wso2.das.integration.tests.templatemanager.util.TemplateManagerTestUtil;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.rmi.RemoteException;
import java.util.List;

public class EventSinkTemplateDeployerTestCase extends DASIntegrationTest {
    private static final String ANALYTICS_SERVICE_NAME = "AnalyticsProcessorAdminService";
    private static final Log log = LogFactory.getLog(SparkTemplateDeployerTestCase.class);

    private static final String STREAM_NAME = "stream";
    private static final String TEST_CONFIG_1 = "TestConfig1";
    private static final String TEST_CONFIG_2 = "TestConfig2";
    private static final String TEST_CONFIG_3 = "TestConfig3";
    private static final String SCENARIO_1 = "scenario1";
    private static final String SCENARIO_2 = "scenario2";
    private static final String SCENARIO_3 = "scenario3";

    private ServerConfigurationManager serverManager;
    private ExecutionManagerAdminServiceClient executionManagerAdminServiceClient;
    private AnalyticsProcessorAdminServiceStub analyticsStub;
    private int configurationCount;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        serverManager = new ServerConfigurationManager(dasServer);

        File newFile = new File(getClass().getResource(File.separator + "templatemanager" + File.separator
                + "EventSinkTestDomain.xml").toURI());
        FileUtils.copyFileToDirectory(newFile, new File(ServerConfigurationManager.getCarbonHome() + File.separator
                + "repository" + File.separator + "conf" + File.separator + "template-manager" + File.separator
                + "domain-template" + File.separator));
        serverManager.restartForcefully();

        initExecutionManagerAdminServiceClient();
        initAnalyticsProcessorStub();
    }

    @Test(groups = {"wso2.das"}, description = "Testing the add/edit/delete of templates, given in a domain template")
    public void addTemplateConfigurationTestScenario1() throws Exception {
        DomainInfoDTO domainInfo = executionManagerAdminServiceClient
                .getDomainInfo("EventSinkTestDomain");

        if (domainInfo == null) {
            Assert.fail("Domain is not loaded");
        } else {
            configurationCount = executionManagerAdminServiceClient.getConfigurationsCount(domainInfo.getName());
            log.info("================== Adding scenario1 of EventSinkTestDomain ==================== ");
            saveConfiguration(domainInfo, TEST_CONFIG_1, SCENARIO_1);
            log.info("================== Adding scenario1 saved config of EventSinkTestDomain ==================== ");

            //Number of configurations should be incremented by one
            Assert.assertEquals(executionManagerAdminServiceClient.getConfigurationsCount(domainInfo.getName()),
                    ++configurationCount, "After adding " + TEST_CONFIG_1 + ", expected configuration count is incorrect");

            waitForEventSinkDeployment(1000000, 100000);
            waitForEventSinkUpdate(2, 20000000, 80000);
            log.info("================== Adding scenario1 going to assert ==================== ");
            Assert.assertTrue(TemplateManagerTestUtil.isEventSinkExists(STREAM_NAME));
            AnalyticsEventStore existingEventStore1 = TemplateManagerTestUtil.getExistingEventStore(STREAM_NAME);    //todo: remove later
            List<String> streamIdList1 = existingEventStore1.getEventSource().getStreamIds();
            log.info("================== Adding scenario1: stream id list count: "+ streamIdList1.size() +"==================== ");
            Assert.assertEquals(streamIdList1.size(), 2, "Subscribed to incorrect number of Streams");

            log.info("================== Adding scenario2 of EventSinkTestDomain ==================== ");
            saveConfiguration(domainInfo, TEST_CONFIG_2, SCENARIO_2);
            log.info("================== Adding scenario2 saved ==================== ");

            //Number of configurations should be incremented by one
            Assert.assertEquals(executionManagerAdminServiceClient.getConfigurationsCount(domainInfo.getName()),
                    ++configurationCount, "After adding " + TEST_CONFIG_2 + ", expected configuration count is incorrect");
            waitForEventSinkUpdate(3, 20000, 80000);
            Assert.assertTrue(TemplateManagerTestUtil.isEventSinkExists(STREAM_NAME));
            AnalyticsEventStore existingEventStore = TemplateManagerTestUtil.getExistingEventStore(STREAM_NAME);
            List<String> streamIdList = existingEventStore.getEventSource().getStreamIds();
            Assert.assertEquals(streamIdList.size(), 3, "Subscribed to incorrect number of Streams");
            Assert.assertTrue(streamIdList.contains("stream:3.0.0"));

            log.info("================== Adding scenario3 of EventSinkTestDomain ==================== ");
            //asserting whether an overwriting config deployment fails.
            try {
                saveConfiguration(domainInfo, TEST_CONFIG_3, SCENARIO_3);
                Assert.fail("Overwriting Event Sink configuration did not fail.");
            } catch (RemoteException e) {
                Assert.assertEquals(executionManagerAdminServiceClient.getConfigurationsCount(domainInfo.getName()),
                        configurationCount, "Adding of " + TEST_CONFIG_3 + " failed as expected. However, " +
                                "expected configuration count is incorrect. Count should stay same.");
            }

            log.info("================== Updating scenario2 of EventSinkTestDomain ==================== ");
            editConfiguration(domainInfo, TEST_CONFIG_2, SCENARIO_2);
            Assert.assertEquals(executionManagerAdminServiceClient.getConfigurationsCount(domainInfo.getName()),
                    configurationCount, "After updating " + TEST_CONFIG_2 + ", expected configuration count is incorrect");
            waitForEventSinkUpdate(3, 20000, 80000);
            Assert.assertTrue(TemplateManagerTestUtil.isEventSinkExists(STREAM_NAME));

            existingEventStore = TemplateManagerTestUtil.getExistingEventStore(STREAM_NAME);
            streamIdList = existingEventStore.getEventSource().getStreamIds();
            Assert.assertEquals(streamIdList.size(), 3, "After updating Scenario Configuration, subscribed Event Stream count has changed.");

            log.info("================== Deleting scenario1 of EventSinkTestDomain ==================== ");
            //this should not actually delete the event sink config because scenario2 is also needs it.
            executionManagerAdminServiceClient.deleteConfiguration(domainInfo.getName(), TEST_CONFIG_1);
            waitForEventSinkUpdate(1, 20000, 80000); //deleting will result in a update to existing event sink. Hence the need to wait.  //todo: add constant for 1 because it's used in 2 places.
            //Number of configurations should be decremented by one
            Assert.assertEquals(executionManagerAdminServiceClient.getConfigurationsCount(domainInfo.getName()),
                    --configurationCount, "After deleting " + TEST_CONFIG_1 + ", expected configuration count is incorrect");
            Assert.assertTrue(TemplateManagerTestUtil.isEventSinkExists(STREAM_NAME));

            existingEventStore = TemplateManagerTestUtil.getExistingEventStore(STREAM_NAME);
            streamIdList = existingEventStore.getEventSource().getStreamIds();
            Assert.assertEquals(streamIdList.size(), 1, "Subscribed Event Stream count is incorrect after deleting " + TEST_CONFIG_1);
            streamIdList.contains("stream:3.0.0");

            log.info("================== Deleting scenario2 of EventSinkTestDomain ==================== ");
            executionManagerAdminServiceClient.deleteConfiguration(domainInfo.getName(), TEST_CONFIG_2);
            waitForEventSinkUndeployment(20000, 100000);
            Assert.assertEquals(executionManagerAdminServiceClient.getConfigurationsCount(domainInfo.getName()),
                    --configurationCount, "After deleting " + TEST_CONFIG_2 + ", expected configuration count is incorrect");
            Assert.assertFalse(TemplateManagerTestUtil.isEventSinkExists(STREAM_NAME));
        }
    }


    private void initExecutionManagerAdminServiceClient()
            throws Exception {

        String loggedInSessionCookie = getSessionCookie();
        executionManagerAdminServiceClient = new ExecutionManagerAdminServiceClient(backendURL, loggedInSessionCookie);
        ServiceClient client = executionManagerAdminServiceClient._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, loggedInSessionCookie);
    }


    private void initAnalyticsProcessorStub() throws Exception {
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

    private void waitForEventSinkDeployment(long sleepTime, long timeout)
            throws InterruptedException, RemoteException {
        long currentWaitTime = 0;
        long startTime = System.currentTimeMillis();
        Boolean isEventSinkExists = TemplateManagerTestUtil.isEventSinkExists(STREAM_NAME);
        while ((!isEventSinkExists) && (currentWaitTime <= timeout)) {
            Thread.sleep(sleepTime);
            isEventSinkExists = TemplateManagerTestUtil.isEventSinkExists(STREAM_NAME);
            currentWaitTime = System.currentTimeMillis() - startTime;
        }
    }

    private void waitForEventSinkUndeployment(long sleepTime, long timeout)
            throws InterruptedException, RemoteException {
        long currentWaitTime = 0;
        long startTime = System.currentTimeMillis();
        Boolean isEventSinkExists = TemplateManagerTestUtil.isEventSinkExists(STREAM_NAME);
        while ((isEventSinkExists) && (currentWaitTime <= timeout)) {
            Thread.sleep(sleepTime);
            isEventSinkExists = TemplateManagerTestUtil.isEventSinkExists(STREAM_NAME);
            currentWaitTime = System.currentTimeMillis() - startTime;
        }
    }

    /**
     *
     * @param streamIdCount
     * @param sleepTime Better to have about 3 to 5 sec @param sleepTime because after each wake up, an unmarshalling operation will happen.
     * @param timeout
     * @throws JAXBException
     * @throws InterruptedException
     */
    private void waitForEventSinkUpdate(int streamIdCount, long sleepTime, long timeout) throws JAXBException, InterruptedException {
        long currentWaitTime = 0;
        long startTime = System.currentTimeMillis();
        AnalyticsEventStore existingEventStore = TemplateManagerTestUtil.getExistingEventStore(STREAM_NAME);
        int actualCount = existingEventStore.getEventSource().getStreamIds().size();
        while ((actualCount != streamIdCount) && (currentWaitTime <= timeout)) {
            Thread.sleep(sleepTime);
            existingEventStore = TemplateManagerTestUtil.getExistingEventStore(STREAM_NAME);
            actualCount = existingEventStore.getEventSource().getStreamIds().size();
            currentWaitTime = System.currentTimeMillis() - startTime;
        }
    }

    private void saveConfiguration(DomainInfoDTO domainInfo, String configName, String configType) throws RemoteException {
        ScenarioConfigurationDTO scenario1Config = new ScenarioConfigurationDTO();

        scenario1Config.setName(configName);
        scenario1Config.setDomain(domainInfo.getName());
        scenario1Config.setType(configType);
        scenario1Config.setDescription("This is a test description");

        for (DomainParameterDTO domainParameterDTO : domainInfo.getScenarioInfoDTOs()[0].getDomainParameterDTOs()) {
            ConfigurationParameterDTO configurationParameterDTO = new ConfigurationParameterDTO();
            configurationParameterDTO.setName(domainParameterDTO.getName());
            configurationParameterDTO.setValue(domainParameterDTO.getDefaultValue());
            scenario1Config.addConfigurationParameterDTOs(configurationParameterDTO);
        }
        executionManagerAdminServiceClient.saveConfiguration(scenario1Config);
    }

    private void editConfiguration(DomainInfoDTO domainInfo, String configName, String configType) throws RemoteException {
        ScenarioConfigurationDTO scenario1Config = new ScenarioConfigurationDTO();

        scenario1Config.setName(configName);
        scenario1Config.setDomain(domainInfo.getName());
        scenario1Config.setType(configType);
        scenario1Config.setDescription("Description edited.");

        for (DomainParameterDTO domainParameterDTO : domainInfo.getScenarioInfoDTOs()[0].getDomainParameterDTOs()) {
            ConfigurationParameterDTO configurationParameterDTO = new ConfigurationParameterDTO();
            configurationParameterDTO.setName(domainParameterDTO.getName());
            configurationParameterDTO.setValue(domainParameterDTO.getDefaultValue());
            scenario1Config.addConfigurationParameterDTOs(configurationParameterDTO);
        }
        executionManagerAdminServiceClient.saveConfiguration(scenario1Config);
    }


    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {

    }
}
