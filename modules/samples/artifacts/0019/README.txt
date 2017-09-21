[1] Build the siddhi-gpl-execution-pmml from the /github.com/wso2-extensions and copy the jar into {WSO2SPHome}/lib
[2] Replace '<CARBON_HOME>' in PmmlModelProcessor.siddhi with the absolute path of the SP instance.
[3] Copy {WSO2SPHome}/samples/artifacts/0019/PmmlModelProcessor.siddhi file to {WSO2SPHome}/wso2/worker/deployment/siddhi-files.
[4] Navigate to {WSO2SPHome}/bin and start the server using ./worker.sh
[5] Run the following curl command to send an event,

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

