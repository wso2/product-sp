package org.wso2.bam.integration.tests.sample;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.dbcp.dbcp.BasicDataSource;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.bam.integration.tests.agents.KPIAgent;
import org.wso2.carbon.bam.toolbox.deployer.stub.BAMToolboxDepolyerServiceStub;
import org.wso2.carbon.databridge.agent.thrift.Agent;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.conf.AgentConfiguration;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.*;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;

import javax.sql.DataSource;
import java.net.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Random;

import static org.testng.Assert.assertTrue;

/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class KPISampleTestCase {
    private static final Log log = LogFactory.getLog(KPISampleTestCase.class);

    private LoginLogoutUtil util = new LoginLogoutUtil();

    private static final String TOOLBOX_DEPLOYER_SERVICE = "/services/BAMToolboxDepolyerService";

    private BAMToolboxDepolyerServiceStub toolboxStub;

    private BasicDataSource dataSource;

    private Connection connection;

    boolean installed = false;
    private String deployedToolBox = "";

    private static final int RETRY_COUNT = 30;

    private static final int HIVE_SAMPLE_EXEC_RETRY_COUNT = 120;

    @BeforeClass(groups = {"wso2.bam"})
    public void init() throws Exception {
        ConfigurationContext configContext = ConfigurationContextFactory.
                createConfigurationContextFromFileSystem(null);

        String loggedInSessionCookie = util.login();

        String EPR = "https://" + FrameworkSettings.HOST_NAME +
                ":" + FrameworkSettings.HTTPS_PORT + TOOLBOX_DEPLOYER_SERVICE;
        toolboxStub = new BAMToolboxDepolyerServiceStub(configContext, EPR);
        ServiceClient client = toolboxStub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                loggedInSessionCookie);
    }


    @Test(groups = {"wso2.bam"})
    public void kpiToolBoxDeployment() throws Exception {
        deployedToolBox = getToolBoxName();

        //Sample Id of KPI sample id - 1
        toolboxStub.deployBasicToolBox(1);

        log.info("Installing toolbox...");

        String toolBoxname = deployedToolBox.replaceAll(".tbox", "");
        installed = false;
        int noOfTry = 1;

        while (!installed && noOfTry <= RETRY_COUNT) {
            Thread.sleep(1000);

            //get List of deployed toolboxes
            BAMToolboxDepolyerServiceStub.ToolBoxStatusDTO statusDTO = toolboxStub.getDeployedToolBoxes("1", "");
            String[] deployed = statusDTO.getDeployedTools();

            if (null != deployed) {

                for (String aTool : deployed) {
                    aTool = aTool.replaceAll(".tbox", "");
                    if (aTool.equalsIgnoreCase(toolBoxname)) {
                        installed = true;
                        break;
                    }
                }
            }
            noOfTry++;
        }

        assertTrue(installed, "Installation of toolbox :" + toolBoxname + " failed!!");
    }

    @Test(groups = {"wso2.bam"}, dependsOnMethods = "kpiToolBoxDeployment")
    public void runKPIAgent() throws AgentException, MalformedURLException, AuthenticationException, MalformedStreamDefinitionException, SocketException, StreamDefinitionException, TransportException, NoStreamDefinitionExistException, DifferentStreamDefinitionAlreadyDefinedException {
        KPIAgent.publish();
//        try {
//            log.info("Waiting to run the hive analysis");
//            Thread.sleep(80000);
//        } catch (InterruptedException e) {
//        }
//        log.info("Finished waiting....");
    }

    @Test(groups = {"wso2.bam"}, dependsOnMethods = "runKPIAgent")
    public void validateData() throws SQLException {
        dataSource = (BasicDataSource) initDataSource("org.h2.Driver",
                "jdbc:h2:repository/database/samples/BAM_STATS_DB;AUTO_SERVER=TRUE",
                "wso2carbon",
                "wso2carbon");
        connection = dataSource.getConnection();

        boolean isTableExists = false;

        int noOfTry = 1;
        while (!isTableExists && noOfTry <= HIVE_SAMPLE_EXEC_RETRY_COUNT) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            Statement statement = connection.createStatement();
            ResultSet tables = statement.executeQuery("show tables");
            boolean isTableCreate = false;
            while (tables.next()) {
                String aTable = tables.getString(1);
                if (aTable.equalsIgnoreCase("brandSummary")) {
                    isTableCreate = true;
                    break;
                }
            }
            if (isTableCreate) {
                ResultSet result = statement.executeQuery("SELECT * FROM brandSummary");
                isTableExists = result.next();
            }
            noOfTry++;
        }
        assertTrue(isTableExists, "No data in the summarized table Brand Summary");

        noOfTry = 1;
        isTableExists = false;

        while (!isTableExists && noOfTry <= HIVE_SAMPLE_EXEC_RETRY_COUNT) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            Statement statement = connection.createStatement();
            ResultSet tables = statement.executeQuery("show tables");
            boolean isTableCreate = false;
            while (tables.next()) {
                String aTable = tables.getString(1);
                if (aTable.equalsIgnoreCase("UserSummary")) {
                    isTableCreate = true;
                    break;
                }
            }
            if (isTableCreate) {
                ResultSet result = statement.executeQuery("SELECT * FROM UserSummary");
                isTableExists = result.next();
            }
            noOfTry++;
        }

        assertTrue(isTableExists, "No data in the summarized table Brand Summary");
    }

    private DataSource initDataSource(String driverName, String url, String username, String password) {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(driverName);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    public void undeployDefaultToolbox() throws Exception {
        String toolBoxname = deployedToolBox.replaceAll(".tbox", "");
        toolboxStub.undeployToolBox(new String[]{toolBoxname});

        boolean unInstalled = false;

        log.info("Un installing toolbox...");

        int noOfTry = 1;

        while (!unInstalled && noOfTry <= RETRY_COUNT) {
            Thread.sleep(1000);

            BAMToolboxDepolyerServiceStub.ToolBoxStatusDTO statusDTO = toolboxStub.getDeployedToolBoxes("1", "");
            String[] deployedTools = statusDTO.getDeployedTools();
            String[] undeployingTools = statusDTO.getToBeUndeployedTools();
            boolean isUninstalled = true;

            if (null != undeployingTools) {
                for (String aTool : undeployingTools) {
                    if (aTool.equalsIgnoreCase(toolBoxname)) {
                        isUninstalled = false;
                        break;
                    }
                }
            }

            if (null != deployedTools && isUninstalled) {
                for (String aTool : deployedTools) {
                    if (aTool.equalsIgnoreCase(toolBoxname)) {
                        isUninstalled = false;
                        break;
                    }
                }
            }
            unInstalled = isUninstalled;
            noOfTry++;
        }

        assertTrue(unInstalled, "Un installing toolbox" + deployedToolBox + " is not successful");
    }

    @AfterClass(groups = {"wso2.bam"})
    public void cleanUp() throws Exception {
        if (installed) undeployDefaultToolbox();
        if (null != connection) {
            connection.close();
        }
    }

    private String getToolBoxName() throws Exception {
        BAMToolboxDepolyerServiceStub.BasicToolBox[] toolBoxes = toolboxStub.getBasicToolBoxes();
        if (null == toolBoxes || toolBoxes.length == 0) {
            throw new Exception("No default toolboxes available..");
        }

        return toolBoxes[0].getTBoxFileName();
    }

}
