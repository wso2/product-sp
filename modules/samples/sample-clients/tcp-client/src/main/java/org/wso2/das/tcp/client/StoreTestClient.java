/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.das.tcp.client;

import org.apache.log4j.Logger;
import org.wso2.extension.siddhi.io.tcp.transport.TCPNettyClient;
import org.wso2.siddhi.core.event.Event;

import java.util.ArrayList;

/**
 * Store test client
 */
public class StoreTestClient {
    static final String STREAM_NAME = "TestData";
    static Logger log = Logger.getLogger(StoreTestClient.class);

    /**
     * Main method to start the test client
     *
     * @param args host and port need to be provided as args
     */
    public static void main(String[] args) {
        /*
         * Stream definition:
         * TestData (property bool)
         */
        TCPNettyClient tcpNettyClient = new TCPNettyClient();
        tcpNettyClient.connect(args[0], Integer.parseInt(args[1]));
        log.info("TCP client for Store Test connected");

        ArrayList<Event> arrayList = new ArrayList<Event>();
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{Boolean.TRUE}));
        tcpNettyClient.send(STREAM_NAME, arrayList.toArray(new Event[1]));
        log.info("TCP client for Store Test finished sending events");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        tcpNettyClient.disconnect();
        tcpNettyClient.shutdown();
    }
}
