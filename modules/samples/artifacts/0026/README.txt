1. Copy {WSO2SPHome}/samples/artifacts/0026/mapExtensionSample.siddhi file to {WSO2SPHome}/deployment/siddhi-files.


2. Start the worker using ./{WSO2SPHome}/bin/worker.sh

3. Run the following curl command to simulate prediction using the test-model

    curl -X POST \
      http://localhost:9090/simulation/single \
      -H 'content-type: text/plain' \
      -d '{
      "siddhiAppName": "mapExtensionSample",
      "streamName": "inputStream",
      "timestamp": null,
      "data": [
        "sugar",
        "100",
        "1"
      ]
    }'

4. See the output in the WSO2SP terminal
