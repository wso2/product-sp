/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.sp.sample.websocket.server;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * This is a sample WebSocket to publish events to endpoint.
 */
public class WebSocketProducer {
    private static final Logger log = Logger.getLogger(WebSocketProducer.class);

    /**
     * Main method to start the test server.
     *
     * @param args no args need to be provided
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        log.info("Initialize websocket publisher.");
        final String[] types = new String[]{"json", "xml", "text"};
        SiddhiManager siddhiManager = new SiddhiManager();
        String host = args[0];
        String port = args[1];
        String type = Arrays.asList(types).contains(args[2]) ? args[2] : "json";
        int noOfEventsToSend = !args[5].isEmpty() ? Integer.parseInt(args[5]) : -1;
        int delay = !args[9].isEmpty() ? Integer.parseInt(args[9]) : -1;
        boolean continuouslyReadFile = !args[6].isEmpty() && Boolean.parseBoolean(args[6]);
        List<String[]> fileEntriesList = null;

        boolean sendEventsCountinously = true;
        if (noOfEventsToSend != -1) {
            sendEventsCountinously = false;
        }

        if (!args[7].equals("")) {
            String filePath = args[2];
            fileEntriesList = readFile(filePath);
        }
        String eventDefinition;
        if (!args[10].equals("")) {
            eventDefinition = args[10];
        } else {
            if (!args[8].equals("")) {
                if (type.equals("json")) {
                    eventDefinition = "{\"item\": {\"id\":\"{0}\",\"amount\": {1}}}";
                } else if (type.equals("xml")) {
                    eventDefinition = "<events><item><id>{0}</id><amount>{1}</amount></item></events>";
                } else {
                    eventDefinition = "id:\"{0}\"\namount:{1}";
                }
            } else {
                if (type.equals("json")) {
                    eventDefinition = "{\"event\": {\"name\":\"{0}\",\"amount\": {1}}}";
                } else if (type.equals("xml")) {
                    eventDefinition = "<events><event><name>{0}</name><amount>{1}</amount></event></events>";
                } else {
                    eventDefinition = "name:\"{0}\",\namount:{1}";
                }
            }
        }
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(
                "@App:name('TestExecutionPlan') " +
                        "@sink(type=\"websocket-server\", host=\'" + host + "\', port=\'" + port + "\'," +
                        "@map(type='" + type + "', @payload(\"{{message}}\")))" +
                        "define stream WebSocketClientStream (message string);");

        siddhiAppRuntime.start();
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("WebSocketClientStream");
        String[] sweetName = {"Cupcake", "Donut", "Éclair", "Froyo", "Gingerbread", "Honeycomb", "Ice",
                              "Cream Sandwich", "Jelly Bean", "KitKat", "Lollipop", "Marshmallow"};

        Thread.sleep(2000);
        EventSendingUtil.publishEvents(fileEntriesList, sendEventsCountinously, noOfEventsToSend, eventDefinition,
                                       sweetName, inputHandler, delay, false, continuouslyReadFile);
        Thread.sleep(2000);
        siddhiAppRuntime.shutdown();
        Thread.sleep(2000);

    }

    private static List<String[]> readFile(String fileName) throws IOException {
        File file = new File(fileName);
        Scanner inputStream = new Scanner(file);
        List<String[]> fileEntriesList = new ArrayList<String[]>();
        while (inputStream.hasNext()) {
            String data = inputStream.next();
            fileEntriesList.add(data.split(","));
        }
        inputStream.close();
        return fileEntriesList;
    }
}
