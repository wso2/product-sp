[1] Copy {WSO2DASHome}/samples/artifacts/0029/GplNLPFindNameEntityType.siddhi file to {WSO2DASHome}/deployment/siddhi-files.
[2] Navigate to {WSO2DASHome}/bin and start the server using ./worker.sh
[3] Run the following curl command to send an event,

curl -X POST \
  http://localhost:9090/simulation/single \
  -H 'content-type: text/plain' \
  -d '{
  "siddhiAppName": "GplNLPFindNameEntityType",
  "streamName": "InputStream",
  "timestamp": null,
  "data": [
    "Woman ARRIVING in West Africa, From Morocco Tests Positive For Ebola: By Lizzie Bennett A South"
  ]
}'