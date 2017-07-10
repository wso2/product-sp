Purpose of this sample is to test custom extension loading to SP 4.0.0

1. Copy {WSO2SPHome}/samples/artifacts/0028/execution-geo-sample.siddhi file to {WSO2SPHome}/deployment/siddhi-files

2. Start the worker using ./{WSO2SPHome}/bin/worker.sh

3. Run following curls commands to send some login events

curl -X POST \
  http://localhost:9090/simulation/single \
  -H 'content-type: text/plain' \
  -d '{
  "siddhiAppName": "execution-geo-sample",
  "streamName": "geocodeStream",
  "timestamp": null,
  "data": [
    "5 Avenue Anatole France",
    "75007 Paris",
    "France"
  ]
}'
