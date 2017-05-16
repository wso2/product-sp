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
package org.wso2.das.sample.kafka.client;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/**
 * Test client for Kafka source
 */
public class KafkaClient {
    static Producer<String, String> producer = null;
    /**
     * Main method to start the test client
     *
     * @param args no args need to be provided
     */
    public static void main(String[] args) {
        String type = args[3];
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        producer = new KafkaProducer<>(props);

        for (int i = 0; i < 100; i++) {
            if ("text".equals(type)) {
                producer.send(new ProducerRecord<>("kafka_topic", "WSO2,55.6,100"));
                producer.send(new ProducerRecord<>("kafka_topic", "IBM,75.6,100"));
            } else if ("xmlDefault".equals(type)) {
                producer.send(new ProducerRecord<>("kafka_topic", "<events><event><symbol>WSO2" +
                        "</symbol><price>55.689</price>" +
                        "<volume>100</volume></event></events>"));
                producer.send(new ProducerRecord<>("kafka_topic", "<events><event><symbol>IBM" +
                        "</symbol><price>75</price>" +
                        "<volume>10</volume></event></events>"));
            } else if ("xmlCustom".equals(type)) {
                producer.send(new ProducerRecord<>("kafka_topic", "<portfolio " +
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
            }
        }
        producer.close();
    }
}
