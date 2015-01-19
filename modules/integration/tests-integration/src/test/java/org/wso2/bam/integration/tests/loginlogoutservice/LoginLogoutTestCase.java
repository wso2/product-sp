package org.wso2.bam.integration.tests.loginlogoutservice;

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

import org.wso2.bam.integration.common.utils.BAMIntegrationTest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;

public class LoginLogoutTestCase extends BAMIntegrationTest {

    private static final Log log = LogFactory.getLog(LoginLogoutTestCase.class);
    private AuthenticatorClient authClient;

    @BeforeClass(alwaysRun = true)
    public void loginInit() throws Exception {
        super.init();
    }

    @Test(groups = "wso2.bam", description = "Login to server")
    public void login() throws Exception {
        authClient = new AuthenticatorClient(backendURL);
        String loginStatus =
                authClient.login(bamServer.getSuperTenant().getTenantAdmin().getUserName(),
                                 bamServer.getSuperTenant().getTenantAdmin().getPassword(),
                                 bamServer.getInstance().getHosts().get("default"));
        log.info("Login status " + loginStatus);
    }

    @Test(groups = "wso2.bam", description = "Logout from server", dependsOnMethods = "login")
    public void logout() throws Exception {
        authClient.logOut();
        log.info("Logged out");
    }
}
