[1] Copy {WSO2SPHome}/samples/artifacts/0042/mqtt-sample.siddhi file to {WSO2_SP_Home}/wso2/worker/deployment/siddhi-files/
[2] Before run MQTT samples, set up mosquitto server which support mqtt.(https://mosquitto.org/download/)
[3] After the set up ,start the mosquitto server by running the following command
        mosquitto or sudo service mosquitto status
[4] Navigate to {WSO2SPHome}/bin and start the server using ./worker.sh
[5] Start the command line subscriber in a separate terminal using the following command:
        mosquitto_sub -t 'mqtt_topic_output'
[6] Publish test message with the command line publisher
        mosquitto_pub -t 'mqtt_topic_input' -m
        '<events><event><id>a</id><value>2</value><property>true</property><plugId>3</plugId><householdId>4</householdId><houseId>5</houseId><currentTime>11:23</currentTime></event></events>'
[7] Check the output in subscriber terminal
        You can assert the output on the log stream as:
        <events><event><houseId>5</houseId><maxVal>2.0</maxVal><minVal>2.0</minVal><avgVal>2.0</avgVal></event></events>

