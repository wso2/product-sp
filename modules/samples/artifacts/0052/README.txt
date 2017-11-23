Purpose of this sample is to test custom extension loading to SP 4.0.0

1. Copy {WSO2SPHome}/samples/artifacts/0052/largest-connected-component.siddhi file to {WSO2_SP_Home}/wso2/worker/deployment/siddhi-files/

2. Start the worker using ./{WSO2SPHome}/bin/worker.sh

3. Run following curls commands to send some events

curl -X POST \
  http://localhost:9090/simulation/single \
  -u admin:admin \
  -H 'content-type: text/plain' \
  -d '{
  "siddhiAppName": "largest-connected-component",
  "streamName": "cseEventStream",
  "timestamp": null,
  "data": [
    "v1",
    "v2"
  ]
}'

curl -X POST \
  http://localhost:9090/simulation/single \
  -u admin:admin \
  -H 'content-type: text/plain' \
  -d '{
  "siddhiAppName": "largest-connected-component",
  "streamName": "cseEventStream",
  "timestamp": null,
  "data": [
    "v2",
    "v3"
  ]
}'

4. See the output in the WSO2SP terminal

NOTE: User credentials used in the curl commands are default values.
