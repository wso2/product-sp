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

package org.wso2.das.integration.common.clients;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.webservice.stub.AnalyticsWebServiceStub;
import org.wso2.carbon.analytics.webservice.stub.beans.RecordBean;
import org.wso2.carbon.analytics.webservice.stub.beans.StreamDefinitionBean;

public class AnalyticsWebServiceClient {

    private static final Log log = LogFactory.getLog(AnalyticsWebServiceClient.class);
    private static final String serviceName = "AnalyticsWebService";
    private static AnalyticsWebServiceStub webServiceStub;

    public AnalyticsWebServiceClient(String backEndUrl, String sessionCookie) throws AxisFault {
        String endPoint = backEndUrl + serviceName;
        try {
            webServiceStub = new AnalyticsWebServiceStub(endPoint);
            AuthenticateStubUtil.authenticateStub(sessionCookie, webServiceStub);
        } catch (AxisFault axisFault) {
            log.error("MessageConsoleStub Initialization fail " + axisFault.getMessage());
            throw new AxisFault("MessageConsoleStub Initialization fail " + axisFault.getMessage());
        }
    }

    public AnalyticsWebServiceClient(String backEndUrl, String userName, String password) throws AxisFault {
        String endPoint = backEndUrl + serviceName;
        try {
            webServiceStub = new AnalyticsWebServiceStub(endPoint);
            AuthenticateStubUtil.authenticateStub(userName, password, webServiceStub);
        } catch (AxisFault axisFault) {
            log.error("MessageConsoleStub Initialization fail " + axisFault.getMessage());
            throw new AxisFault("MessageConsoleStub Initialization fail " + axisFault.getMessage());
        }
    }

    public long getRecordCount(String tableName, long timeFrom, long timeTo) throws Exception {
        return webServiceStub.getRecordCount(tableName, timeFrom, timeTo);
    }

    public void addStreamDefinition(StreamDefinitionBean streamDefinitionBean) throws Exception {
        webServiceStub.addStreamDefinition(streamDefinitionBean);
    }

    public RecordBean[] getByRange(String tableName, long timeFrom, long timeTo, int recordFrom, int pageSize)
            throws Exception {
        return webServiceStub.getByRange(tableName, 1, null, timeFrom, timeTo, recordFrom, pageSize);
    }
}
