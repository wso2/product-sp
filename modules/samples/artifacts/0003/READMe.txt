1. Copy {WSO2DASHome}/samples/0003/LoginAnalysisPlan.siddhi file to {WSO2DASHome}/deployment/siddhi-files

2. Start the worker using ./{WSO2DASHome}/bin/worker.sh

3. Run following curls commands to send some login events

curl -X POST \
  http://localhost:9090/simulation/single \
  -H 'content-type: text/plain' \
  -d '{
  "siddhiAppName": "LoginAnalysisPlan",
  "streamName": "UserLoginStream",
  "timestamp": null,
  "data": [
    "suho",
    "developer",
    "02:00 23/04/2017"
  ]
}'

curl -X POST \
  http://localhost:9090/simulation/single \
  -H 'content-type: text/plain' \
  -d '{
  "siddhiAppName": "LoginAnalysisPlan",
  "streamName": "UserLoginStream",
  "timestamp": null,
  "data": [
    "mohan",
   "developer",
   "02:12 23/04/2017"
  ]
}'

4. Run following command to check the last login time of the user,
   Results should be printed as logs from #log() function

curl -X POST \
  http://localhost:9090/simulation/single \
  -H 'content-type: text/plain' \
  -d '{
  "siddhiAppName": "LoginAnalysisPlan",
  "streamName": "UserLoginCheckStream",
  "timestamp": null,
  "data": [
    "suho"
  ]
}'