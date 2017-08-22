[1] Install the rabbitmq server as mentioned in https://www.rabbitmq.com/install-debian.html
[2] Copy {WSO2SPHome}/samples/artifacts/0039/rabbitmq-sample.siddhi file to {WSO2SPHome}/deployment/siddhi-files.
[3] Navigate to {WSO2SPHome}/bin and start the server using ./worker.sh
[4] Publish below text message to rabbitmqSample exchange in the RabbitMQ broker.
    <events>
        <event>
            <symbol>wso2</symbol>
            <price>10</price>
            <volume>100</volume>
        </event>
    </events>
[4] You can observe the result from RabbitMQ broker.