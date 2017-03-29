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
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.tcp.transport.TCPNettyClient;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Test client for TCP source
 */
public class TCPClient {
    static Logger log = Logger.getLogger(TCPClient.class);
    static final int EVENT_COUNT = 100;
    static final int BATCH_SIZE = 10;
    static final String STREAM_NAME = "SmartHomeData";

    /**
     * Main method to start the test client
     *
     * @param args host and port need to be provided as args
     */
    public static void main(String[] args) {
        /*
         * Stream definition:
         * SmartHomeData (id string, value float, property bool, plugId int, householdId int, houseId int,
         *      currentTime string)
         */
        TCPNettyClient tcpNettyClient = new TCPNettyClient();
        tcpNettyClient.connect(args[0], Integer.parseInt(args[1]));
        log.info("TCP client connected");

        int houseId, householdId, plugId;
        boolean property;
        float value;

        int i = 0;
        for (; i < EVENT_COUNT; i += BATCH_SIZE) {
            ArrayList<Event> arrayList = new ArrayList<Event>(BATCH_SIZE);
            for (int j = 0; j < BATCH_SIZE; j++) {
                houseId = ThreadLocalRandom.current().nextInt(1, 10);
                householdId = ThreadLocalRandom.current().nextInt(30, 40);
                plugId = ThreadLocalRandom.current().nextInt(11, 20);
                property = ThreadLocalRandom.current().nextBoolean();
                value = (float) ThreadLocalRandom.current().nextDouble(300, 500);
                arrayList.add(new Event(System.currentTimeMillis(), new Object[]{UUID.randomUUID().toString(), value,
                        property, plugId, householdId, houseId, getCurrentTimestamp()}));
            }
            tcpNettyClient.send(STREAM_NAME, arrayList.toArray(new Event[BATCH_SIZE]));
        }
        log.info("TCP client finished sending events");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        tcpNettyClient.disconnect();
        tcpNettyClient.shutdown();
    }

    private static String getCurrentTimestamp() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }
}
