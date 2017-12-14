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

package org.wso2.sp.sample.kafka.consumer;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;

/**
 * Test server for Kafka sink.
 */
public class KafkaReceiver {
    private static final Logger log = Logger.getLogger(KafkaReceiver.class);

    /**
     * Main method to start the test server.
     *
     * @param args no args need to be provided
     */
    public static void main(String[] args) {
        log.info("Initialize Kafka receiver.");
        SiddhiManager siddhiManager = new SiddhiManager();
        String topicName = args[1];
        String broker = args[0];
        String type = args[2];
        String exchange = args[3];
        String threadingOption = args[4];
        String groupid = args[5];
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(
                "@App:name('KafkaSample') " +
                        "@sink(type='log')" +
                        "define stream logStream(name string, amount double);\n" +
                        "@source(" +
                        "type='kafka', " +
                        "topic.list='" + topicName + "', " +
                        "group.id='" + groupid + "', " +
                        "threading.option='" + threadingOption + "', " +
                        "bootstrap.servers='" + broker + "'," +
                        "exchange.name ='" + exchange + "', " +
                        "@map(type='" + type + "'))" +
                        "define stream LowProducitonAlertStream(name string, amount double);\n" +
                        "from LowProducitonAlertStream\n" +
                        "select * \n" +
                        "insert into logStream;");
        siddhiAppRuntime.start();
        while (true) {
        }
    }
}
