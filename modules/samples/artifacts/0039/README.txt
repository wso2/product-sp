1. Copy {WSO2SPHome}/samples/artifacts/0039/online-perceptron-sample.siddhi file to {WSO2SPHome}/deployment/siddhi-files

2. Copy {WSO2SPHome}/samples/artifacts/0038/trainPerceptron.csv and testPerceptron.csv file to {WSO2SPHome}/deployment/csv-files.

3. Start the worker using ./{WSO2SPHome}/bin/worker.sh

4. Run the following curl command to simulate Binary Classification with events in perceptron.csv and testPerceptron.csv
   curl -X POST http://localhost:9090/simulation/feed/ClassifyTrain/?action=run

    curl -X POST \
    http://localhost:9090/simulation/single \
    -H 'cache-control: no-cache' \
    -H 'content-type: text/plain' \
    -H 'postman-token: 9429de40-429a-96c1-1980-4e0eedcafd7f' \
    -d '{
    "streamName": "StreamTrain",
    "siddhiAppName": "online-perceptron-sample",
    "timestamp": null,
    "data": [
      "0.83",
      "0.79",
      "0.02",
      "0.122",
      "false"
    ]
   }'

   curl -X POST \
    http://localhost:9090/simulation/single \
    -H 'cache-control: no-cache' \
    -H 'content-type: text/plain' \
    -H 'postman-token: 9429de40-429a-96c1-1980-4e0eedcafd7f' \
    -d '{
    "streamName": "StreamA",
    "siddhiAppName": "online-perceptron-sample",
    "timestamp": null,
    "data": [
          "0.83",
           "0.79",
           "0.02",
           "0.122"
        ]
   }'

    curl -X POST \
    http://localhost:9090/simulation/single \
    -H 'cache-control: no-cache' \
    -H 'content-type: text/plain' \
    -H 'postman-token: 9429de40-429a-96c1-1980-4e0eedcafd7f' \
    -d '{
    "streamName": "StreamTrain",
    "siddhiAppName": "online-perceptron-sample",
    "timestamp": null,
    "data": [
        "0.83",
         "0.79",
        "0.02",
        "0.122",
        "true"
       ]
      }'

      curl -X POST \
       http://localhost:9090/simulation/single \
       -H 'cache-control: no-cache' \
       -H 'content-type: text/plain' \
       -H 'postman-token: 9429de40-429a-96c1-1980-4e0eedcafd7f' \
       -d '{
       "streamName": "StreamA",
       "siddhiAppName": "online-perceptron-sample",
        "timestamp": null,
       "data": [
              "0.83",
             "0.79",
               "0.02",
              "0.122"
             ]
            }'

   curl -X POST http://localhost:9090/simulation/feed/ClassifyTest/?action=run

5. See the output in the WSO2 SP terminal
