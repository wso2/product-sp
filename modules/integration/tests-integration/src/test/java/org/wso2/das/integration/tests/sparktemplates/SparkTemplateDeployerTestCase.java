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

package org.wso2.das.integration.tests.sparktemplates;

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
import org.wso2.carbon.event.execution.manager.admin.dto.configuration.xsd.ParameterDTOE;
import org.wso2.carbon.event.execution.manager.admin.dto.configuration.xsd.ScenarioConfigurationDTO;
import org.wso2.carbon.event.execution.manager.admin.dto.domain.xsd.ExecutionManagerTemplateInfoDTO;
import org.wso2.carbon.event.execution.manager.admin.dto.domain.xsd.ParameterDTO;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.das.integration.common.clients.ExecutionManagerAdminServiceClient;
import org.wso2.das.integration.common.utils.DASIntegrationTest;

import java.io.File;

public class SparkTemplateDeployerTestCase extends DASIntegrationTest {
    private static final String ANALYTICS_SERVICE_NAME = "AnalyticsProcessorAdminService";

    private static final Log log = LogFactory.getLog(SparkTemplateDeployerTestCase.class);
    private int eventStreamCount;
    private int scriptCount;
    private int configurationCount;
    private ServerConfigurationManager serverManager;
    private ExecutionManagerAdminServiceClient executionManagerAdminServiceClient;
    private AnalyticsProcessorAdminServiceStub analyticsStub;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        serverManager = new ServerConfigurationManager(dasServer);

        File newFile = new File(getClass().getResource(File.separator + "sparktemplates" + File.separator
                + "TestDomain.xml").toURI());
        FileUtils.copyFileToDirectory(newFile, new File(ServerConfigurationManager.getCarbonHome() + File.separator
                + "repository" + File.separator + "conf" + File.separator + "execution-manager" + File.separator
                + "domain-template" + File.separator));
        serverManager.restartForcefully();

        initExecutionManagerAdminServiceClient();
        initAnalyticsProcessorStub();
    }


    @Test(groups = {"wso2.das"}, description = "Testing the addition of configuration for a domain template")
    public void addTemplateConfigurationTestScenario1() throws Exception {

        ExecutionManagerTemplateInfoDTO executionManagerTemplate = executionManagerAdminServiceClient
                .getExecutionManagerTemplateInfo("TestDomain");

        if (executionManagerTemplate == null) {
            Assert.fail("Domain is not loaded");
        } else {

            log.info("==================Testing the adding a configuration for a domain template==================== ");

            log.info("=======================Adding a configuration====================");
            ScenarioConfigurationDTO scenarioConfigurationDTO = new ScenarioConfigurationDTO();

            scenarioConfigurationDTO.setName("TestConfig");
            scenarioConfigurationDTO.setDomain(executionManagerTemplate.getDomain());
            scenarioConfigurationDTO.setScenario(executionManagerTemplate.getScenarioInfoDTOs()[0].getName());
            scenarioConfigurationDTO.setDescription("This is a test description");

            for (ParameterDTO parameterDTO : executionManagerTemplate.getScenarioInfoDTOs()[0].getParameterDTOs()) {
                ParameterDTOE parameterDTOE = new ParameterDTOE();
                parameterDTOE.setName(parameterDTO.getName());

                if (parameterDTO.getType().toLowerCase().equals("int")) {
                    parameterDTOE.setValue("2");
                } else if (parameterDTO.getType().toLowerCase().equals("string")) {
                    parameterDTOE.setValue("test");
                }

                scenarioConfigurationDTO.addParameterDTOs(parameterDTOE);
            }

            AnalyticsProcessorAdminServiceStub.AnalyticsScriptDto[] scripts = analyticsStub.getAllScripts();
            if (scripts != null) {
                scriptCount = scripts.length;
            } else {
                scriptCount = 0;
            }
            configurationCount = executionManagerAdminServiceClient.getConfigurationsCount(executionManagerTemplate.getDomain());

            executionManagerAdminServiceClient.saveConfiguration(scenarioConfigurationDTO);

            //Number of configurations should be incremented by one
            Assert.assertEquals(executionManagerAdminServiceClient.getConfigurationsCount(executionManagerTemplate.getDomain()),
                    ++configurationCount, "After adding configuration, expected configuration count is incorrect");

            //There is one script for template, which will be deployed when a configuration added
            Assert.assertEquals(analyticsStub.getAllScripts().length,
                    ++scriptCount, "After adding configuration, expected Spark Script count is incorrect");

            log.info("=======================Edit a configuration====================");
            scenarioConfigurationDTO.setDescription("Description edited");
            executionManagerAdminServiceClient.saveConfiguration(scenarioConfigurationDTO);
            //When existing configuration is been updated, the batch script will be un-deployed and redeployed
            Assert.assertEquals(analyticsStub.getAllScripts().length,
                    scriptCount, "After editing configuration, expected Spark Script count is incorrect");
//            Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), eventStreamCount);
            Assert.assertEquals(executionManagerAdminServiceClient.getConfigurationsCount(executionManagerTemplate.getDomain()),
                    configurationCount, "After editing configuration, expected configuration count is incorrect");


            log.info("=======================Delete a configuration====================");
            executionManagerAdminServiceClient.deleteConfiguration(scenarioConfigurationDTO.getDomain(), scenarioConfigurationDTO.getName());
            //When configuration is deleted the script will be un-deployed so count should be decremented
            scripts = analyticsStub.getAllScripts();
            int currentScriptCount = 0;
            if (scripts != null) {
                currentScriptCount = scripts.length;
            }
            Assert.assertEquals(currentScriptCount, --scriptCount, "After deleting configuration, expected Spark Script count is incorrect");
            //When configuration is deleted the configuration count should be decremented by one
            Assert.assertEquals(executionManagerAdminServiceClient.getConfigurationsCount(executionManagerTemplate.getDomain()),
                    --configurationCount, "After deleting configuration, expected configuration count is incorrect");
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


    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {

    }
}