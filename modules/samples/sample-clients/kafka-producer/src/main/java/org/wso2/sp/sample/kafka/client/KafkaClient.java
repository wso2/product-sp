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
package org.wso2.sp.sample.kafka.client;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Test client for Kafka source
 */
public class KafkaClient {
    static Producer<String, String> producer = null;
    static String sampleFilPath =
            ".." + File.separator + ".." + File.separator + "artifacts" + File.separator + "sampleNumber" + File
                    .separator;
    static String fileExtension = ".txt";
    private static List<String> messagesList = new ArrayList<String>();
    private static BufferedReader bufferedReader = null;
    private static StringBuffer message = new StringBuffer("");
    private static final String asterixLine = "*****";
    static Logger log = Logger.getLogger(KafkaClient.class);

    /**
     * Main method to start the test client
     *
     * @param args no args need to be provided
     */
    public static void main(String[] args) {
        String broker = args[0];
        String topicName = args[1];
        String type = args[2];
        Properties props = new Properties();
        props.put("bootstrap.servers", broker);
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        log.info("Initializing producer");

        producer = new KafkaProducer<>(props);

        if (5 <= args.length && !"".equalsIgnoreCase(args[3])) {
            log.info("Producing to kafka topic: " + topicName);
            String sampleNumber = args[3];
            String fileName = args[4];
            String partitionNo = null;
            if (6 == args.length && !"".equalsIgnoreCase(args[5])) {
                partitionNo = args[5];
            }
            try {
                sampleNumber = getMessageFilePath(sampleNumber, fileName);
                readMsg(sampleNumber);
                for (String message : messagesList) {
                    if (null == partitionNo) {
                        log.info("Sending " + message + " on topic: " + topicName);
                        producer.send(new ProducerRecord<>(topicName, message));
                    } else {
                        log.info("Sending " + message + " on topic: " + topicName + " to partition: " + partitionNo);
                        producer.send(new ProducerRecord<>(topicName, partitionNo, message));
                    }
                }
                Thread.sleep(1000);
                log.info("Kafka client finished sending events");
            } catch (Exception e) {
                log.error("Error when sending the messages", e);
            }

        } else {
            for (int i = 0; i < 100; i++) {
                if ("json".equals(type)) {
                    producer.send(new ProducerRecord<>(topicName, "{\"event\": {\"symbol\": \"wso2symbol\", "
                            + "\"price\":123.123, \"volume\":100}}"));
                    producer.send(new ProducerRecord<>(topicName, "{\"event\": {\"symbol\": \"wso2symbol\", "
                            + "\"price\":123.123, \"volume\":200}}"));
                } else if ("xmlDefault".equals(type)) {
                    producer.send(new ProducerRecord<>(topicName, "<events><event><symbol>WSO2" +
                            "</symbol><price>55.689</price>" +
                            "<volume>100</volume></event></events>"));
                    producer.send(new ProducerRecord<>(topicName, "<events><event><symbol>IBM" +
                            "</symbol><price>75</price>" +
                            "<volume>10</volume></event></events>"));
                    log.info("Sending message on topic: " + topicName);
                } else if ("xmlCustom".equals(type)) {
                    producer.send(new ProducerRecord<>(topicName, "<portfolio " +
                            "xmlns:dt=\"urn:schemas-microsoft-com:datatypes\">" +
                            "  <stock exchange=\"nasdaq\">" +
                            "    <volume>100</volume>" +
                            "    <symbol>WSO2</symbol>" +
                            "    <price dt:dt=\"number\">55.6</price>" +
                            "  </stock>" +
                            "  <stock exchange=\"nyse\">" +
                            "    <volume>200</volume>" +
                            "    <symbol>IBM</symbol>" +
                            "    <price dt:dt=\"number\">75.6</price>" +
                            "  </stock>" +
                            "</portfolio>"));
                    log.info("Sending message on topic: " + topicName);
                }
            }
        }
        producer.close();
    }

    /**
     * File path will be created with sample number given with the file name.
     *
     * @param sampleNumber Number of the sample
     * @param fileName     name of the file with events
     */
    private static String getMessageFilePath(String sampleNumber, String fileName) throws Exception {
        String resultingFilePath = sampleFilPath.replace("sampleNumber", sampleNumber) + fileName + fileExtension;
        File file = new File(resultingFilePath);
        log.info("AVSOLUTE: " + file.getAbsolutePath());
        if (!file.isFile()) {
            throw new Exception("'" + resultingFilePath + "' is not a file");
        }
        if (!file.exists()) {
            throw new Exception("file '" + resultingFilePath + "' does not exist");
        }
        return resultingFilePath;
    }

    /**
     * messages will be read from the given filepath and stored in the array list (messagesList)
     *
     * @param filePath Text file to be read
     */
    private static void readMsg(String filePath) {
        try {
            String line;
            bufferedReader = new BufferedReader(new FileReader(filePath));
            while ((line = bufferedReader.readLine()) != null) {
                if ((line.equals(asterixLine.trim()) && !"".equals(message.toString().trim()))) {
                    messagesList.add(message.toString());
                    message = new StringBuffer("");
                } else {
                    message = message.append(String.format("\n%s", line));
                }
            }
            if (!"".equals(message.toString().trim())) {
                messagesList.add(message.toString());
            }
        } catch (FileNotFoundException e) {
            log.error("Error in reading file " + filePath, e);
        } catch (IOException e) {
            log.error("Error in reading file " + filePath, e);
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                log.error("Error occurred when closing the file : " + e.getMessage(), e);
            }
        }
    }
}
