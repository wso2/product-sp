Purpose of this sample is to test custom extension loading to DAS 4.0.0

1. Copy {WSO2DASHome}/samples/artifacts/0028/gpl-execution-geo-sample.siddhi file to {WSO2DASHome}/deployment/siddhi-files

2. Start the worker using ./{WSO2DASHome}/bin/worker.sh

3. Run following curls commands to send some login events

    curl -X POST \
      http://localhost:9090/simulation/single \
      -H 'content-type: text/plain' \
      -d '{
      "siddhiAppName": "gpl-execution-geo-sample",
      "streamName": "cleanedStream",
      "timestamp": null,
      "data": [
        "8.116553",
        "77.523679",
        "9.850047",
        "98.597177"
      ]
    }'

4. See the output in the WSO2DAS terminal
