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

package org.sp.integration.tests.core.commons;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sp.integration.tests.core.FrameworkConstants;
import org.sp.integration.tests.core.beans.Deployment;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reader for test-deployment.yaml for deployment information
 */
@SuppressWarnings("ALL")
public class DeploymentConfigurationReader {

    private static final Log log = LogFactory.getLog(DeploymentConfigurationReader.class);
    private static DeploymentConfigurationReader deploymentConfigurationReader = null;
    private static HashMap<String, Deployment> deploymentHashMap;

    //Get the only object available
    private DeploymentConfigurationReader() {
    }

    public static DeploymentConfigurationReader readConfiguration() throws IOException {
        synchronized (DeploymentConfigurationReader.class) {
            if (deploymentConfigurationReader == null) {
                deploymentConfigurationReader = new DeploymentConfigurationReader();
                deploymentHashMap = readConfigurationYaml();
            }
        }
        return deploymentConfigurationReader;
    }

    /**
     * Methos to read configuration yaml file
     * @return
     * @throws IOException
     */
    private static HashMap<String, Deployment> readConfigurationYaml() throws IOException {
        Map<String, Object> map = getDeploymentObjectMap();
        HashMap<String, Deployment> deploymentHashMap = new HashMap<>();
        ArrayList<Object> deploymentList = (ArrayList<Object>) map.get(DeploymentYamlConstants.YAML_DEPLOYMENTS);
        HashMap<String, String> instanceList;
        for (Object deploymentObj : deploymentList) {

            Deployment deployment = new Deployment();
            deployment.setName(
                    ((LinkedHashMap) deploymentObj).get(DeploymentYamlConstants.YAML_DEPLOYMENT_NAME).toString());
            deployment.setDeployScripts(
                    ((LinkedHashMap) deploymentObj).get(DeploymentYamlConstants.YAML_DEPLOYMENT_SCRIPT).toString());
            deployment.setRepository(
                    ((LinkedHashMap) deploymentObj).get(DeploymentYamlConstants.YAML_DEPLOYMENT_REPO).toString());

            deployment.setSuite(
                    ((LinkedHashMap) deploymentObj).get(DeploymentYamlConstants.YAML_DEPLOYMENT_SUITE).toString());

            deployment.setUnDeployScripts(
                    ((LinkedHashMap) deploymentObj).get(DeploymentYamlConstants.YAML_UNDEPLOYMENT_SCRIPT).toString());

            deployment.setEnable(Boolean.parseBoolean(
                    ((LinkedHashMap) deploymentObj).get(DeploymentYamlConstants.YAML_UNDEPLOYMENT_SCRIPT).toString()));

            deployment.setFilePath(
                    ((LinkedHashMap) deploymentObj).get(DeploymentYamlConstants.YAML_DEPLOYMENT_URL_FILE_PATH)
                            .toString());
            instanceList = (HashMap<String, String>) ((ArrayList<Object>) ((LinkedHashMap) deploymentObj)
                    .get(DeploymentYamlConstants.YAML_DEPLOYMENT_INSTANCE_MAP)).get(0);
            deployment.setInstanceMap(instanceList);

            deploymentHashMap.put(deployment.getName(), deployment);
        }

        return deploymentHashMap;
    }

    /**
     * Method to get Map of deployment objects
     * @return
     * @throws IOException
     */
    private static Map<String, Object> getDeploymentObjectMap() throws IOException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(System.getProperty(FrameworkConstants.SYSTEM_ARTIFACT_RESOURCE_LOCATION)
                    + DeploymentYamlConstants.DEPLYMENT_YAML_FILE_NAME);
            Yaml yaml = new Yaml();
            return (Map<String, Object>) yaml.load(fis);
        } finally {

            if (fis != null) {
                fis.close();
            }
        }
    }

    /**
     * Method to get Information about a given deployment pattern
     * @param pattern
     * @return
     * @throws IOException
     */
    public HashMap getDeploymentInstanceMap(String pattern) throws IOException {
        return getDeploymentHashMap().get(pattern).getInstanceMap();
    }

    /**
     * Method to get Map of deployments
     * @return
     * @throws IOException
     */
    public HashMap<String, Deployment> getDeploymentHashMap() throws IOException {
        if (deploymentConfigurationReader == null) {
            readConfiguration();
        }
        return deploymentHashMap;
    }

}
