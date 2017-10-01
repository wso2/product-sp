1. Copy {WSO2SPHome}/samples/artifacts/0012/jms-sample.siddhi file to {WSO2_SP_Home}/wso2/worker/deployment/siddhi-files/

2. Copy following libs in {ActiveMQHome}/lib to {WSO2SPHome}/lib
    * hawtbuf-1.9.jar
    * geronimo-j2ee-management_1.1_spec-1.0.1.jar

3. Navigate to {ActiveMQHome}/bin and start ActiveMQ using ./activemq start

4. Navigate to {WSO2SPHome}/bin and start using ./worker.sh

5. Publish below text message to SP_JMS_TEST topic using ActiveMQ web console (Default address
http://localhost:8161/admin).
    <events>
        <event>
            <symbol>wso2</symbol>
            <price>10</price>
            <volume>100</volume>
        </event>
    </events>

6. Result will be published to SP_JMS_OUTPUT_TEST queue in broker. You can observe it via ActiveMQ web console.
