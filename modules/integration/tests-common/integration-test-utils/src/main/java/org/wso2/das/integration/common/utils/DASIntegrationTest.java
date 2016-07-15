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
package org.wso2.das.integration.common.utils;

import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;

public class DASIntegrationTest {

    protected AutomationContext dasServer;
    protected String backendURL;
    protected String webAppURL;
    protected LoginLogoutClient loginLogoutClient;
    protected User userInfo;

    protected void init() throws Exception {
        init(TestUserMode.SUPER_TENANT_ADMIN);
    }

    protected void init(TestUserMode testUserMode) throws Exception {
        dasServer = new AutomationContext("DAS", testUserMode);
        loginLogoutClient = new LoginLogoutClient(dasServer);
        backendURL = dasServer.getContextUrls().getBackEndUrl();
        webAppURL = dasServer.getContextUrls().getWebAppURL();
        userInfo = dasServer.getContextTenant().getContextUser();
    }

    protected void init(String domainKey, String userKey) throws Exception {
        dasServer = new AutomationContext("DAS", "das001", domainKey, userKey);
        loginLogoutClient = new LoginLogoutClient(dasServer);
        backendURL = dasServer.getContextUrls().getBackEndUrl();
        webAppURL = dasServer.getContextUrls().getWebAppURL();
    }

    protected void init(String domainKey, String instance, String userKey) throws Exception {
        dasServer = new AutomationContext("DAS", instance, domainKey, userKey);
        loginLogoutClient = new LoginLogoutClient(dasServer);
        backendURL = dasServer.getContextUrls().getBackEndUrl();
        webAppURL = dasServer.getContextUrls().getWebAppURL();
    }

    protected String getSessionCookie() throws Exception {
        return loginLogoutClient.login();
    }

    @SuppressWarnings("rawtypes")
    protected String getResourceContent(Class testClass, String resourcePath) throws Exception {
        String content = "";
        URL url = testClass.getClassLoader().getResource(resourcePath);
        BufferedReader bufferedReader = null;
        if (url != null) {
            try {
                bufferedReader = new BufferedReader(new FileReader(
                        new File(url.toURI()).getAbsolutePath()));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    content += line;
                }
                return content;
            } finally {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            }
        } else {
            throw new Exception("No resource found in the given path : "+ resourcePath);
        }
    }
    
}

