1. Copy {WSO2SPHome}/samples/artifacts/0012/jms-sample.siddhi file to {WSO2_SP_Home}/wso2/worker/deployment/siddhi-files/

2. Naviagate to {WSO2SPHome}/bin and run the following command
(This will register the InitialContextFactory implementation according to the OSGi JNDI spec.)
    ./icf-provider.(sh|bat) org.apache.activemq.jndi.ActiveMQInitialContextFactory {ActiveMQHome}/lib/activemq-client-5.14.5.jar <Output Jar Path>

3. Copy generated jar from <Output Jar Path> to {WSO2SPHome}/lib

4. Copy following libs in {ActiveMQHome}/lib to {WSO2SPHome}/lib
    * hawtbuf-1.9.jar
    * geronimo-j2ee-management_1.1_spec-1.0.1.jar

5. Navigate to {ActiveMQHome}/bin and start ActiveMQ using ./activemq start

6. Navigate to {WSO2SPHome}/bin and start using ./worker.sh

7. Publish below text message to SP_JMS_TEST topic using ActiveMQ web console (Default address
http://localhost:8161/admin).
    <events>
        <event>
            <symbol>wso2</symbol>
            <price>10</price>
            <volume>100</volume>
        </event>
    </events>

8. Result will be published to SP_JMS_OUTPUT_TEST queue in broker. You can observe it via ActiveMQ web console.
