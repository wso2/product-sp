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
import org.wso2.carbon.analytics.messageconsole.stub.MessageConsoleStub;
import org.wso2.carbon.analytics.messageconsole.stub.beans.PermissionBean;
import org.wso2.carbon.analytics.messageconsole.stub.beans.ScheduleTaskInfo;

public class MessageConsoleClient {

    private static final Log log = LogFactory.getLog(MessageConsoleClient.class);
    private static final String serviceName = "MessageConsole";
    private final MessageConsoleStub messageConsoleStub;

    public MessageConsoleClient(String backEndUrl, String sessionCookie) throws AxisFault {
        String endPoint = backEndUrl + serviceName;
        try {
            messageConsoleStub = new MessageConsoleStub(endPoint);
            AuthenticateStubUtil.authenticateStub(sessionCookie, messageConsoleStub);
        } catch (AxisFault axisFault) {
            log.error("MessageConsoleStub Initialization fail " + axisFault.getMessage());
            throw new AxisFault("MessageConsoleStub Initialization fail " + axisFault.getMessage());
        }
    }

    public MessageConsoleClient(String backEndUrl, String userName, String password) throws AxisFault {
        String endPoint = backEndUrl + serviceName;
        try {
            messageConsoleStub = new MessageConsoleStub(endPoint);
            AuthenticateStubUtil.authenticateStub(userName, password, messageConsoleStub);
        } catch (AxisFault axisFault) {
            log.error("MessageConsoleStub Initialization fail " + axisFault.getMessage());
            throw new AxisFault("MessageConsoleStub Initialization fail " + axisFault.getMessage());
        }
    }

    public void scheduleDataPurgingTask(String table, String cronString, int retentionPeriod)
            throws Exception {
        messageConsoleStub.scheduleDataPurging(table, cronString, retentionPeriod);
    }

    public ScheduleTaskInfo getDataPurgingDetails(String table) throws Exception {
        return messageConsoleStub.getDataPurgingDetails(table);
    }

    public PermissionBean getAvailablePermissions() throws Exception {
        return messageConsoleStub.getAvailablePermissions();
    }
}
