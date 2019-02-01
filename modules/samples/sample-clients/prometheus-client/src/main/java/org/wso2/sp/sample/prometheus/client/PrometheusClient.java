/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.sp.sample.prometheus.client;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Test client for Prometheus source.
 */
public class PrometheusClient {

    private static Log log = LogFactory.getLog(PrometheusClient.class);
    private static Random random = new Random();
    private static AtomicInteger eventCount = new AtomicInteger(0);
    private static String[] deviceIDArray = new String[]{"server001", "server002", "monitor001", "server003"};
    private static String[] roomIDArray = new String[]{"F3Room2", "F2Room2", "F3Room4", "F2Room1"};

    /**
     * Main method to start the test client.
     *
     * @param args no args need to be provided
     */
    public static void main(String[] args) {
        eventCount.set(0);
        String serverURL = args[0];
        log.info("Initialize Prometheus client.");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inputStream = "@App:name('TestSiddhiApp')" +
                "                        \"define stream InputStream (deviceID string, roomID string, power int);";
        String sinkStream = "@sink(type='prometheus'," +
                "job='prometheusSample'," +
                "publish.mode='server'," +
                "server.url='" + serverURL + "'," +
                "metric.type='counter'," +
                "value.attribute= 'power'," +
                "metric.name= 'total_device_power_consumption_WATTS'," +
                "metric.help= 'Total Power consumption of each devices in Watts'," +
                "@map(type = 'keyvalue'))" +
                "Define stream SinkStream (deviceID string, roomID string, power int);";
        String query = (
                "@info(name = 'query') "
                        + "from InputStream "
                        + "select *"
                        + "insert into SinkStream;"
        );

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inputStream + sinkStream + query);
        siddhiAppRuntime.start();
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("InputStream");
        int i;
        while (true) {
            i = random.nextInt(3);
            try {
                inputHandler.send(new Object[]{deviceIDArray[i], roomIDArray[i], getPowerConsumption()});
                eventCount.getAndIncrement();
                Thread.sleep(3000);
                if (eventCount.get() >= 10) {
                    break;
                }
            } catch (InterruptedException e) {
                log.error("Interrupted exception thrown while sending events", e);
            }
        }
        try {
            Thread.sleep(150000);
        } catch (InterruptedException e) {
            log.error("Interrupted exception thrown while executing client", e);
        }
        siddhiAppRuntime.shutdown();
    }

    private static int getPowerConsumption() {
        return random.nextInt(15) + 5;
    }
}
