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

import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 *   class contains the utility methods required by integration tests
 */
public class Utils {

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
}
