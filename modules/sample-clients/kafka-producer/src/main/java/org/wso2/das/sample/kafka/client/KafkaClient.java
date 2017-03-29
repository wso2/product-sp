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
            producer.send(new ProducerRecord<>("kafka_topic", "WSO2,55.6,100"));
            producer.send(new ProducerRecord<>("kafka_topic", "IBM,75.6,100"));
        }
        producer.close();
    }
}
