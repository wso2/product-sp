package org.wso2.bam.integration.common.utils;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;

public class BAMIntegrationTest {
    
    private static final Log log = LogFactory.getLog(BAMIntegrationTest.class);
    protected AutomationContext bamServer;
    protected String sessionCookie;
    protected String backendURL;
    protected String webAppURL;
    protected LoginLogoutClient loginLogoutClient;
    protected User userInfo;

    protected void init() throws Exception {
        init(TestUserMode.SUPER_TENANT_ADMIN);
    }

    protected void init(TestUserMode testUserMode) throws Exception {
        bamServer = new AutomationContext("BAM", testUserMode);
        loginLogoutClient = new LoginLogoutClient(bamServer);
        backendURL = bamServer.getContextUrls().getBackEndUrl();
        webAppURL = bamServer.getContextUrls().getWebAppURL();
        userInfo = bamServer.getContextTenant().getContextUser();
    }

    protected void init(String domainKey, String userKey) throws Exception {
        bamServer = new AutomationContext("BAM", "bam001", domainKey, userKey);
        sessionCookie = loginLogoutClient.login();
        backendURL = bamServer.getContextUrls().getBackEndUrl();
        webAppURL = bamServer.getContextUrls().getWebAppURL();
    }

    protected String getSessionCookie() throws Exception {
        return loginLogoutClient.login();
    }
}
