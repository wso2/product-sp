package org.wso2.sp.sample.kafka.consumer;

import org.apache.kafka.clients.consumer.CommitFailedException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Test client for Kafka source
 */
public class KafkaReceiver {
    private static final Logger log = Logger.getLogger(KafkaReceiver.class);
    /**
     * Main method to start the test client
     *
     * @param args no args need to be provided
     */
    public static void main(String[] args) {
        List<TopicPartition> partitionsList = new ArrayList<>();

        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "group");
        props.put("session.timeout.ms", "30000");
        props.put("enable.auto.commit", "false");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        KafkaConsumer<byte[], byte[]> consumer  = new KafkaConsumer<>(props);
        TopicPartition partition = new TopicPartition("kafka_result_topic", 0);
        partitionsList.add(partition);

        consumer.assign(partitionsList);

        while (true) {
            ConsumerRecords<byte[], byte[]> records = consumer.poll(10);
            for (ConsumerRecord record : records) {
                String event = record.value().toString();
                if (log.isDebugEnabled()) {
                    log.info("Event received in Kafka Event Adaptor: " + event + ", offSet: " + record.offset() +
                            ", key: " + record.key() + ", topic: " + record.topic() + ", partition: " + record
                            .partition());
                }
            }
            try {
                if (!records.isEmpty()) {
                    consumer.commitSync();
                }
            } catch (CommitFailedException e) {
                log.error("Kafka commit failed for topic kafka_result_topic", e);
            }
        }
    }
}
