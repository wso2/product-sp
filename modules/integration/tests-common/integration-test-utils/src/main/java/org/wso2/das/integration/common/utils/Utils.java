/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.das.integration.common.utils;

import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.stream.persistence.stub.dto.AnalyticsTable;
import org.wso2.carbon.analytics.stream.persistence.stub.dto.AnalyticsTableRecord;
import org.wso2.carbon.analytics.webservice.stub.beans.StreamDefinitionBean;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.das.integration.common.clients.AnalyticsWebServiceClient;
import org.wso2.das.integration.common.clients.EventStreamPersistenceClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * This class contains the utility methods required by integration tests.
 */
public class Utils {
    
    private static final int DEFAULT_CHECK_AND_WAIT_RETRY_COUNT = 20;
    private static final int DEFAULT_CHECK_AND_WAIT_INTERVAL = 2000;
    private static org.apache.commons.logging.Log log = LogFactory.getLog(Utils.class);

    //use this method since HttpRequestUtils.doGet does not support HTTPS.
    public static HttpResponse doGet(String endpoint, Map<String, String> headers) throws
                                                                                   IOException {
        HttpResponse httpResponse;
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setDoOutput(true);
        conn.setReadTimeout(30000);
        //setting headers
        if (headers != null && headers.size() > 0) {
            for (String key : headers.keySet()) {
                if (key != null) {
                    conn.setRequestProperty(key, headers.get(key));
                }
            }
            for (String key : headers.keySet()) {
                conn.setRequestProperty(key, headers.get(key));
            }
        }
        conn.connect();
        // Get the response
        StringBuilder sb = new StringBuilder();
        BufferedReader rd = null;
        try {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            httpResponse = new HttpResponse(sb.toString(), conn.getResponseCode());
            httpResponse.setResponseMessage(conn.getResponseMessage());
        } catch (IOException ignored) {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            httpResponse = new HttpResponse(sb.toString(), conn.getResponseCode());
            httpResponse.setResponseMessage(conn.getResponseMessage());
        } finally {
            if (rd != null) {
                rd.close();
            }
        }
        return httpResponse;
    }
    
    public static void checkAndWait(Callable<Boolean> exec, int intervalMS, int maxRetryCount, String waitTimeoutMessage) {
        int i = 0;
        while (!checkResultOrException(exec)) {
            if (i >= maxRetryCount) {
                String msg = "Check and Wait Expired";
                if (waitTimeoutMessage != null) {
                    msg += ": " + waitTimeoutMessage;
                }
                throw new RuntimeException(msg);
            }
            try {
                Thread.sleep(intervalMS);
            } catch (InterruptedException e) {
                log.warn("Check and Wait Interuppted: " + e.getMessage());
                return;
            }
            i++;
        }
    }
    
    private static boolean checkResultOrException(Callable<Boolean> exec) {
        try {
            return exec.call();
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Check Result Or Exception: " + e.getMessage());
            }
            return false;
        }
    }
    
    public static void checkAndWait(Callable<Boolean> exec, String waitTimeoutMessage) {
        checkAndWait(exec, DEFAULT_CHECK_AND_WAIT_INTERVAL, DEFAULT_CHECK_AND_WAIT_RETRY_COUNT, waitTimeoutMessage);
    }
    
    public static void checkAndWait(Callable<Boolean> exec) {
        checkAndWait(exec, DEFAULT_CHECK_AND_WAIT_INTERVAL, DEFAULT_CHECK_AND_WAIT_RETRY_COUNT, null);
    }
    
    public static void checkAndWaitForStream(final AnalyticsWebServiceClient webServiceClient, 
            final String stream, final String version, final boolean exists) throws Exception {
        checkAndWait(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                StreamDefinitionBean def = webServiceClient.getStreamDefinition(stream, version);
                return (def != null) == exists;
            }
        });
    }
    
    public static void checkAndWaitForStreamAndPersist(final AnalyticsWebServiceClient webServiceClient, 
            final EventStreamPersistenceClient persistenceClient, final String stream, final String version) throws Exception {
        checkAndWaitForStreamAndPersist(webServiceClient, persistenceClient, stream, version, true);
    }
    
    public static void checkAndWaitForStreamAndPersist(final AnalyticsWebServiceClient webServiceClient, 
            final EventStreamPersistenceClient persistenceClient, final String stream, final String version, 
            final boolean persist) throws Exception {
        checkAndWait(new Callable<Boolean>() {            
            @Override
            public Boolean call() throws Exception {
                StreamDefinitionBean def = webServiceClient.getStreamDefinition(stream, version);
                AnalyticsTable table = persistenceClient.getAnalyticsTable(stream, version);
                return def != null && table != null && (table.getPersist() == persist);
            }
        });
    }
    
    public static void checkAndWaitForStreamAndPersistColumn(final AnalyticsWebServiceClient webServiceClient, 
            final EventStreamPersistenceClient persistenceClient, final String stream, final String version, 
            final String columnName, final boolean persist) throws Exception {
        checkAndWait(new Callable<Boolean>() {            
            @Override
            public Boolean call() throws Exception {
                StreamDefinitionBean def = webServiceClient.getStreamDefinition(stream, version);
                AnalyticsTable table = persistenceClient.getAnalyticsTable(stream, version);
                return def != null && table != null && (columnExists(columnName, table.getAnalyticsTableRecords()) == persist);
            }
        });
    }
    
    private static boolean columnExists(String columnName, AnalyticsTableRecord[] records) {
        for (AnalyticsTableRecord rec : records) {
            if (rec.getColumnName().equals(columnName)) {
                return true;
            }
        }
        return false;
    }
    
    public static void addStreamAndPersistence(final AnalyticsWebServiceClient webServiceClient, 
            final EventStreamPersistenceClient persistenceClient, StreamDefinitionBean streamDef, 
            AnalyticsTable persistedTable) throws Exception {
        webServiceClient.addStreamDefinition(streamDef);
        persistenceClient.addAnalyticsTable(persistedTable);
        checkAndWaitForStreamAndPersist(webServiceClient, persistenceClient, streamDef.getName(), streamDef.getVersion());
    }
    
    public static void checkAndWaitForTableSize(final AnalyticsWebServiceClient webServiceClient, 
            final String tableName, final int count) {
        checkAndWait(new Callable<Boolean>() {            
            @Override
            public Boolean call() throws Exception {
                return webServiceClient.getByRange(tableName, Long.MIN_VALUE + 1, Long.MAX_VALUE, 0, count + 1).length == count;
            }
        });
    }
    
    public static void checkAndWaitForSearchQuerySize(final AnalyticsWebServiceClient webServiceClient, 
            final String tableName, final String query, final int count) {
        checkAndWait(new Callable<Boolean>() {            
            @Override
            public Boolean call() throws Exception {
                return webServiceClient.searchCount(tableName, query) == count;
            }
        });
    }
    
}
