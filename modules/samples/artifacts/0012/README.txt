1. Copy {WSO2SPHome}/samples/artifacts/0012/jms-sample.siddhi file to {WSO2_SP_Home}/wso2/worker/deployment/siddhi-files/

2. Download activemq-client-5.x.x.jar (http://central.maven.org/maven2/org/apache/activemq/activemq-client/5.9.0/activemq-client-5.9.0.jar)

3. Naviagate to {WSO2SPHome}/bin and run the following command
(This will register the InitialContextFactory implementation according to the OSGi JNDI spec.)
    ./icf-provider.(sh|bat) org.apache.activemq.jndi.ActiveMQInitialContextFactory <Downloaded Jar Path> <Output Jar Path>

4. Copy generated jar from <Output Jar Path> to {WSO2SPHome}/lib

5. Copy following libs in {ActiveMQHome}/lib to {WSO2SPHome}/lib
    * hawtbuf-1.9.jar
    * geronimo-j2ee-management_1.1_spec-1.0.1.jar
    * geronimo-jms_1.1_spec-1.1.1.jar

6. Navigate to {ActiveMQHome}/bin and start ActiveMQ using ./activemq start

7. Navigate to {WSO2SPHome}/bin and start using ./worker.sh

8. Publish below text message to SP_JMS_TEST topic using ActiveMQ web console (Default address
http://localhost:8161/admin).
    <events>
        <event>
            <symbol>wso2</symbol>
            <price>10</price>
            <volume>100</volume>
        </event>
    </events>

9. Result will be published to SP_JMS_OUTPUT_TEST queue in broker. You can observe it via ActiveMQ web console.
