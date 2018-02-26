/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.sp.sample.websocket.receiver;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;

/**
 * Test server for WebSocket source.
 */
public class WebSocketReceiver {
    private static final Logger log = Logger.getLogger(WebSocketReceiver.class);

    /**
     * Main method to start the test server.
     *
     * @param args no args need to be provided
     */
    public static void main(String[] args) {
        log.info("Initialize WebSocket receiver.");
        SiddhiManager siddhiManager = new SiddhiManager();
        String host = args[0];
        String port = args[1];
        String type = args[2];
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(
                "@App:name(\"WebSocketSample\")\n" +
                        "@source(type ='websocket-server',host = '" + host + "',port ='" + port + "', " +
                        "@map(type='" + type + "'))" +
                        "define stream LowProducitonAlertStream (name string, amount double);\n" +
                        "@sink(type='log')\n" +
                        "define stream logStream(name string, amount double);\n" +
                        "from LowProducitonAlertStream\n" +
                        "select * \n" +
                        "insert into logStream;");
        siddhiAppRuntime.start();
        while (true) {
        }
    }
}
