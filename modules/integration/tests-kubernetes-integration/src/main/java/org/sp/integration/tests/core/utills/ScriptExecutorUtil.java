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

package org.sp.integration.tests.core.utills;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sp.integration.tests.core.FrameworkConstants;
import org.sp.integration.tests.core.beans.Deployment;
import org.sp.integration.tests.core.commons.DeploymentConfigurationReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;

/**
 * This class serves as the automation script runner and related operations
 */
public class ScriptExecutorUtil {

    private static final Log log = LogFactory.getLog(ScriptExecutorUtil.class);
    public static final String DEFAULT_PATH = "default";

    private static void processOutputGenerator(String[] command, String filePath) {

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        BufferedReader br = null;
        try {
            Process process = processBuilder.start();
            br = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            String line;

            log.info(" Listing Run Output .... " + Arrays.toString(command) + " is: ");

            while ((line = br.readLine()) != null) {
                log.info(line);
            }
        } catch (IOException e) {
            log.error("Error while streaming process output : " + e.getMessage(), e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }

            }
        }
        if (!((DEFAULT_PATH).equals(filePath))) {
            File f = new File(filePath);
            if (f.exists() && !f.isDirectory()) {
                System.setProperty(FrameworkConstants.JSON_FILE_PATH, filePath);
            }
        }
    }

    private static void processOutputGenerator(String[] command) {
        processOutputGenerator(command, DEFAULT_PATH);
    }

    public static void deployScenario(String scenario) throws IOException {
        String resourceLocation = System.getProperty(FrameworkConstants.SYSTEM_ARTIFACT_RESOURCE_LOCATION);
        HashMap<String, Deployment> deploymentHashMap = DeploymentConfigurationReader.readConfiguration()
                .getDeploymentHashMap();
        Deployment deployment = deploymentHashMap.get(scenario);
        String scriptLocation = resourceLocation + "artifacts" + File.separator + deployment.getName();
        String[] cmdArray = deployment.getDeployScripts().split(",");
        for (String cmd : cmdArray) {
            String[] command = new String[]{"/bin/bash", scriptLocation + File.separator + cmd};
            processOutputGenerator(command, deployment.getFilePath());
        }
    }

    public static void unDeployScenario(String scenario) throws IOException {
        String resourceLocation = System.getProperty(FrameworkConstants.SYSTEM_ARTIFACT_RESOURCE_LOCATION);
        HashMap<String, Deployment> deploymentHashMap = DeploymentConfigurationReader.readConfiguration()
                .getDeploymentHashMap();
        Deployment deployment = deploymentHashMap.get(scenario);
        String scriptLocation = resourceLocation + "artifacts" + File.separator + deployment.getName();
        String[] cmdArray = deployment.getUnDeployScripts().split(",");
        for (String cmd : cmdArray) {
            String[] command = new String[]{"/bin/bash", scriptLocation + File.separator + cmd};
            processOutputGenerator(command, deployment.getFilePath());
        }
    }

    public static void execute(String patternName, String scriptName) throws IOException {
        String resourceLocation = System.getProperty(FrameworkConstants.SYSTEM_ARTIFACT_RESOURCE_LOCATION);
        String scriptLocation = resourceLocation + "artifacts" + File.separator + patternName;
        String[] command = new String[]{"/bin/bash", scriptLocation + File.separator + scriptName};
        processOutputGenerator(command);
    }
}

