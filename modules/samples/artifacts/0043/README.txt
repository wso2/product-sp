1. Copy {WSO2SPHome}/samples/artifacts/0043/aggregation-sample.siddhi file to {WSO2_SP_Home}/wso2/worker/deployment/siddhi-files/

2. Copy {WSO2SPHome}/samples/artifacts/0043/AggregationSimulator.json file and RetrievalSimulator.json file to
   {WSO2_SP_Home}/wso2/worker/deployment/simulation-configs

3. Copy {WSO2SPHome}/samples/artifacts/0043/AggregationTest.csv file and RetrievalTest.csv file to
   {WSO2_SP_Home}/wso2/worker/deployment/csv-files

4. Start the worker using ./{WSO2SPHome}/bin/worker.sh

5. Run the following curl command to simulate aggregation with events in AggregationTest.csv
   curl -X POST http://localhost:9090/simulation/feed/AggregationSimulator/?action=run
   -u admin:admin \

6. Wait for few seconds and run the following curl command to simulate retrieval from aggregator with events
   in RetrievalTest.csv
   curl -X POST http://localhost:9090/simulation/feed/RetrievalSimulator/?action=run
   -u admin:admin \

7. See the output in the WSO2SP terminal

NOTE: User credentials used in the curl commands are default values.

