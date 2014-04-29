/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.bam.integration.tests.hive;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;
import org.wso2.bam.integration.tests.agents.KPIAgent;
import org.wso2.carbon.analytics.hive.stub.HiveExecutionServiceHiveExecutionException;
import org.wso2.carbon.analytics.hive.stub.HiveExecutionServiceStub;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.NoStreamDefinitionExistException;
import org.wso2.carbon.databridge.commons.exception.StreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.rmi.RemoteException;

import static junit.framework.Assert.fail;

public class HiveScriptRegistryValueTestCase {

    private static final Log log = LogFactory.getLog(HiveScriptRegistryValueTestCase.class);

    private static final String HIVE_SERVICE = "/services/HiveExecutionService";
    private static final String REGISTRY_SERVICE = "/services/ResourceAdminService";

    private LoginLogoutUtil util = new LoginLogoutUtil();
    private HiveExecutionServiceStub hiveStub;
    private ResourceAdminServiceStub registryStub;


    @Test(groups = {"wso2.bam"})
    public void runKPIAgent() throws AgentException, MalformedURLException, AuthenticationException,
                                     MalformedStreamDefinitionException, SocketException,
                                     StreamDefinitionException, TransportException,
                                     NoStreamDefinitionExistException,
                                     DifferentStreamDefinitionAlreadyDefinedException {
        KPIAgent.publish();
    }

    @Test(groups = {"wso2.bam"}, dependsOnMethods = "runKPIAgent")
    public void executeScript() {

        try {
            String loggedInSessionCookie = util.login();
            ConfigurationContext configContext = ConfigurationContextFactory.
                    createConfigurationContextFromFileSystem(null);

            initializeHiveStub(loggedInSessionCookie, configContext);
            initializedRegistryStub(loggedInSessionCookie, configContext);

            settingUpRegistryValues();

            String[] queries = getHiveQueries("HiveRegistryValueSampleScript");

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
        } catch (HiveExecutionServiceHiveExecutionException e) {
            fail("Failed while executing hive script " + e.getMessage());
            log.error("Failed while executing hive script " + e.getMessage(), e);
        } catch (RemoteException e) {
            fail("Remote Exception: " + e.getMessage());
            log.error("Remote Exception: " + e.getMessage(), e);
        } catch (ResourceAdminServiceExceptionException e) {
            fail("Failed while setting up registry values: " + e.getMessage());
            log.error("Failed while setting up registry values: " + e.getMessage(), e);
        } catch (Exception e) {
            fail("Error: " + e.getMessage());
            log.error("Error: " + e.getMessage(), e);
        }
    }

    private void settingUpRegistryValues()
            throws RemoteException, ResourceAdminServiceExceptionException {
        registryStub.addTextResource("/_system/local/event/text", "Event", "text/plain", "sample", "EVENT_KS");
        registryStub.addTextResource("/_system/config/username/text", "Username", "text/plain", "sample", "admin");
        registryStub.addTextResource("/_system/governance/password/text", "Password", "text/plain", "sample", "admin");
    }

    private void initializeHiveStub(String loggedInSessionCookie,
                                    ConfigurationContext configContext) throws Exception {

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

    private void initializedRegistryStub(String loggedInSessionCookie,
                                         ConfigurationContext configContext) throws Exception {

        String EPR = "https://" + FrameworkSettings.HOST_NAME +
                     ":" + FrameworkSettings.HTTPS_PORT + REGISTRY_SERVICE;
        registryStub = new ResourceAdminServiceStub(configContext, EPR);
        ServiceClient client = registryStub._getServiceClient();
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

}
