1. Copy {WSO2DASHome}/samples/0025/ml-prediction-sample.siddhi file to {WSO2DASHome}/deployment/siddhi-files.
   In ml-prediction-sample.siddhi file, replace "<CARBON-HOME>" with the absolute path to DAS home.

2. Start the worker using ./{WSO2DASHome}/bin/worker.sh

3. Run the following curl command to simulate prediction using the test-model
   curl -X POST   http://localhost:9090/simulation/single   -H 'content-type: text/plain'
   -d '{"siddhiAppName": "ml-prediction-sample",
   "streamName": "inputStream",
   "timestamp": null,
   "data": ["2", "84", "0", "0", "0", "0.0", "0.304", "21"]}'

4. See the output in the WSO2DAS terminal
