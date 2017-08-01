/*
 *  Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */
package org.wso2.sp.http.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Test Server Listener Manger.
 */
public class HttpServerListener implements HttpHandler {
    private static final Logger logger = Logger.getLogger(HttpServerListener.class);
    private AtomicBoolean isEventArraved = new AtomicBoolean(false);
    private StringBuilder strBld = new StringBuilder();
    private Headers headers;

    HttpServerListener() {
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        // Get the paramString form the request
        String line;
        headers = t.getRequestHeaders();
        InputStream is = t.getRequestBody();
        BufferedReader in = new BufferedReader(new InputStreamReader(is)); // initiating
        String name = null;
        while ((line = in.readLine()) != null) {
            strBld = strBld.append(line).append(" , ");
            name = line;
        }

        logger.info("Event Name Arrived: " + name);
        isEventArraved.set(true);
    }

    String getData() {
        return strBld.toString();
    }

    Headers getHeaders() {
        return headers;
    }

    boolean isMessageArrive() {
        return isEventArraved.get();
    }

}
