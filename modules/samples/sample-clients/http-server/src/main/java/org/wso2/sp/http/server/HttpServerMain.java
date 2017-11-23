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

/**
 * This is a sample HTTP server to receive events through HTTP/HTTPS protocol.
 */
public class HttpServerMain {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(HttpServerMain.class);

    public static void main(String[] args) throws InterruptedException {
        while (true) {
            HttpServerListenerHandler lst = new HttpServerListenerHandler(8080);
            while (!lst.getServerListner().isMessageArrive()) {
                Thread.sleep(100);
            }
            logger.info("Received Event Names:" + lst.getServerListner().getData());
            logger.info("Received Event Headers key set:" + lst.getServerListner().getHeaders().keySet().toString());
            logger.info("Received Event Headers value set:" + lst.getServerListner().getHeaders().values().toString());
            lst.shutdown();
        }
    }
}
