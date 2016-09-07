/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.das.integration.tests.sso;

import com.sun.mail.util.BASE64DecoderStream;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.das.integration.common.utils.DASIntegrationTest;
import org.wso2.das.integration.common.utils.TestConstants;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class SSOAuthenticationLoginTest extends DASIntegrationTest {
    private AuthenticatorClient authClient;
    private static final String SSO = "sso";
    private static final String POST = "POST";
    private static final String SAML_REQUEST = "SAMLRequest";
    private static final String VALUE = "value";
    private static final String NAME = "name";
    private static final String INPUT = "input";
    private static final String SIGNATURE = "ds:Signature";
    private static final String UTF_8 = "utf-8";

    @Test(groups = "wso2.das", description = "Login to server with SSO - RequestSigning Enable") public void testSSOLoginAuthRequest()
            throws Exception {
        registerPortalApplication();
        setLoginMethod(SSO);
        URL url = new URL(TestConstants.ANALYTICS_PORTAL_ENDPOINT);
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        httpCon.setDoOutput(true);
        httpCon.setRequestMethod(POST);

        BufferedReader in = new BufferedReader(new InputStreamReader(httpCon.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }

        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputSource is = new InputSource();
        String s = response.substring(0, response.indexOf("<script"));
        is.setCharacterStream(new StringReader(s));
        Document doc = db.parse(is);
        String SAML = null;
        NodeList nodeList = doc.getElementsByTagName(INPUT);
        for (int x = 0, size = nodeList.getLength(); x < size; x++) {
            if (nodeList.item(x).getAttributes().getNamedItem(NAME).getNodeValue().equalsIgnoreCase(SAML_REQUEST)) {
                SAML = nodeList.item(x).getAttributes().getNamedItem(VALUE).getNodeValue();
            }
        }
        SAML = new String(BASE64DecoderStream.decode(SAML.getBytes()));
        doc = db.parse(new InputSource(new ByteArrayInputStream(SAML.getBytes(UTF_8))));
        nodeList = doc.getElementsByTagName(SIGNATURE);
        Assert.assertEquals(nodeList.getLength(), 1);
        in.close();
    }

    /**
     * Register portal as a service provider, by using configuration files
     *
     * @throws Exception
     */
    private void registerPortalApplication() throws Exception {
        String carbonHome = FrameworkPathUtil.getCarbonHome();
        String systemResourceLocation = FrameworkPathUtil.getSystemResourceLocation();
        String pathToSSOIdpConfig = systemResourceLocation + "identity" + File.separator + "sso-idp-config.xml";
        String targetSSOIdpConfig =
                carbonHome + File.separator + "repository" + File.separator + "conf" + File.separator +
                        "identity" + File.separator + "sso-idp-config.xml";
        String pathToPortal = systemResourceLocation + "identity" + File.separator + "service-providers" +
                File.separator + "portal.xml";

        String serviceProviderDirPath =
                carbonHome + File.separator + "repository" + File.separator + "conf" + File.separator +
                        "identity" + File.separator + "service-providers";

        String targetToPortal = serviceProviderDirPath + File.separator + "portal.xml";

        File serviceProviderDir = new File(serviceProviderDirPath);
        if (!serviceProviderDir.exists()) {
            serviceProviderDir.mkdirs();
        }
        AutomationContext automationContext = new AutomationContext("DAS", TestUserMode.SUPER_TENANT_USER);
        ServerConfigurationManager serverConfigurationManager = new ServerConfigurationManager(automationContext);
        // copy files
        serverConfigurationManager
                .applyConfigurationWithoutRestart(new File(pathToSSOIdpConfig), new File(targetSSOIdpConfig), false);
        serverConfigurationManager
                .applyConfigurationWithoutRestart(new File(pathToPortal), new File(targetToPortal), false);
        // restart the server to activate configuration files
        serverConfigurationManager.restartGracefully();
    }

    public void setLoginMethod(String method) throws Exception {
        PrintWriter pw = null;
        if ((method == null) || !(method.toLowerCase().equals("basic") || method.toLowerCase().equals("sso"))) {
            method = "basic";
        }
        try {
            String designerFilePath =
                    FrameworkPathUtil.getCarbonHome() + File.separator + "repository" + File.separator + "deployment"
                            + File.separator + "server" + File.separator + "jaggeryapps" + File.separator + "portal" +
                            File.separator + "configs" + File.separator + "designer.json";
            File f = new File(designerFilePath);
            BufferedReader br = new BufferedReader(new FileReader(f));
            StringBuilder sb = new StringBuilder();
            JSONObject designerJson;
            while (br.ready()) {
                sb.append(br.readLine());
            }
            br.close();
            // convert json string to json object
            designerJson = new JSONObject(sb.toString());
            // set active method
            designerJson.getJSONObject("authentication").put("activeMethod", method);
            designerJson.getJSONObject("authentication").getJSONObject("methods").getJSONObject("sso")
                    .getJSONObject("attributes").put("responseSigningEnabled", true);
            pw = new PrintWriter(f);
            pw.println(designerJson.toString());
            pw.flush();
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }
}
