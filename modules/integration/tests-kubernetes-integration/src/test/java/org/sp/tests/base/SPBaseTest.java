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
package org.sp.tests.base;

import org.apache.log4j.Logger;
import org.testng.ITestContext;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

/**
 * SPBaseTest class
 */
public class SPBaseTest extends SPInit {

    private static final Logger log = Logger.getLogger(SPBaseTest.class);

    /**
     * Base test class for all Stream Processor test cases.
     */
    @BeforeSuite(alwaysRun = true)
    public void createEnvironment(ITestContext ctx) throws Exception {
        log.info("Creating environment for suite " + ctx.getSuite().getName());
        super.setTestSuite(ctx.getCurrentXmlTest().getSuite().getName());
        super.init(ctx.getCurrentXmlTest().getSuite().getName());
    }

    @BeforeClass(alwaysRun = true)
    public void init(ITestContext ctx) throws Exception {

    }

    @AfterSuite(alwaysRun = true)
    public void deleteEnvironment(ITestContext ctx) throws Exception {
        log.info("Deleting environment for suite " + ctx.getSuite().getName());
        super.unSetTestSuite(ctx.getCurrentXmlTest().getSuite().getName());
        Thread.sleep(5000); // Wait for environment to delete before moving to next test suite
    }
}
