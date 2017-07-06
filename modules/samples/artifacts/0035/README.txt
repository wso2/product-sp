1. Copy {WSO2DASHome}/samples/artifacts/0035/markov-chain-sample.siddhi file to {WSO2DASHome}/deployment/siddhi-files

2. Copy {WSO2DASHome}/samples/artifacts/0035/MarkovChainSimulator.json file to {WSO2DASHome}/deployment/simulation-configs

3. Copy {WSO2DASHome}/samples/artifacts/0035/MarkovChainTest.csv file to {WSO2DASHome}/deployment/csv-files

4. Start the worker using ./{WSO2DASHome}/bin/worker.sh

5. Run the following curl command to simulate classification with events in ClassificationTest.csv
   curl -X POST http://localhost:9090/simulation/feed/MarkovChainSimulator/?action=run

6. See the result output in the WSO2DAS terminal.
