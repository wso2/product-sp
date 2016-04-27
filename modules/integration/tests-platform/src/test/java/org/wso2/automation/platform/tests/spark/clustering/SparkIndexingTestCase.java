/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.automation.platform.tests.spark.clustering;

import com.google.gson.Gson;
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
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.das.analytics.rest.beans.QueryBean;
import org.wso2.das.integration.common.utils.DASIntegrationTest;
import org.wso2.das.integration.common.utils.TestConstants;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * This Note: This test case is not run with default DAS integration tests. To run this test we assume
 * that DAS server is running in the clustering mode.
 */

public class SparkIndexingTestCase extends DASIntegrationTest {
    private static final Log log = LogFactory.getLog(SparkIndexingTestCase.class);

    private static final String TABLE_NAME = "spark_indexing_test";
    private static final String ANALYTICS_SERVICE_NAME = "AnalyticsProcessorAdminService";

    private AnalyticsProcessorAdminServiceStub analyticsStub;
    private Map<String, String> headers;
    private static final Gson gson = new Gson();

    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        super.init();
        initializeStub();

        headers = new HashMap<>();
        headers.put("Content-Type", TestConstants.CONTENT_TYPE_JSON);
        headers.put("Accept", TestConstants.CONTENT_TYPE_JSON);
        headers.put("Authorization", TestConstants.BASE64_ADMIN_ADMIN);
    }


    private void initializeStub() throws Exception {
        ConfigurationContext configContext = ConfigurationContextFactory.
                createConfigurationContextFromFileSystem(null);
        String loggedInSessionCookie = getSessionCookie();
        analyticsStub = new AnalyticsProcessorAdminServiceStub(configContext,
                                                               backendURL + "/services/" + ANALYTICS_SERVICE_NAME);
        ServiceClient client = analyticsStub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                           loggedInSessionCookie);
    }


    @Test(groups = "wso2.das", description = "Executing spark queries with indexing")
    public void populateDataUsingSpark() throws Exception {
        log.info("Executing data population test case ...");
        analyticsStub.executeQuery("CREATE TEMPORARY TABLE testIdx USING CarbonAnalytics OPTIONS" +
                                   "(tableName \"" + TABLE_NAME + "\", schema \"x int -i, y int -i\" ) ;");
        Random rn = new Random();
        for (int i = 0; i < 10; i++) {
            analyticsStub.executeQuery("INSERT INTO TABLE testIdx SELECT " + rn.nextInt(1000) + " , "
                                       + rn.nextInt(1000) + " ;");
        }
        AnalyticsProcessorAdminServiceStub.AnalyticsQueryResultDto result = analyticsStub.
                executeQuery("SELECT * FROM testIdx ;");

        Assert.assertEquals(10, result.getRowsResults().length, "Number of rows doesn't match");
    }

    @Test(groups = "wso2.das", description = "Check the indexed data from a lucene query", dependsOnMethods =
            "populateDataUsingSpark")
    public void checkIndexedData() throws Exception {
        log.info("Executing indexed data check test case ...");
        URL restUrl = new URL(TestConstants.ANALYTICS_SEARCH_COUNT_ENDPOINT_URL);
        QueryBean query = new QueryBean();
        query.setTableName(TABLE_NAME);
        query.setQuery("*:*");
        boolean codeOK = false;
        int counter = 0;
        HttpResponse response;
        while (!codeOK) {
            response = HttpRequestUtil.doPost(restUrl, gson.toJson(query), headers);
            codeOK = (response.getResponseCode() == 200) && response.getData().contains("10");
            if (!codeOK) {
                Thread.sleep(2000L);
            }
            if (counter == 10) {
                Assert.assertEquals(response.getResponseCode(), 200, "Status code is different");
                Assert.assertTrue(response.getData().contains("10"), "Search Count mismatch!");
            }
            counter++;
        }
    }
}
