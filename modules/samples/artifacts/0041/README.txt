[1] Install the rabbitmq server as mentioned in https://www.rabbitmq.com/install-debian.html
[2] Copy {WSO2SPHome}/samples/artifacts/0041/rabbitmq-sample.siddhi file to {WSO2_SP_Home}/wso2/worker/deployment/siddhi-files/.
[3] Navigate to {WSO2SPHome}/bin and start the server using ./worker.sh
[4] Publish below text message to RABBITMQ_SAMPLE exchange in the RabbitMQ broker.
    <events>
        <event>
            <symbol>wso2</symbol>
            <price>10</price>
            <volume>100</volume>
        </event>
    </events>
[4] You can observe the result from the exchange RABBITMQ_SAMPLE in the RabbitMQ broker.