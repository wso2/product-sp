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
import org.wso2.carbon.analytics.webservice.stub.beans.AnalyticsAggregateRequest;
import org.wso2.carbon.analytics.webservice.stub.beans.AnalyticsDrillDownRequestBean;
import org.wso2.carbon.analytics.webservice.stub.beans.AnalyticsSchemaBean;
import org.wso2.carbon.analytics.webservice.stub.beans.CategoryDrillDownRequestBean;
import org.wso2.carbon.analytics.webservice.stub.beans.EventBean;
import org.wso2.carbon.analytics.webservice.stub.beans.RecordBean;
import org.wso2.carbon.analytics.webservice.stub.beans.SortByFieldBean;
import org.wso2.carbon.analytics.webservice.stub.beans.StreamDefinitionBean;
import org.wso2.carbon.analytics.webservice.stub.beans.SubCategoriesBean;
import org.wso2.carbon.analytics.webservice.stub.beans.ValuesBatchBean;

public class AnalyticsWebServiceClient {

    private static final Log log = LogFactory.getLog(AnalyticsWebServiceClient.class);
    private static final String serviceName = "AnalyticsWebService";
    private AnalyticsWebServiceStub webServiceStub;

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

    public void removeStreamDefinition(StreamDefinitionBean streamDefinitionBean) throws Exception {
        webServiceStub.removeStreamDefinition(streamDefinitionBean.getName(), streamDefinitionBean.getVersion());
    }

    public RecordBean[] getByRange(String tableName, long timeFrom, long timeTo, int recordFrom, int pageSize)
            throws Exception {
        RecordBean[] result = webServiceStub.getByRange(tableName, 1, null, timeFrom, timeTo, recordFrom, pageSize);
        if (result == null) {
            return new RecordBean[0];
        }
        return result;
    }

    public RecordBean[] getByRange(String tableName, String[] columns, long timeFrom, long timeTo, int recordFrom, int
            pageSize) throws Exception {
        RecordBean[] result = webServiceStub.getByRange(tableName, 1, columns, timeFrom, timeTo, recordFrom, pageSize);
        if (result == null) {
            return new RecordBean[0];
        }
        return result;
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
        String[] result = webServiceStub.listTables();
        if (result == null) {
            return new String[0];
        }
        return result;
    }

    public RecordBean[] getById(String tableName, String[] columns, String[] ids) throws Exception {
        RecordBean[] result = webServiceStub.getById(tableName, 0, columns, ids);
        if (result == null) {
            return new RecordBean[0];
        }
        return result;
    }

    public void deleteByIds(String tableName, String[] ids) throws Exception {
        webServiceStub.deleteByIds(tableName, ids);
    }

    public int searchCount(String tableName, String query) throws Exception {
        return webServiceStub.searchCount(tableName, query);
    }

    public RecordBean[] search(String tableName, String query, int start, int count) throws Exception {
        RecordBean[] result = webServiceStub.search(tableName, query, start, count);
        if (result == null) {
            return new RecordBean[0];
        }
        return result;
    }

    public RecordBean[] search(String tableName, String query, int start, int count, String[] columns, SortByFieldBean[] fieldBeans) throws Exception {
        RecordBean[] result = webServiceStub.searchWithSorting(tableName, query, start, count, columns, fieldBeans );
        if (result == null) {
            return new RecordBean[0];
        }
        return result;
    }

    public RecordBean[] drillDownSearch(AnalyticsDrillDownRequestBean bean) throws Exception {
        RecordBean[] result = webServiceStub.drillDownSearch(bean);
        if (result == null) {
            return new RecordBean[0];
        }
        return result;
    }

    public SubCategoriesBean drillDownCategories(CategoryDrillDownRequestBean bean) throws Exception {
        SubCategoriesBean result = webServiceStub.drillDownCategories(bean);
        if (result == null) {
            return new SubCategoriesBean();
        }
        return result;
    }

    public double drillDownSearchCount(AnalyticsDrillDownRequestBean bean) throws Exception {
        double count = webServiceStub.drillDownSearchCount(bean);
        return count;
    }

    public RecordBean[] getWithKeyValues(String tableName, String[] columns, ValuesBatchBean[] valuesBatchBeans)
            throws Exception {
        RecordBean[] result = webServiceStub.getWithKeyValues(tableName, 1, columns, valuesBatchBeans);
        if (result == null) {
            return new RecordBean[0];
        }
        return result;
    }

    public RecordBean[] searchWithAggregates(AnalyticsAggregateRequest bean)
            throws Exception {
        RecordBean[] result = webServiceStub.searchWithAggregates(bean);
        if (result == null) {
            return new RecordBean[0];
        }
        return result;
    }

    public void clearIndices(String tableName) throws Exception {
        webServiceStub.clearIndices(tableName);
    }

    public boolean isPaginationSupported(String tableName) throws Exception {
        return webServiceStub.isPaginationSupported(webServiceStub.getRecordStoreNameByTable(tableName));
    }
    
}
