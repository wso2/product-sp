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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static junit.framework.Assert.fail;

public class HiveTempFileCleanupTestCase {

    private static final Log log = LogFactory.getLog(HiveTempFileCleanupTestCase.class);

    private HiveExecutionServiceStub hiveStub;
    private LoginLogoutUtil util = new LoginLogoutUtil();
    private static final String HIVE_SERVICE = "/services/HiveExecutionService";

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

        String[] queries = getHiveQueries("KPISampleScript");

        try {
            for (String query : queries) {
                hiveStub.executeHiveScript(null, query);
            }
        } catch (HiveExecutionServiceHiveExecutionException e) {
            fail("Failed while excecuting hive script " + e.getMessage());
        } catch (Exception e){
            fail("Error when trying to run hive script: "+ e.getMessage());
        }

    }

    @Test(groups = {"wso2.bam"}, dependsOnMethods = "executeScript")
    public void validateTmpFileCleanup() {

        String[] tmpDirectories = {
                FrameworkSettings.CARBON_HOME + "/tmp/" + System.getProperty("user.name"),
                FrameworkSettings.CARBON_HOME + "/tmp/hadoop/local",
                FrameworkSettings.CARBON_HOME + "/tmp/hadoop/staging",
                FrameworkSettings.CARBON_HOME + "/tmp/hive/" + System.getProperty("user.name")+ "-querylogs"};

        for (String tmpDirectory : tmpDirectories) {
            File tmpDirectoryFile = new File(tmpDirectory);
            String[] subDirectories = tmpDirectoryFile.list();
            if (subDirectories != null && subDirectories.length > 0) {
                fail("Error cleaning the temporary file location after Hive script execution at : "
                     + tmpDirectory);
            }
        }

    }

    private void initializeHiveStub() throws Exception {
        ConfigurationContext configContext = ConfigurationContextFactory.
                createConfigurationContextFromFileSystem(null);

        String loggedInSessionCookie = util.login();

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
        try {
            initializeHiveStub();
        } catch (Exception e) {
            fail("Error while initializing hive stub: " + e.getMessage());
        }

        URL url = BAMJDBCHandlerTestCase.class.getClassLoader().getResource(resourceName);

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(
                    new File(url.toURI()).getAbsolutePath()));
            String script = "";
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                script += line;
            }
            //queries = script.split(";");
            queries = this.extractHiveQueries(script);
        } catch (Exception e) {
            fail("Error while reading resource : " + resourceName);
        }
        return queries;
    }
    
    private String[] extractHiveQueries(String hiveString) {
        String[] outputQueries;
        if (hiveString != null && !hiveString.equals("")) {
            Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
            Matcher regexMatcher = regex.matcher(hiveString);
            String formattedScript = "";
            while (regexMatcher.find()) {
                String temp = "";
                if (regexMatcher.group(1) != null) {
                    // Add double-quoted string without the quotes
                    temp = regexMatcher.group(1).trim().replaceAll(";", "%%");
                    temp = "\"" + temp + "\"";
                } else if (regexMatcher.group(2) != null) {
                    // Add single-quoted string without the quotes
                    temp = regexMatcher.group(2).trim().replaceAll(";", "%%");
                    temp = "\'" + temp + "\'";
                } else {
                    temp = regexMatcher.group().trim();
                }
                formattedScript += temp + " ";
            }
            String[] queries = formattedScript.split(";");
            outputQueries = new String[queries.length];
            int a = 0;
            hiveString = "";
            for (String aquery : queries) {
                aquery = aquery.trim();
                if (!aquery.equals("")) {
                    aquery = aquery.replaceAll("%%\n", ";");
                    aquery = aquery.replaceAll("%% ", ";");
                    aquery = aquery.replaceAll(" %% ", ";");
                    aquery = aquery.replaceAll("%%", ";");
                    //hiveString = hiveString + aquery + ";" + "\n";
                    outputQueries[a] = aquery;
                    a++;
                }
            }
            return outputQueries;
        }
        return new String[0];
    }

}
