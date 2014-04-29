package org.wso2.bam.integration.tests.analytics;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.testng.annotations.Test;
import org.wso2.bam.integration.tests.hive.BAMJDBCHandlerTestCase;
import org.wso2.carbon.analytics.hive.stub.HiveExecutionServiceHiveExecutionException;
import org.wso2.carbon.analytics.hive.stub.HiveExecutionServiceStub;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;

import static junit.framework.Assert.fail;

public class BAMAnalyzerTestCase {

    private HiveExecutionServiceStub hiveStub;
    private LoginLogoutUtil util = new LoginLogoutUtil();
    private static final String HIVE_SERVICE = "/services/HiveExecutionService";

    @Test(groups = {"wso2.bam"})
    public void executeScript() {

        String[] queries = getHiveQueries("BAMAnalyzerSampleScript");

        try {
            hiveStub.executeHiveScript(null, queries[0]); // Create table with map data type in schema

            HiveExecutionServiceStub.QueryResult[] results = hiveStub.executeHiveScript(null, queries[1]);

            if (results == null || results.length == 0) {
                fail("No results returned..");
            }

        } catch (HiveExecutionServiceHiveExecutionException e) {
            fail("Failed while excecuting hive script " + e.getMessage());
        } catch (Exception e){
            fail("Error when trying to run hive script: "+ e.getMessage());
        }

    }

    private void initializeHiveStub() throws Exception {
        ConfigurationContext configContext = ConfigurationContextFactory.
                createConfigurationContextFromFileSystem(null);

        String loggedInSessionCookie = util.login();

        String EPR = "https://" + FrameworkSettings.HOST_NAME +
                ":" + FrameworkSettings.HTTPS_PORT + HIVE_SERVICE;
        hiveStub = new HiveExecutionServiceStub(configContext, EPR);
        ServiceClient client = hiveStub._getServiceClient();
        Options option = client.getOptions();
        option.setTimeOutInMilliSeconds(10 * 60000);
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                loggedInSessionCookie);
    }

    private String[] getHiveQueries(String resourceName) {
        String[] queries = null;
        try {
            initializeHiveStub();
        } catch (Exception e) {
            fail("Error while initializing hive stub: " + e.getMessage());
        }

        URL url = BAMJDBCHandlerTestCase.class.getClassLoader().getResource(resourceName);

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(
                    new File(url.toURI()).getAbsolutePath()));
            String script = "";
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                script += line;
            }
            queries = script.split(";");
        } catch (Exception e) {
            fail("Error while reading resource : " + resourceName);
        }
        return queries;
    }
}
