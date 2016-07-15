/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.das.integration.tests.analytics.execution;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.spark.admin.stub.AnalyticsProcessorAdminServiceStub;
import org.wso2.das.integration.common.utils.DASIntegrationTest;

import java.io.File;

public class CarbonJDBCTestCase extends DASIntegrationTest {

    private static final Log log = LogFactory.getLog(CarbonJDBCTestCase.class);
    private static final String ANALYTICS_SERVICE_NAME = "AnalyticsProcessorAdminService";
    private static final String SCRIPT_RESOURCE_DIR = "analytics" + File.separator + "scripts";
    private AnalyticsProcessorAdminServiceStub analyticsStub;

    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        super.init();
        String apiConf = new File(this.getClass().getClassLoader().getResource("dasconfig" + File.separator + "api"
                + File.separator + "analytics-data-config.xml").toURI()).getAbsolutePath();
        initializeStub();
    }

    @Test(groups = "wso2.bam", description = "Create table test")
    public void initTableTest() throws Exception {
        String query = getResourceContent(CarbonJDBCTestCase.class, getAnalyticsScriptResourcePath("CarbonJDBCScript.ql"));
        AnalyticsProcessorAdminServiceStub.AnalyticsQueryResultDto[] resultArr = this.analyticsStub.execute(query);
        AnalyticsProcessorAdminServiceStub.AnalyticsQueryResultDto result = resultArr[resultArr.length - 1];
        AnalyticsProcessorAdminServiceStub.AnalyticsRowResultDto[] rows = result.getRowsResults();
        Assert.assertEquals(rows.length, 10);
    }

    @Test(groups = "wso2.bam", description = "Testing INSERT INTO", dependsOnMethods = "initTableTest")
    public void insertIntoTest() throws Exception {
        String state = "Oregon";
        String polarity = "11";
        String value = "1683.08";
        String query1 = "insert into table test1 select \"" + state + "\", " + polarity + ", " + value;
        this.analyticsStub.execute(query1);
        String query2 = "select * from test1";
        AnalyticsProcessorAdminServiceStub.AnalyticsQueryResultDto[] resultArr = this.analyticsStub.execute(query2);
        AnalyticsProcessorAdminServiceStub.AnalyticsQueryResultDto result = resultArr[resultArr.length - 1];
        AnalyticsProcessorAdminServiceStub.AnalyticsRowResultDto[] rows = result.getRowsResults();
        for (AnalyticsProcessorAdminServiceStub.AnalyticsRowResultDto row : rows) {
            if (state.equals(row.getColumnValues()[0])) {
                Assert.assertEquals(row.getColumnValues()[1], polarity);
                Assert.assertEquals(row.getColumnValues()[2], value);
            }
        }
    }

    @Test(groups = "wso2.bam", description = "Testing INSERT OVERWRITE", dependsOnMethods = "initTableTest")
    public void insertOverwriteTest() throws Exception {
        String state = "New Orleans";
        String polarity = "7";
        String value = "211.67";
        String query1 = "insert overwrite table test1 select \"" + state + "\", " + polarity + ", " + value + ";";
        this.analyticsStub.execute(query1);
        String query2 = "select * from test1";
        AnalyticsProcessorAdminServiceStub.AnalyticsQueryResultDto[] resultArr = this.analyticsStub.execute(query2);
        AnalyticsProcessorAdminServiceStub.AnalyticsQueryResultDto result = resultArr[resultArr.length - 1];
        AnalyticsProcessorAdminServiceStub.AnalyticsRowResultDto[] rows = result.getRowsResults();
        Assert.assertEquals(rows.length, 1);
        Assert.assertEquals(rows[0].getColumnValues()[0], state);
        Assert.assertEquals(rows[0].getColumnValues()[1], polarity);
        Assert.assertEquals(rows[0].getColumnValues()[2], value);
    }

    private void initializeStub() throws Exception {
        ConfigurationContext cCtx = ConfigurationContextFactory.
                createConfigurationContextFromFileSystem(null);
        analyticsStub = new AnalyticsProcessorAdminServiceStub(cCtx,
                backendURL + "/services/" + ANALYTICS_SERVICE_NAME);
        ServiceClient client = analyticsStub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, getSessionCookie());
    }


    private String getAnalyticsScriptResourcePath(String scriptName) {
        return SCRIPT_RESOURCE_DIR + File.separator + scriptName;
    }

}
