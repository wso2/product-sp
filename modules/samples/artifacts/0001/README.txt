1. Copy {WSO2DASHome}/samples/0001/kafka-sample.siddhi file to {WSO2DASHome}/deployment/siddhi-files

Kafka libs to be added and converted to OSGI from {KafkaHome}/libs are as follows
    * kafka_2.10-0.9.0.1.jar
    * kafka-clients-0.9.0.1.jar
    * metrics-core-2.2.0.jar
    * scala-library-2.10.5.jar
    * zkclient-0.7.jar
    * zookeeper-3.4.6.jar

2. Add the OSGI converted kafka libs to {WSO2DASHome}/lib
3. Add the kafka libs to {WSO2DASHome}/samples/sample-clients/lib

4. Navigate to {KafkaHome} and start zookeeper node using bin/zookeeper-server-start.sh config/zookeeper.properties
5. Navigate to {KafkaHome} and start kafka server node using bin/kafka-server-start.sh config/server.properties

6. Navigate to {WSO2DASHome}/bin and start using ./carbon.sh
7. Navigate to {WSO2DASHome}/samples/sample-clients/kafka-consumer and run ant command without arguments
8. Navigate to {WSO2DASHome}/samples/sample-clients/kafka-producer and run ant command without arguments

Published values should be printed on the kafka-consumer console.