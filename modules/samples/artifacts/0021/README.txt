Purpose of this sample is to test custom extension loading to DAS 4.0.0

1. Copy {WSO2DASHome}/samples/0021/mathExtensionSample.siddhi file to {WSO2DASHome}/deployment/siddhi-files

2. Start the worker using ./{WSO2DASHome}/bin/worker.sh

3. Run following curls commands to send some login events

curl -X POST \
  http://localhost:9090/simulation/single \
  -H 'content-type: text/plain' \
  -d '{
  "siddhiAppName": "mathExtensionSample",
  "streamName": "userLoginStream",
  "timestamp": null,
  "data": [
    "suho",
    "developer",
    "02:00 23/04/2017",
    1005,
    30.3,
    true
  ]
}'

4. See the output in the WSO2DAS terminal
