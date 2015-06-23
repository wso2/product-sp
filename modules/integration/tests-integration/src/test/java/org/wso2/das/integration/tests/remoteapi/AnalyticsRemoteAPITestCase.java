/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.das.integration.tests.remoteapi;

import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.das.integration.common.utils.DASIntegrationTest;

import java.io.File;

public class AnalyticsRemoteAPITestCase extends DASIntegrationTest {

    @BeforeGroups(groups = {"wso2.das"}, alwaysRun = true)
    public void startChangeAnalyticsAPIXml() throws Exception {
        super.init();
        String remoteAPIConfResource = FrameworkPathUtil.getSystemResourceLocation() +
                "dasconfig" + File.separator + "api" + File.separator + "analytics-remote-data-conf.xml";
        String analyticsAPIConf = FrameworkPathUtil.getCarbonHome() + File.separator + "repository"
                + File.separator + "conf" + File.separator + "analytics" + File.separator;
        FileManager.copyResourceToFileSystem(remoteAPIConfResource, analyticsAPIConf, "analytics-data-config.xml");
        ServerConfigurationManager serverConfigurationManager =
                new ServerConfigurationManager(dasServer);
        serverConfigurationManager.restartGracefully();
    }

    @Test(groups = "wso2.das", description = "Login to server")
    public void login() throws Exception {
        getSessionCookie();
    }
}
