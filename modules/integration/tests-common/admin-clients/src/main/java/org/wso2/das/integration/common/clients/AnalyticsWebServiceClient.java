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
import org.wso2.carbon.analytics.webservice.stub.beans.AnalyticsSchemaBean;
import org.wso2.carbon.analytics.webservice.stub.beans.EventBean;
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

    public RecordBean[] getByRange(String tableName, String[] columns, long timeFrom, long timeTo, int recordFrom, int
            pageSize) throws Exception {
        return webServiceStub.getByRange(tableName, 1, columns, timeFrom, timeTo, recordFrom, pageSize);
    }

    public StreamDefinitionBean getStreamDefinition(String streamName, String version) throws Exception {
        return webServiceStub.getStreamDefinition(streamName, version);
    }

    public void publishEvent(EventBean eventBean) throws Exception {
        webServiceStub.publishEvent(eventBean);
    }

    public AnalyticsSchemaBean getTableSchema(String tableName) throws Exception {
        return webServiceStub.getTableSchema(tableName);
    }

    public boolean tableExists(String tableName) throws Exception {
        return webServiceStub.tableExists(tableName);
    }

    public String[] listTables() throws Exception {
        return webServiceStub.listTables();
    }

    public RecordBean[] getById(String tableName, String[] columns, String[] ids) throws Exception {
        return webServiceStub.getById(tableName, 0, columns, ids);
    }

    public void deleteByIds(String tableName, String[] ids) throws Exception {
        webServiceStub.deleteByIds(tableName, ids);
    }

    public int searchCount(String tableName, String query) throws Exception {
        return webServiceStub.searchCount(tableName, query);
    }

    public RecordBean[] search(String tableName, String query, int start, int count) throws Exception {
        return webServiceStub.search(tableName, query, start, count);
    }

    public void clearIndices(String tableName) throws Exception {
        webServiceStub.clearIndices(tableName);
    }

    public boolean isPaginationSupported() throws Exception {
        return webServiceStub.isPaginationSupported();
    }

    public void waitForIndexing(long maxWait) throws Exception {
        webServiceStub.waitForIndexing(maxWait);
    }
}
