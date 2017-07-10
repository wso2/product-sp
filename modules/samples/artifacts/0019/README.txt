[1] Replace '<CARBON_HOME>' in PmmlModelProcessor.siddhi with the absolute path of the SP instance.
[2] Copy {WSO2SPHome}/samples/artifacts/0019/PmmlModelProcessor.siddhi file to {WSO2SPHome}/deployment/siddhi-files.
[3] Navigate to {WSO2SPHome}/bin and start the server using ./worker.sh
[4] Run the following curl command to send an event,

curl -X POST \
  http://localhost:9090/simulation/single \
  -H 'content-type: text/plain' \
  -d '{
  "siddhiAppName": "PmmlModelProcessor",
  "streamName": "InputStream",
  "timestamp": null,
  "data": [
    6, 148, 72, 35, 0, 33.6, 0.627, 50, 1, 2, 3, 4, 5
  ]
}'

