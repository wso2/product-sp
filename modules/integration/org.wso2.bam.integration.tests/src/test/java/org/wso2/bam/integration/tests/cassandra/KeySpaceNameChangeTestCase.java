package org.wso2.bam.integration.tests.cassandra;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import org.wso2.bam.integration.tests.agents.KPIAgent;
import org.wso2.bam.integration.tests.hive.BAMJDBCHandlerTestCase;
import org.wso2.carbon.analytics.hive.stub.HiveExecutionServiceStub;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.server.admin.ui.ServerAdminClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.FileChannel;

import static junit.framework.Assert.fail;
import static org.testng.AssertJUnit.assertTrue;

/**
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
public class KeySpaceNameChangeTestCase {

    private static final Log log = LogFactory.getLog(KeySpaceNameChangeTestCase.class);
    private static final String HIVE_SERVICE = "/services/HiveExecutionService";
    private HiveExecutionServiceStub hiveStub;
    private LoginLogoutUtil util = new LoginLogoutUtil();
    private static final long TIMEOUT = 5 * 60000;


    @Test(groups = {"wso2.bam"})
    public void testKeySpaceNameChange() throws java.lang.Exception {

        log.info("Testing testKeySpaceNameChange starting ...");

        File source = new File(BAMJDBCHandlerTestCase.class.getClassLoader().getResource("data-bridge-config-test.xml").toURI());
        File dest = new File(System.getProperty("carbon.home") + "/repository/conf/data-bridge/data-bridge-config.xml");

        log.info("Starting to move data-bridge-config-test.xml ...");
        copyFile(source.getAbsoluteFile(), dest.getAbsoluteFile());

        log.info("Move data-bridge-config-test.xml file completed.");

        ServerAdminClient serverAdmin = LoginLogoutUtil.getServerAdminClient(0);
        assertTrue(serverAdmin.restartGracefully());
        Thread.sleep(5000);
        ClientConnectionUtil.waitForPort(9443, TIMEOUT, true);
        Thread.sleep(5000);
        initializeHiveStub();

        KPIAgent.publish();

        String[] queries = getHiveQueries("KeySpaceNameSampleScript");

        if (queries != null) {
            hiveStub.executeHiveScript(null, queries[0].trim());

            HiveExecutionServiceStub.QueryResult[] results = hiveStub.executeHiveScript(null, queries[1].trim());

            if (results == null || results.length == 0) {
                fail("No results returned..");
            }

            for (HiveExecutionServiceStub.QueryResult result : results) {
                if (result.getColumnNames().length != 1) {
                    fail("Unexpected result returned. Expected 1 columns in the results. Only " +
                         result.getColumnNames().length + " columns returned.");
                }
                break;
            }
        }

        log.info("Testing testKeySpaceNameChange finished.");
    }


    private static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            long count = 0;
            long size = source.size();
            while ((count += destination.transferFrom(source, count, size - count)) < size) {
            }
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    private void initializeHiveStub() throws Exception {
        String loggedInSessionCookie = util.login();
        ConfigurationContext configContext = ConfigurationContextFactory.
                createConfigurationContextFromFileSystem(null);
        String EPR = "https://" + FrameworkSettings.HOST_NAME +
                     ":" + FrameworkSettings.HTTPS_PORT + HIVE_SERVICE;
        hiveStub = new HiveExecutionServiceStub(configContext, EPR);
        ServiceClient client = hiveStub._getServiceClient();
        Options option = client.getOptions();
        option.setTimeOutInMilliSeconds(10 * 60000);
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                           loggedInSessionCookie);
    }

    private String[] getHiveQueries(String resourceName) {
        String[] queries = null;

        URL url = BAMJDBCHandlerTestCase.class.getClassLoader().getResource(resourceName);

        if (url != null) {
            BufferedReader bufferedReader = null;
            FileReader fileReader = null;
            File file;
            try {
                file = new File(url.toURI());
                fileReader = new FileReader(file.getAbsoluteFile());
                bufferedReader = new BufferedReader(fileReader);
                String script = "";
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    script += line;
                }
                queries = script.split(";");
            } catch (Exception e) {
                fail("Error while reading resource : " + resourceName);
                log.error("Error while reading resource : " + resourceName, e);
            } finally {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                    }
                }

                if (fileReader != null) {
                    try {
                        fileReader.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        return queries;
    }

    @AfterClass(groups = {"wso2.bam"})
    public void shutdown() throws java.lang.Exception {
        File source = new File(BAMJDBCHandlerTestCase.class.getClassLoader().getResource("data-bridge-config.xml").toURI());
        File dest = new File(System.getProperty("carbon.home") + "/repository/conf/data-bridge/data-bridge-config.xml");

        log.info("Starting to move data-bridge-config.xml ...");
        copyFile(source.getAbsoluteFile(), dest.getAbsoluteFile());

        log.info("Move data-bridge-config.xml file completed.");

        ServerAdminClient serverAdmin = LoginLogoutUtil.getServerAdminClient(0);
        assertTrue(serverAdmin.restartGracefully());
        Thread.sleep(5000);
        ClientConnectionUtil.waitForPort(9443, TIMEOUT, true);
        Thread.sleep(5000);
    }

}
