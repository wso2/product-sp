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
import org.wso2.carbon.analytics.stream.persistence.stub.EventStreamPersistenceAdminServiceStub;
import org.wso2.carbon.analytics.stream.persistence.stub.dto.AnalyticsTable;

public class EventStreamPersistenceClient {

    private static final Log log = LogFactory.getLog(EventStreamPersistenceClient.class);
    private static final String serviceName = "EventStreamPersistenceAdminService";
    private final EventStreamPersistenceAdminServiceStub persistenceAdminServiceStub;

    public EventStreamPersistenceClient(String backEndUrl, String sessionCookie) throws AxisFault {
        String endPoint = backEndUrl + serviceName;
        try {
            persistenceAdminServiceStub = new EventStreamPersistenceAdminServiceStub(endPoint);
            AuthenticateStubUtil.authenticateStub(sessionCookie, persistenceAdminServiceStub);
        } catch (AxisFault axisFault) {
            log.error("MessageConsoleStub Initialization fail " + axisFault.getMessage());
            throw new AxisFault("MessageConsoleStub Initialization fail " + axisFault.getMessage());
        }
    }

    public EventStreamPersistenceClient(String backEndUrl, String userName, String password) throws AxisFault {
        String endPoint = backEndUrl + serviceName;
        try {
            persistenceAdminServiceStub = new EventStreamPersistenceAdminServiceStub(endPoint);
            AuthenticateStubUtil.authenticateStub(userName, password, persistenceAdminServiceStub);
        } catch (AxisFault axisFault) {
            log.error("MessageConsoleStub Initialization fail " + axisFault.getMessage());
            throw new AxisFault("MessageConsoleStub Initialization fail " + axisFault.getMessage());
        }
    }

    public boolean isBackendServicePresent() throws Exception {
        return persistenceAdminServiceStub.isBackendServicePresent();
    }

    public void addAnalyticsTable(AnalyticsTable analyticsTable) throws Exception {
        persistenceAdminServiceStub.addAnalyticsTable(analyticsTable);
    }

    public AnalyticsTable getAnalyticsTable(String streamName, String version) throws Exception {
        return persistenceAdminServiceStub.getAnalyticsTable(streamName, version);
    }
}
