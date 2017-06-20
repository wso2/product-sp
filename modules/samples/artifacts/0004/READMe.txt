1. Copy {WSO2DASHome}/samples/0003/RoundRobinPlan.siddhi file to {WSO2DASHome}/deployment/siddhi-files

2. Run two tcp server clients on two terminals with follow commands at {WSO2DASHome}/samples/sample-clients/tcp-server
   To receive events in round robin manner

ant -Dport=8081

ant -Dport=8082

3. Start the worker using ./{WSO2DASHome}/bin/worker.sh

4. Run following curls command multiple times to send some events to there server

curl -X POST   http://localhost:9090/simulation/single   -H 'content-type: text/plain'   -d '{
  "siddhiAppName": "RoundRobinPlan",
  "streamName": "UsageInputStream",
  "timestamp": null,
  "data": [
    "1",
    "100",
    "10",
    "5"
  ]
}'