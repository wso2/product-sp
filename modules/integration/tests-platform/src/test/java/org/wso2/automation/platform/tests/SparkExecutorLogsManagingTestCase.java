/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.automation.platform.tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.das.integration.common.utils.DASIntegrationTest;

import java.io.File;
import java.util.Scanner;

/**
 * This Note: This test case is not run with default DAS integration tests. To run this test we assume
 * that DAS server is running in the clustering mode.
 */
public class SparkExecutorLogsManagingTestCase extends DASIntegrationTest {

    private ServerConfigurationManager serverManager;

    @Test(groups = "wso2.das.clsutering", description = "Testing the output log file")
    public void checkOutputLog() throws Exception {

        String artifactsLocation = FrameworkPathUtil.getSystemResourceLocation() + File.separator
                                   + "clustering" + File.separator + "logging" +
                                   File.separator + "log4j.properties";

        String dataserviceConfigLocation =
                FrameworkPathUtil.getCarbonHome() + File.separator + "repository" + File.separator + "conf" + File
                        .separator + "analytics" + File.separator + "spark" + File.separator + "log4j.properties";

        serverManager = new ServerConfigurationManager(dasServer);
        File sourceFile = new File(artifactsLocation);
        File targetFile = new File(dataserviceConfigLocation);
        serverManager.applyConfigurationWithoutRestart(sourceFile, targetFile, true);
        serverManager.restartGracefully();
        Thread.sleep(150000);

        File workDir = new File(FrameworkPathUtil.getCarbonHome() + File.separator + "work");
        File[] executorDirs = workDir.listFiles();
        assert executorDirs != null;
        File file = new File(executorDirs[executorDirs.length - 1].getAbsolutePath() + File.separator
                             + "sout");
        boolean result = true;

        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.contains("INFO  org.apache.spark")) {
                result = false;
                break;
            }
        }

        Assert.assertTrue(result, "Log output contains org.apache.spark INFO logs, hence " +
                                  "failing the test");

    }
}
