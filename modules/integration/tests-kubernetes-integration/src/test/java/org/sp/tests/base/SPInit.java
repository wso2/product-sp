package org.sp.tests.base;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sp.integration.tests.core.beans.InstanceUrls;
import org.sp.integration.tests.core.beans.Port;
import org.sp.integration.tests.core.commons.DeploymentConfigurationReader;
import org.sp.integration.tests.core.commons.DeploymentDataReader;
import org.sp.integration.tests.core.utills.ScriptExecutorUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;


public class SPInit {

    private static final Log log = LogFactory.getLog(SPInit.class);
    public static String spURL;
    public static String mysqlURL;

    protected HashMap<String, String> instanceMap;

    /**
     * This method will initialize test environment
     * based on the configuration given at testng.xml
     */
    protected void init(String pattern) throws Exception {
        setURLs(pattern);
    }

    //set the url's as specified in the deployment.json
    protected void setURLs(String patternName) {

        HashMap<String, String> instanceMap = null;
        try {
            DeploymentConfigurationReader depconf = DeploymentConfigurationReader.readConfiguration();
            instanceMap = depconf.getDeploymentInstanceMap(patternName);
        } catch (IOException e) {
            log.error("Exception occured while getting the deployment instance map : " + e.getMessage(), e);
        }
        DeploymentDataReader dataJsonReader = new DeploymentDataReader();
        List<InstanceUrls> urlList = dataJsonReader.getInstanceUrlsList();
        for (InstanceUrls url : urlList) {
            if (instanceMap != null) {
                if (url.getLable().equals(instanceMap.get(SPConstants.POD_TAG_NAME))) {
                    spURL = getHTTPSUrl(SPConstants.SP_PORT_NAME, url.getHostIP(), url.getPorts(), "");
                }

            }
        }
    }

    protected String getHTTPSUrl(String protocol, String hostIP, List<Port> ports, String context) {

        String url = "http://" + hostIP + ":";
        for (Port port : ports) {
            if (port.getProtocol().equals(protocol)) {
                url = url + port.getPort() + context;
                break;
            }
        }
        return url;
    }

    protected String getJDBCUrl(String protocol, String hostIP, List<Port> ports, String databasename) {

        // 192.168.48.44:30306/DAS_DB
        String url = hostIP + ":";
        for (Port port : ports) {
            if (port.getProtocol().equals(protocol)) {
                url = url + port.getPort() + databasename;
                break;
            }
        }
        return url;
    }

    private boolean isURLRemapEnabled() {
        log.info("URL Remap Enabled is set to : " + System.getenv(SPConstants.ENABLE_URL_REMAP));
        return Boolean.parseBoolean((System.getenv(SPConstants.ENABLE_URL_REMAP)));
    }

    private String getRemappedURL(String localIP) {

        String remappedURL = System.getenv("IP_" + localIP.replace(".", "_"));

        if (remappedURL.equals("") | remappedURL == null) {
            log.info("No remap value found for the Local IP : " + localIP);
        }
        return remappedURL;
    }

    //deploy environment
    protected void setTestSuite(String pattern) throws IOException {
        ScriptExecutorUtil.deployScenario(pattern);
    }

    //Undeploy environment
    protected void unSetTestSuite(String pattern) throws Exception {
        ScriptExecutorUtil.unDeployScenario(pattern);
    }
}
