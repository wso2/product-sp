package org.wso2.das.integration.tests.sessionTimeoutExceptionTest;

import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;
import org.wso2.carbon.automation.extensions.servers.carbonserver.TestServerManager;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.Map;

public class CarbonTestServerManager extends TestServerManager {

    public CarbonTestServerManager(AutomationContext context) throws XPathExpressionException {
        super(context);
    }
    public CarbonTestServerManager(AutomationContext context, String carbonZip, Map<String, String> startupParameterMap)
            throws XPathExpressionException {
        super(context, carbonZip, startupParameterMap);
    }

    public CarbonTestServerManager(AutomationContext context, int portOffset) throws XPathExpressionException {
        super(context, portOffset);
    }

    public String startServer() throws AutomationFrameworkException, IOException, XPathExpressionException {
        carbonHome = super.startServer();
        return carbonHome;
    }

    public void stopServer() throws AutomationFrameworkException {
        super.stopServer();
    }

    public String getCarbonHome() {
        return carbonHome;
    }
}