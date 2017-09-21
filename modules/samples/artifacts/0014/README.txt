1. Copy {WSO2SPHome}/samples/artifacts/0014/stats-execution-sample.siddhi file to {WSO2_SP_Home}/wso2/worker/deployment/siddhi-files/

2. Start the worker using ./{WSO2SPHome}/bin/worker.sh

3. Run the following curl command to simulate prediction using the test-model

    curl -X POST \
      http://localhost:9090/simulation/single \
      -H 'content-type: text/plain' \
      -d '{
      "siddhiAppName": "stats-execution-sample",
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
      "siddhiAppName": "stats-execution-sample",
      "streamName": "SmartHomeData",
      "timestamp": null,
      "data": [
        "10",
        "55.6",
        "55.6",
        "20.5"
      ]
    }'

    curl -X POST \
      http://localhost:9090/simulation/single \
      -H 'content-type: text/plain' \
      -d '{
      "siddhiAppName": "stats-execution-sample",
      "streamName": "SmartHomeData",
      "timestamp": null,
      "data": [
        "10",
        "55.6",
        "55.6",
        "30.5"
      ]
    }'

4. See the output in the WSO2SP terminal
