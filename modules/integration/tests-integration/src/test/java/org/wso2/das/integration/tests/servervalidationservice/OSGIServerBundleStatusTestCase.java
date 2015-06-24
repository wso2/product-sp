package org.wso2.das.integration.tests.servervalidationservice;

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
import org.testng.annotations.BeforeClass;
import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;
import org.wso2.carbon.integration.common.tests.OSGIServerBundleStatusTest;

import javax.xml.xpath.XPathExpressionException;

public class OSGIServerBundleStatusTestCase extends OSGIServerBundleStatusTest {

    private static final Log log = LogFactory.getLog(OSGIServerBundleStatusTestCase.class);

    @BeforeClass
    @Override
    public void init() throws XPathExpressionException, AutomationFrameworkException {
        log.info("Starting OSGIServerBundleStatusTestCase .......");
        super.init();
    }
}
