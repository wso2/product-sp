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

package org.wso2.sp.sample.rabbitmq.producer;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Test client for RabbitMQ source.
 */
public class RabbitMQProducer {
    private static final Logger log = Logger.getLogger(RabbitMQProducer.class);

    /**
     * Main method to start the test client.
     *
     * @param args no args need to be provided
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        log.info("Initialize rabbitmq receiver.");
        SiddhiManager siddhiManager = new SiddhiManager();
        /*String uri = args[0];
        String exchange = args[1];
        String type = args[2];
        int noOfEventsToSend = !args[6].isEmpty() ? Integer.parseInt(args[6]) : -1;
        List<String[]> fileEntriesList = null;*/

        String uri = "amqp://guest:guest@localhost:5672";
        String exchange = "rabbitmq_sample";
        String type = "xml";
        String events = "5";
        String filepath = "";
        String eventDefinitionsarg = "";
        int noOfEventsToSend = !events.isEmpty() ? Integer.parseInt(events) : -1;
        List<String[]> fileEntriesList = null;

        boolean sendEventsCountinously = true;
        if (noOfEventsToSend != -1) {
            sendEventsCountinously = false;
        }

        //if (!args[3].equals("")) {
        if (!filepath.equals("")) {
            String filePath = args[3];
            fileEntriesList = readFile(filePath);
        }
        String eventDefinition;
        //if (!args[4].equals("")) {
        if (!eventDefinitionsarg.equals("")) {
            //eventDefinition = args[4];
            eventDefinition = eventDefinitionsarg;
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

        InputHandler rabbitmqClientStream;

        String[] sweetName = {"Cupcake", "Donut", "Eclair", "Froyo", "Gingerbread", "Honeycomb", "Ice",
                "Cream Sandwich", "Jelly Bean", "KitKat", "Lollipop", "Marshmallow"};


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(
                "@App:name('TestExecutionPlan')\n" +
                        "@sink(type ='rabbitmq',uri = '" + uri + "', exchange.name = '" + exchange + "'," +
                        "@map(type='" + type + "'))" +
                        "define stream RabbitmqClientStream (message string);");

        siddhiAppRuntime.start();
        rabbitmqClientStream = siddhiAppRuntime.getInputHandler("RabbitmqClientStream");

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
                    rabbitmqClientStream.send(new Object[]{message});
                }
            } else {
                int amount = ThreadLocalRandom.current().nextInt(1, 10000);
                String name = sweetName[ThreadLocalRandom.current().nextInt(0, sweetName.length)];
                message = eventDefinition.replace("{0}", name).replace("{1}", Integer.toString(amount));
                rabbitmqClientStream.send(new Object[]{message});
            }
            log.info("Sent event:" + message);
            Thread.sleep(Long.parseLong(args[4]));
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

