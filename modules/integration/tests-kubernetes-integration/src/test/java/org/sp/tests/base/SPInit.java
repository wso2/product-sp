/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.sp.tests.base;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sp.integration.tests.core.beans.InstanceUrls;
import org.sp.integration.tests.core.beans.Port;
import org.sp.integration.tests.core.commons.DeploymentConfigurationReader;
import org.sp.integration.tests.core.commons.DeploymentDataReader;
import org.sp.integration.tests.core.utills.ScriptExecutorUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * SPInit class
 */
public class SPInit {

    private static final Log log = LogFactory.getLog(SPInit.class);
    public static String spURL;
    public static String msf4jURL;
    public static String haNodeOneMsf4jURL;
    public static String haNodeTwoMsf4jURL;
    public static String mysqlURL;
    public static String haNodeOneURL;
    public static String haNodeTwoURL;

    protected HashMap<String, String> instanceMap;

    /**
     * This method will initialize test environment
     * based on the configuration given at testng.xml
     */
    protected void init(String pattern) throws Exception {
        setURLs(pattern);
    }

    //set the url's as specified in the deployment.json
    protected void setURLs(String patternName) {

        HashMap<String, String> instanceMap = null;
        try {
            DeploymentConfigurationReader depconf = DeploymentConfigurationReader.readConfiguration();
            instanceMap = depconf.getDeploymentInstanceMap(patternName);
        } catch (IOException e) {
            log.error("Exception occured while getting the deployment instance map : " + e.getMessage(), e);
        }
        DeploymentDataReader dataJsonReader = new DeploymentDataReader();
        List<InstanceUrls> urlList = dataJsonReader.getInstanceUrlsList();
        for (InstanceUrls url : urlList) {
            if (instanceMap != null) {
                if (url.getLabel().equals(instanceMap.get(SPConstants.POD_TAG_NAME))) {
                    spURL = getHTTPSUrl(SPConstants.SP_PORT_NAME, url.getHostIP(), url.getPorts(), "");
                    msf4jURL = getHTTPSUrl(SPConstants.MSF4J_PORT_NAME, url.getHostIP(), url.getPorts(), "");
                } else if (url.getLabel().equals(instanceMap.get(SPConstants.HA_NODE_1_POD_NAME))) {
                    haNodeOneURL = getHTTPSUrl(SPConstants.SP_PORT_NAME, url.getHostIP(), url.getPorts(), "");
                    haNodeOneMsf4jURL = getHTTPSUrl(SPConstants.MSF4J_PORT_NAME, url.getHostIP(), url.getPorts(), "");
                } else if (url.getLabel().equals(instanceMap.get(SPConstants.HA_NODE_2_POD_NAME))) {
                    haNodeTwoURL = getHTTPSUrl(SPConstants.SP_PORT_NAME, url.getHostIP(), url.getPorts(), "");
                    haNodeTwoMsf4jURL = getHTTPSUrl(SPConstants.MSF4J_PORT_NAME, url.getHostIP(), url.getPorts(), "");
                } else if (url.getLabel().equals(instanceMap.get(SPConstants.MYSQL_POD_NAME))) {
                    mysqlURL = getHTTPSUrl(SPConstants.MYSQL_PORT_NAME, url.getHostIP(), url.getPorts(), "");
                }
            }
        }
    }

    protected String getHTTPSUrl(String protocol, String hostIP, List<Port> ports, String context) {

        String url = "http://" + hostIP + ":";
        for (Port port : ports) {
            if (port.getProtocol().equals(protocol)) {
                url = url + port.getPort() + context;
                break;
            }
        }
        return url;
    }

    protected String getJDBCUrl(String protocol, String hostIP, List<Port> ports, String databasename) {

        // 192.168.48.44:30306/DAS_DB
        String url = hostIP + ":";
        for (Port port : ports) {
            if (port.getProtocol().equals(protocol)) {
                url = url + port.getPort() + databasename;
                break;
            }
        }
        return url;
    }

    private boolean isURLRemapEnabled() {
        log.info("URL Remap Enabled is set to : " + System.getenv(SPConstants.ENABLE_URL_REMAP));
        return Boolean.parseBoolean((System.getenv(SPConstants.ENABLE_URL_REMAP)));
    }

    private String getRemappedURL(String localIP) {

        String remappedURL = System.getenv("IP_" + localIP.replace(".", "_"));

        if (remappedURL.equals("") | remappedURL == null) {
            log.info("No remap value found for the Local IP : " + localIP);
        }
        return remappedURL;
    }

    //deploy environment
    protected void setTestSuite(String pattern) throws IOException {
        ScriptExecutorUtil.deployScenario(pattern);
    }

    //Undeploy environment
    protected void unSetTestSuite(String pattern) throws Exception {
        ScriptExecutorUtil.unDeployScenario(pattern);
    }

    //Run a script
    public void runBashScript(String patternName, String scriptName) throws IOException {
        ScriptExecutorUtil.execute(patternName, scriptName);
    }
}
