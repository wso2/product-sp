/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.das.integration.tests.servervalidationservice;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeSuite;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.das.integration.common.utils.DASIntegrationTest;

import java.io.File;

public class DASDebugLogChangeTestCase extends DASIntegrationTest {

    private static final Log log = LogFactory.getLog(DASDebugLogChangeTestCase.class);

    @BeforeSuite(alwaysRun = true)
    public void ChangeToLogDebug() throws Exception {
        super.init();
        ServerConfigurationManager serverManager = new ServerConfigurationManager(dasServer);
        String artifactsLocation = FrameworkPathUtil.getSystemResourceLocation() + File.separator + "config" +
                                   File.separator + "log4j.properties";
        String dataserviceConfigLocation =
                FrameworkPathUtil.getCarbonHome() + File.separator + "repository" + File.separator + "conf" + File
                        .separator + "log4j.properties";
        File sourceFile = new File(artifactsLocation);
        File targetFile = new File(dataserviceConfigLocation);
        serverManager.applyConfigurationWithoutRestart(sourceFile, targetFile, false);
        serverManager.restartGracefully();
        log.info("Replace log4j file with analytics debug enabled");
    }
}
