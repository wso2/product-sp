1. Copy {WSO2SPHome}/samples/artifacts/0027/extrema-sample.siddhi file to {WSO2SPHome}/deployment/siddhi-files

2. Copy {WSO2SPHome}/samples/artifacts/0027/ExtremaSimulator.json file to {WSO2SPHome}/deployment/simulation-configs

3. Copy {WSO2SPHome}/samples/artifacts/0027/ExtremaTest.csv file to {WSO2SPHome}/deployment/csv-files

4. Start the worker using ./{WSO2SPHome}/bin/worker.sh

5. Run the following curl command to simulate regression with events in ExtremaTest.csv
   curl -X POST http://localhost:9090/simulation/feed/ExtremaSimulator/?action=run

6. See the output in the WSO2SP terminal
