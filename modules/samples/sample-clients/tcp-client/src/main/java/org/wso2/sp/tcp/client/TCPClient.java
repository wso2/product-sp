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

package org.wso2.sp.tcp.client;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This is a sample TCP client to publish events to TCP endpoint.
 */
public class TCPClient {
    private static final Logger log = Logger.getLogger(TCPClient.class);

    /**
     * Main method to start the test client.
     *
     * @param args no args need to be provided
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        log.info("Initialize tcp client.");
        final String[] types = new String[]{"json", "xml", "text"};
        SiddhiManager siddhiManager = new SiddhiManager();
        String publisherUrl = args[0];
        String type = args[1];
        int noOfEventsToSend = !args[6].isEmpty() ? Integer.parseInt(args[6]) : -1;
        List<String[]> fileEntriesList = null;

        boolean sendEventsCountinously = true;
        if (noOfEventsToSend != -1) {
            sendEventsCountinously = false;
        }
        if (!args[2].equals("")) {
            String filePath = args[2];
            fileEntriesList = readFile(filePath);
        }
        String eventDefinition;
        if (!args[3].equals("")) {
            eventDefinition = args[3];
        } else {
            if (!args[5].equals("")) {
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

        InputHandler tcpClientStream;

        String[] sweetName = {"Cupcake", "Donut", "Eclair", "Froyo", "Gingerbread", "Honeycomb", "Ice",
                "Cream Sandwich", "Jelly Bean", "KitKat", "Lollipop", "Marshmallow"};

        //This is for binary mapping
        if (args[1].equals("binary")) {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(
                    "@App:name('TestExecutionPlan') " +
                            "@sink(type = 'tcp', url = '" + publisherUrl + "'," +
                            "@map(type='" + type + "'))" +
                            "define stream TcpClientStream (name string, amount double);");
            siddhiAppRuntime.start();
            tcpClientStream = siddhiAppRuntime.getInputHandler("TcpClientStream");

            for (int i = 0; i < sweetName.length; i++) {
                String name = sweetName[i];
                Random r = new Random();
                double amount = (10.0 + r.nextDouble() * 200.0);
                tcpClientStream.send(new Object[]{name, amount});
                Thread.sleep(Long.parseLong(args[4]));
            }
            //This is for other mappings
        } else {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(
                    "@App:name('TestExecutionPlan') " +
                            "@sink(type = 'tcp', url = '" + publisherUrl + "'," +
                            "@map(type='" + type + "',@payload(\"{{message}}\")))" +
                            "define stream TcpClientStream (message string);");

            siddhiAppRuntime.start();
            tcpClientStream = siddhiAppRuntime.getInputHandler("TcpClientStream");

            String message = null;
            int sentEvents = 0;
            while (sendEventsCountinously || sentEvents != noOfEventsToSend--) {

                if (fileEntriesList != null) {
                    Iterator iterator = fileEntriesList.iterator();
                    while (iterator.hasNext()) {
                        String[] stringArray = (String[]) iterator.next();
                        message = eventDefinition;
                        for (int i = 0; i < stringArray.length; i++) {
                            message = eventDefinition.replace("{" + i + "}", stringArray[i]);
                        }
                        tcpClientStream.send(new Object[]{message});
                    }
                } else {
                    int amount = ThreadLocalRandom.current().nextInt(1, 10000);
                    String name = sweetName[ThreadLocalRandom.current().nextInt(0, sweetName.length)];
                    message = eventDefinition.replace("{0}", name).replace("{1}", Integer.toString(amount));
                    tcpClientStream.send(new Object[]{message});
                }
                log.info("Sent event:" + message);
                Thread.sleep(Long.parseLong(args[4]));
            }
            siddhiAppRuntime.shutdown();
        }
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
