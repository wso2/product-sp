
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
package org.wso2.sp.sample.jms.client;

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
 * This is a sample JMS client to publish events to JMS endpoint.
 */
public class JmsClient {
    private static final Logger log = Logger.getLogger(JmsClient.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        log.info("Initialize jms client.");
        SiddhiManager siddhiManager = new SiddhiManager();
        String publisherUrl = args[0];
        String destination = args[1];
        String type = args[2];
        String factoryType = args[3];
        String jndiName = args[4];
        int delay = Integer.parseInt(args[9]);
        boolean sendContinuously = false;
        int noOfEvents = 0;
        if (args[7].equals("")) {
            sendContinuously = true;
        } else {
            noOfEvents = Integer.parseInt(args[7]);
        }

        List<String[]> fileEntriesList = null;
        if (!args[6].equals("")) {
            String filePath = args[6];
            fileEntriesList = readFile(filePath);
        }
        String eventDefinition = null;
        if (!args[8].equals("")) {
            eventDefinition = args[8];
        } else {
            if (!args[6].equals("")) { //for non custom mappings
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

        InputHandler jmsClientStream;
        String[] sweetName = {"Cupcake", "Donut", "Eclair", "Froyo", "Gingerbread", "Honeycomb", "Ice",
                "Cream Sandwich", "Jelly Bean", "KitKat", "Lollipop", "Marshmallow"};
        SiddhiAppRuntime siddhiAppRuntime;
        if (type.equals("keyvalue")) {
            siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(
                    "@App:name('TestExecutionPlan')\n" +
                            "@sink(type='jms',factory.initial='org.apache.activemq.jndi" +
                            ".ActiveMQInitialContextFactory',\n" +
                            "provider.url='" + publisherUrl + "',destination='" + destination + "',\n" +
                            "connection.factory.type='" + factoryType + "',\n" +
                            "connection.factory.jndi.name='" + jndiName + "',\n" +
                            "@map(type='" + type + "'))\n" +
                            "define stream jmsClientStream (name string, amount double);");
        } else {
            siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(
                    "@App:name('TestExecutionPlan')\n" +
                            "@sink(type='jms',factory.initial='org.apache.activemq.jndi" +
                            ".ActiveMQInitialContextFactory',\n" +
                            "provider.url='" + publisherUrl + "',destination='" + destination + "',\n" +
                            "connection.factory.type='" + factoryType + "',\n" +
                            "connection.factory.jndi.name='" + jndiName + "',\n" +
                            "@map(type='" + type + "',@payload(\"{{message}}\")))\n" +
                            "define stream jmsClientStream (message string);");
        }
        siddhiAppRuntime.start();
        jmsClientStream = siddhiAppRuntime.getInputHandler("jmsClientStream");
        while (sendContinuously || 0 != noOfEvents--) {
            if (type.equals("keyvalue")) {
                double amount = ThreadLocalRandom.current().nextDouble(1, 10000);
                String name = sweetName[ThreadLocalRandom.current().nextInt(0, sweetName.length)];
                log.info("JMS producer is sending name: " + name + " amount: " + amount);
                jmsClientStream.send(new Object[]{name, amount});
                Thread.sleep(delay);
            } else {
                String message;
                if (fileEntriesList != null) {
                    Iterator iterator = fileEntriesList.iterator();
                    while (iterator.hasNext()) {
                        String[] stringArray = (String[]) iterator.next();
                        message = eventDefinition;
                        for (int i = 0; i < stringArray.length; i++) {
                            message = message.replace("{" + i + "}", stringArray[i]);
                            log.info("JMS producer is sending : " + message);
                            jmsClientStream.send(new Object[]{message});
                            Thread.sleep(delay);
                        }
                    }
                } else {
                    double amount = ThreadLocalRandom.current().nextDouble(1, 10000);
                    String name = sweetName[ThreadLocalRandom.current().nextInt(0, sweetName.length)];
                    message = eventDefinition.replace("{0}", name).replace("{1}", Double.toString(amount));
                    log.info("JMS producer is sending : " + message);
                    jmsClientStream.send(new Object[]{message});
                    Thread.sleep(delay);
                }
            }
            Thread.sleep(delay);
        }
        Thread.sleep(delay);
        siddhiAppRuntime.shutdown();
        Thread.sleep(delay);
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

