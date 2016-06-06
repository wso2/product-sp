/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.das.integration.common.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.execution.manager.admin.dto.configuration.xsd.ScenarioConfigurationDTO;
import org.wso2.carbon.event.execution.manager.admin.dto.configuration.xsd.ScenarioConfigurationInfoDTO;
import org.wso2.carbon.event.execution.manager.admin.dto.configuration.xsd.StreamMappingDTO;
import org.wso2.carbon.event.execution.manager.admin.dto.domain.xsd.DomainInfoDTO;
import org.wso2.carbon.event.execution.manager.stub.ExecutionManagerAdminServiceStub;

import java.rmi.RemoteException;

public class ExecutionManagerAdminServiceClient {
    private static final Log log = LogFactory.getLog(ExecutionManagerAdminServiceClient.class);
    private final String serviceName = "ExecutionManagerAdminService";
    private ExecutionManagerAdminServiceStub executionManagerAdminServiceStub;
    private String endPoint;

    public ExecutionManagerAdminServiceClient(String backEndUrl, String sessionCookie) throws AxisFault {
        this.endPoint = backEndUrl + serviceName;
        executionManagerAdminServiceStub = new ExecutionManagerAdminServiceStub(endPoint);
        AuthenticateStubUtil.authenticateStub(sessionCookie, executionManagerAdminServiceStub);

    }

    public ExecutionManagerAdminServiceClient(String backEndUrl, String userName, String password) throws AxisFault {
        this.endPoint = backEndUrl + serviceName;
        executionManagerAdminServiceStub = new ExecutionManagerAdminServiceStub(endPoint);
        AuthenticateStubUtil.authenticateStub(userName, password, executionManagerAdminServiceStub);
    }

    public ServiceClient _getServiceClient() {
        return executionManagerAdminServiceStub._getServiceClient();
    }

    public String[] saveConfiguration(ScenarioConfigurationDTO scenarioConfigurationDTO) throws RemoteException {
        try {
            return executionManagerAdminServiceStub.saveConfiguration(scenarioConfigurationDTO);
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    public boolean saveStreamMapping(StreamMappingDTO[]
                                             streamMappingDTOs, String configName, String domainName) throws RemoteException {
        try {
            return executionManagerAdminServiceStub.saveStreamMapping(streamMappingDTOs, configName, domainName);
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    public boolean deleteConfiguration(String domainName, String configurationName) throws RemoteException {
        try {
            return executionManagerAdminServiceStub.deleteConfiguration(domainName, configurationName);
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    public int getConfigurationsCount(String domainName) throws RemoteException {
        int count = 0;
        try {
            ScenarioConfigurationInfoDTO[] configs = executionManagerAdminServiceStub.getConfigurationInfos(domainName);
            if (configs != null) {
                count = configs.length;
            }
            return count;
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    public DomainInfoDTO[] getAllDomainInfos()
            throws RemoteException {
        try {
            return executionManagerAdminServiceStub.getAllDomainInfos();
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    public DomainInfoDTO getDomainInfo(String domainName) throws RemoteException {
        try {
            return executionManagerAdminServiceStub.getDomainInfo(domainName);
        } catch (RemoteException e) {
            log.error("RemoteException", e);
            throw new RemoteException(e.getMessage(), e);
        }
    }
}
