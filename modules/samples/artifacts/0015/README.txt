1. Copy {WSO2DASHome}/samples/0015/kalmanfilter-execution-sample.siddhi file to {WSO2DASHome}/deployment/siddhi-files

2. Start the worker using ./{WSO2DASHome}/bin/worker.sh

3. Run the following curl command to simulate prediction using the test-model

    curl -X POST \
      http://localhost:9090/simulation/single \
      -H 'content-type: text/plain' \
      -d '{
      "siddhiAppName": "kalmanfilter-execution-sample",
      "streamName": "SmartHomeData",
      "timestamp": null,
      "data": [
        "10",
        "55.6",
        "55.6",
        "10.5"
      ]
    }'
    curl -X POST \
      http://localhost:9090/simulation/single \
      -H 'content-type: text/plain' \
      -d '{
      "siddhiAppName": "kalmanfilter-execution-sample",
      "streamName": "SmartHomeData",
      "timestamp": null,
      "data": [
        "10",
        "55.6",
        "55.6",
        "20.5"
      ]
    }'

4. See the output in the WSO2DAS terminal
