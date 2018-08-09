/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.sp.sample.jms.consumer;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;

/**
 * Test client for Jms source.
 */
public class JmsReceiver {
    private static final Logger log = Logger.getLogger(JmsReceiver.class);

    /**
     * Main method to start the test client.
     *
     * @param args no args need to be provided
     */
    public static void main(String[] args) {
        log.info("Initialize Jms receiver.");
        SiddhiManager siddhiManager = new SiddhiManager();
        String publisherUrl = args[0];
        String destination = args[1];
        String type = args[2];
        String factoryType = args[3];
        String jndiName = args[4];
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(
                        "@App:name('JmsReceiver')\n" +
                        "@sink(type='log')" +
                        "define stream logStream(name string, amount double);\n" +
                        "@source(type='jms',@map(type='" + type + "'),\n" +
                        "factory.initial='org.apache.activemq.jndi.ActiveMQInitialContextFactory',\n" +
                        "provider.url='" + publisherUrl + "', destination='" + destination + "', " +
                        "connection.factory.type='" + factoryType + "', \n" +
                        "connection.factory.jndi.name='" + jndiName + "')\n" +
                        "define stream jmsConsumerStream(name string, amount double);\n" +
                        "from jmsConsumerStream\n" +
                        "select * \n" +
                        "insert into logStream;");
        siddhiAppRuntime.start();
        while (true) {
        }
    }
}

