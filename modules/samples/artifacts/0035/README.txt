1. Copy {WSO2SPHome}/samples/artifacts/0035/markov-chain-sample.siddhi file to {WSO2_SP_Home}/wso2/worker/deployment/siddhi-files/

2. Copy {WSO2SPHome}/samples/artifacts/0035/MarkovChainSimulator.json file to {WSO2_SP_Home}/wso2/worker/deployment/simulation-configs

3. Copy {WSO2SPHome}/samples/artifacts/0035/MarkovChainTest.csv file to {WSO2_SP_Home}/wso2/worker/deployment/csv-files

4. Start the worker using ./{WSO2SPHome}/bin/worker.sh

5. Run the following curl command to simulate classification with events in ClassificationTest.csv
   curl -X POST http://localhost:9090/simulation/feed/MarkovChainSimulator/?action=run
   -u admin:admin \
6. See the result output in the WSO2SP terminal.

NOTE: User credentials used in the curl commands are default values.
