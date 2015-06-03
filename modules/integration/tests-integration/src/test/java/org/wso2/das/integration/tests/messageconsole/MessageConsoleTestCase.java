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

package org.wso2.das.integration.tests.messageconsole;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.das.integration.common.clients.MessageConsoleClient;
import org.wso2.das.integration.common.utils.DASIntegrationTest;

public class MessageConsoleTestCase extends DASIntegrationTest {

    private MessageConsoleClient messageConsoleClient;
//    public Map<String, String> startupParameterMap1 = new HashMap<String, String>();
//    private CarbonTestServerManager server1;

    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        /*super.init("superTenant", "bam002", "admin");
        startupParameterMap1.put("-DportOffset", "20");
        server1 = new CarbonTestServerManager(bamServer, System.getProperty("carbon.zip"), (HashMap<String, String>) startupParameterMap1);
        server1.startServer();
        String backendURL = super.backendURL.replace("9443", "9463");
        String session = new AuthenticatorClient(backendURL).
                login(bamServer.getContextTenant().getContextUser().getUserName(),
                      bamServer.getContextTenant().getContextUser().getPassword(),
                      bamServer.getInstance().getHosts().get("default"));
        messageConsoleClient = new MessageConsoleClient("https://localhost:9463/services/", session);*/

        super.init();
        String session = getSessionCookie();
        messageConsoleClient = new MessageConsoleClient(backendURL, session);
    }

    @AfterClass(alwaysRun = true)
    protected void cleanup() throws Exception {
        /*if (server1 != null) {
            server1.stopServer();
        }*/
    }

    @Test(groups = "wso2.das", description = "Adding script with task")
    public void scheduleTask() throws Exception {
//        messageConsoleClient.scheduleDataPurgingTask();
    }
}
