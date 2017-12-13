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

package org.wso2.sp.tcp.server;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;



/**
 * Test Server for TCP source.
 */
public class TCPServer {
    static Logger log = Logger.getLogger(TCPServer.class);

    /**
     * Main method to start the test Server.
     *
     * @param args host and port are passed as args
     */
    public static void main(String[] args) {
        log.info("Initialize tcp server.");
        SiddhiManager siddhiManager = new SiddhiManager();
        String url = args[0];
        String type = args[1];

        //This is for binary mapping
        if (args[1].equals("binary") || args[1].equals("json") || args[1].equals("text"))
        {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(
                    "@App:name('TestExecutionPlan') " +
                            "@source(type ='tcp',url = '" + url + "', context='LowProducitonAlertStream'," +
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
            //This is for other mappings
        }
        else
        {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(
                    "@App:name('TestExecutionPlan') " +
                            "@source(type ='tcp',url = '" + url + "', context='LowProducitonAlertStream'," +
                            "@map(type='" + type + "',@payload(\"{{message}}\")))" +
                            "define stream LowProducitonAlertStream (message string);\n" +
                            "@sink(type='log')\n" +
                            "define stream logStream(message string);\n" +
                            "from LowProducitonAlertStream\n" +
                            "select * \n" +
                            "insert into logStream;");
            siddhiAppRuntime.start();
            while (true) {
            }
        }

    }
}
