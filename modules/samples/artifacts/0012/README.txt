1. Copy {WSO2DASHome}/samples/artifacts/0012/jms-sample.siddhi file to {WSO2DASHome}/deployment/siddhi-files
2. Copy following libs in {ActiveMQHome}/lib to {WSO2DASHome}/lib
    * hawtbuf-1.9.jar
    * geronimo-j2ee-management_1.1_spec-1.0.1.jar
4. Navigate to {ActiveMQHome}/bin and start ActiveMQ using ./activemq start
5. Navigate to {WSO2DASHome}/bin and start using ./worker.sh
6. Publish below text message to DAS_JMS_TEST topic using ActiveMQ web console.
    <events>
        <event>
            <symbol>wso2</symbol>
            <price>10</price>
            <volume>100</volume>
        </event>
    </events>
7. Result will be published to DAS_JMS_OUTPUT_TEST queue in broker. You can observe it via ActiveMQ web console.