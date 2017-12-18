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

package org.wso2.sp.http.client;

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
 * This is a sample HTTP client to publish events to HTTP/HTTPS endpoint.
 */
public class HttpClient {
    private static final Logger log = Logger.getLogger(HttpClient.class);

    /**
     * Main method to start the test client.
     *
     * @param args no args need to be provided
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        log.info("Initialize http client.");
        final String[] types = new String[]{"json", "xml", "text"};
        SiddhiManager siddhiManager = new SiddhiManager();
        String publisherUrl = args[0];
        String method = args[1];
        String type = Arrays.asList(types).contains(args[2]) ? args[2] : "json";
        int noOfEventsToSend = !args[7].isEmpty() ? Integer.parseInt(args[7]) : -1;
        boolean continuouslyReadFile = !args[8].isEmpty() && Boolean.parseBoolean(args[8]);
        List<String[]> fileEntriesList = null;

        boolean sendEventsCountinously = true;
        if (noOfEventsToSend != -1) {
            sendEventsCountinously = false;
        }

        if (args.length >= 4 && !args[3].equals("")) {
            String filePath = args[3];
            fileEntriesList = readFile(filePath);
        }
        String eventDefinition;
        if (args.length >= 5 && !args[4].equals("")) {
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
                    eventDefinition = "name:\"{0}\"\namount:{1}";
                }
            }
        }
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(
                "@App:name('TestExecutionPlan') " +
                        "@sink(type = 'http', publisher.url = '" + publisherUrl + "', method = '" + method + "'," +
                        "@map(type='" + type + "', @payload(\"{{message}}\")))" +
                        "define stream HttpClientStream (message string);");
        siddhiAppRuntime.start();
        InputHandler httpClientStream = siddhiAppRuntime.getInputHandler("HttpClientStream");
        String[] sweetName = {"Cupcake", "Donut", "Éclair", "Froyo", "Gingerbread", "Honeycomb", "Ice",
                "Cream Sandwich", "Jelly Bean", "KitKat", "Lollipop", "Marshmallow"};

        EventSendingUtil.publishEvents(fileEntriesList, sendEventsCountinously, noOfEventsToSend, eventDefinition,
                                       sweetName, httpClientStream, Integer.parseInt(args[5]), false,
                                       continuouslyReadFile);
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
