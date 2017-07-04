1. Copy {WSO2DASHome}/samples/artifacts/0026/mapExtensionSample.siddhi file to {WSO2DASHome}/deployment/siddhi-files.
   In mapExtensionSample.siddhi file, replace "<CARBON-HOME>" with the absolute path to DAS home.

2. Start the worker using ./{WSO2DASHome}/bin/worker.sh

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

4. See the output in the WSO2DAS terminal
