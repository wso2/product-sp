Purpose of this sample is to test custom extension loading to SP 4.0.0

1. Copy {WSO2SPHome}/samples/artifacts/0016/stringExtensionSample.siddhi file to {WSO2_SP_Home}/wso2/worker/deployment/siddhi-files/

2. Start the worker using ./{WSO2SPHome}/bin/worker.sh

3. Run following curls commands to send some login events

curl -X POST \
  http://localhost:9090/simulation/single \
  -H 'content-type: text/plain' \
  -d '{
  "siddhiAppName": "stringExtensionSample",
  "streamName": "userLoginStream",
  "timestamp": null,
  "data": [
    "SUHO",
    "developer",
    "02:00 23/04/2017",
    1005,
    30.3,
    true
  ]
}'

curl -X POST \
  http://localhost:9090/simulation/single \
  -H 'content-type: text/plain' \
  -d '{
  "siddhiAppName": "stringExtensionSample",
  "streamName": "userLoginStream",
  "timestamp": null,
  "data": [
    "MOHAN",
   "developer",
   "02:12 23/04/2017",
   1006,
   29.2,
   true
  ]
}'
