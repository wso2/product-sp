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
import org.wso2.carbon.analytics.spark.admin.stub.AnalyticsProcessorAdminServiceStub;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.event.template.manager.admin.dto.configuration.xsd.ConfigurationParameterDTO;
import org.wso2.carbon.event.template.manager.admin.dto.configuration.xsd.ScenarioConfigurationDTO;
import org.wso2.carbon.event.template.manager.admin.dto.domain.xsd.DomainInfoDTO;
import org.wso2.carbon.event.template.manager.admin.dto.domain.xsd.DomainParameterDTO;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.das.integration.common.clients.TemplateManagerAdminServiceClient;
import org.wso2.das.integration.common.utils.DASIntegrationTest;

import java.io.File;

public class SparkTemplateDeployerTestCase extends DASIntegrationTest {
    private static final String ANALYTICS_SERVICE_NAME = "AnalyticsProcessorAdminService";

    private static final Log log = LogFactory.getLog(SparkTemplateDeployerTestCase.class);
    private int eventStreamCount;
    private int scriptCount;
    private int configurationCount;
    private ServerConfigurationManager serverManager;
    private TemplateManagerAdminServiceClient templateManagerAdminServiceClient;
    private AnalyticsProcessorAdminServiceStub analyticsStub;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        serverManager = new ServerConfigurationManager(dasServer);

        File newFile = new File(getClass().getResource(File.separator + "templatemanager" + File.separator
                + "TestDomain.xml").toURI());
        FileUtils.copyFileToDirectory(newFile, new File(ServerConfigurationManager.getCarbonHome() + File.separator
                + "repository" + File.separator + "conf" + File.separator + "template-manager" + File.separator
                + "domain-template" + File.separator));
        serverManager.restartForcefully();

        initTemplateManagerAdminServiceClient();
        initAnalyticsProcessorStub();
    }


    @Test(groups = {"wso2.das"}, description = "Testing the addition of configuration for a domain template")
    public void addTemplateConfigurationTestScenario1() throws Exception {

        DomainInfoDTO domainInfo = templateManagerAdminServiceClient
                .getDomainInfo("TestDomain");

        if (domainInfo == null) {
            Assert.fail("Domain is not loaded");
        } else {

            log.info("==================Testing the adding a configuration for a domain template==================== ");

            log.info("=======================Adding a configuration====================");
            ScenarioConfigurationDTO scenarioConfigurationDTO = new ScenarioConfigurationDTO();

            scenarioConfigurationDTO.setName("TestConfig");
            scenarioConfigurationDTO.setDomain(domainInfo.getName());
            scenarioConfigurationDTO.setType(domainInfo.getScenarioInfoDTOs()[0].getType());
            scenarioConfigurationDTO.setDescription("This is a test description");

            for (DomainParameterDTO domainParameterDTO : domainInfo.getScenarioInfoDTOs()[0].getDomainParameterDTOs()) {
                ConfigurationParameterDTO configurationParameterDTO = new ConfigurationParameterDTO();
                configurationParameterDTO.setName(domainParameterDTO.getName());

                if (domainParameterDTO.getType().toLowerCase().equals("int")) {
                    configurationParameterDTO.setValue("2");
                } else if (domainParameterDTO.getType().toLowerCase().equals("string")) {
                    configurationParameterDTO.setValue("test");
                }

                scenarioConfigurationDTO.addConfigurationParameterDTOs(configurationParameterDTO);
            }

            AnalyticsProcessorAdminServiceStub.AnalyticsScriptDto[] scripts = analyticsStub.getAllScripts();
            if (scripts != null) {
                scriptCount = scripts.length;
            } else {
                scriptCount = 0;
            }
            configurationCount = templateManagerAdminServiceClient.getConfigurationsCount(domainInfo.getName());

            templateManagerAdminServiceClient.saveConfiguration(scenarioConfigurationDTO);

            //Number of configurations should be incremented by one
            Assert.assertEquals(templateManagerAdminServiceClient.getConfigurationsCount(domainInfo.getName()),
                    ++configurationCount, "After adding configuration, expected configuration count is incorrect");

            //There is one script for template, which will be deployed when a configuration added
            Assert.assertEquals(analyticsStub.getAllScripts().length,
                    ++scriptCount, "After adding configuration, expected Spark Script count is incorrect");

            log.info("=======================Edit a configuration====================");
            scenarioConfigurationDTO.setDescription("Description edited");
            templateManagerAdminServiceClient.editConfiguration(scenarioConfigurationDTO);
            //When existing configuration is been updated, the batch script will be un-deployed and redeployed
            Assert.assertEquals(analyticsStub.getAllScripts().length,
                    scriptCount, "After editing configuration, expected Spark Script count is incorrect");
//            Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), eventStreamCount);
            Assert.assertEquals(templateManagerAdminServiceClient.getConfigurationsCount(domainInfo.getName()),
                    configurationCount, "After editing configuration, expected configuration count is incorrect");


            log.info("=======================Delete a configuration====================");
            templateManagerAdminServiceClient.deleteConfiguration(scenarioConfigurationDTO.getDomain(), scenarioConfigurationDTO.getName());
            //When configuration is deleted the script will be un-deployed so count should be decremented
            scripts = analyticsStub.getAllScripts();
            int currentScriptCount = 0;
            if (scripts != null) {
                currentScriptCount = scripts.length;
            }
            Assert.assertEquals(currentScriptCount, --scriptCount, "After deleting configuration, expected Spark Script count is incorrect");
            //When configuration is deleted the configuration count should be decremented by one
            Assert.assertEquals(templateManagerAdminServiceClient.getConfigurationsCount(domainInfo.getName()),
                    --configurationCount, "After deleting configuration, expected configuration count is incorrect");
        }

    }

    private void initTemplateManagerAdminServiceClient()
            throws Exception {

        String loggedInSessionCookie = getSessionCookie();
        templateManagerAdminServiceClient = new TemplateManagerAdminServiceClient(backendURL, loggedInSessionCookie);
        ServiceClient client = templateManagerAdminServiceClient._getServiceClient();
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


    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {

    }
}