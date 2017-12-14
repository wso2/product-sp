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

package org.wso2.sp.mqtt.client;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This is a sample MQTT client to publish events to endpoint.
 */
public class MqttClient {
    private static final Logger log = Logger.getLogger(MqttClient.class);

    /**
     * Main method to start the test client.
     *
     * @param args no args need to be provided
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        log.info("Initialize mqtt client.");
        final String[] types = new String[]{"json", "xml", "text"};
        SiddhiManager siddhiManager = new SiddhiManager();
        String publisherUrl = args[0];
        String topic = args[1];
        String type = Arrays.asList(types).contains(args[2]) ? args[2] : "json";
        int noOfEventsToSend = !args[7].isEmpty() ? Integer.parseInt(args[7]) : -1;
        List<String[]> fileEntriesList = null;

        boolean sendEventsCountinously = true;
        if (noOfEventsToSend != -1) {
            sendEventsCountinously = false;
        }

        if (!args[3].equals("")) {
            String filePath = args[2];
            fileEntriesList = readFile(filePath);
        }
        String eventDefinition;
        if (!args[4].equals("")) {
            eventDefinition = args[4];
        } else {
            if (!args[6].equals("")) {
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
                        "@sink(type=\"mqtt\", url=\'" + publisherUrl + "\', topic=\'" + topic + "\'," +
                        "@map(type='" + type + "', @payload(\"{{message}}\")))" +
                        "define stream MqttClientStream (message string);");

        siddhiAppRuntime.start();
        InputHandler httpClientStream = siddhiAppRuntime.getInputHandler("MqttClientStream");
        String[] sweetName = {"Cupcake", "Donut", "Éclair", "Froyo", "Gingerbread", "Honeycomb", "Ice",
                "Cream Sandwich", "Jelly Bean", "KitKat", "Lollipop", "Marshmallow"};

        String message = null;
        int sentEvents = 0;
        while (sendEventsCountinously || sentEvents != noOfEventsToSend--) {
            if (fileEntriesList != null) {
                Iterator iterator = fileEntriesList.iterator();
                while (iterator.hasNext()) {
                    String[] stringArray = (String[]) iterator.next();
                    for (int i = 0; i < stringArray.length; i++) {
                        message = eventDefinition.replace("{" + i + "}", stringArray[i]);
                    }
                    httpClientStream.send(new Object[]{message});
                }
            } else {
                int amount = ThreadLocalRandom.current().nextInt(1, 10000);
                String name = sweetName[ThreadLocalRandom.current().nextInt(0, sweetName.length)];
                message = eventDefinition.replace("{0}", name).replace("{1}", Integer.toString(amount));
                httpClientStream.send(new Object[]{message});
            }
            log.info("Sent event:"+message);
            Thread.sleep(Long.parseLong(args[5]));
        }
        siddhiAppRuntime.shutdown();

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
