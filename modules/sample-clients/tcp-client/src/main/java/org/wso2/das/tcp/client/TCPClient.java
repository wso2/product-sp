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

import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.tcp.transport.TCPNettyClient;

import java.util.ArrayList;

/**
 * Test client for TCP source
 */
public class TCPClient {
    /**
     * Main method to start the test client
     *
     * @param args no args need to be provided
     */
    public static void main(String[] args) {
        TCPNettyClient tcpNettyClient = new TCPNettyClient();
        tcpNettyClient.connect("localhost", 9892);
        int i = 0;
        for (; i < 10; i++) {
            ArrayList<Event> arrayList = new ArrayList<Event>(100);
            for (int j = 0; j < 5; j++) {
                arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"WSO2", i, 10}));
                arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"IBM", i, 10}));
            }
            tcpNettyClient.send("StockStream", arrayList.toArray(new Event[10]));
        }
        System.out.println("");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        tcpNettyClient.disconnect();
        tcpNettyClient.shutdown();
    }
}
