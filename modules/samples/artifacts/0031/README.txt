
1. Copy {WSO2SPHome}/samples/artifacts/0031/script-js-sample.siddhi file to {WSO2_SP_Home}/wso2/worker/deployment/siddhi-files/.


2. Start the worker using ./{WSO2SPHome}/bin/worker.sh

3. Run the following curl command to simulate prediction using the test-model

    curl -X POST \
      http://localhost:9090/simulation/single \
      -u admin:admin \
      -H 'content-type: text/plain' \
      -d '{
      "siddhiAppName": "script-js-sample",
      "streamName": "cseEventStream",
      "timestamp": null,
      "data": [
        "sugar",
        "100",
        "1"
      ]
    }'

4. See the output in the WSO2SP terminal

NOTE: User credentials used in the curl commands are default values.
