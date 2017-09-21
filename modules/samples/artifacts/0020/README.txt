1. Copy {WSO2SPHome}/samples/artifacts/0020/ml-clustering-sample.siddhi file to {WSO2_SP_Home}/wso2/worker/deployment/siddhi-files/

2. Copy {WSO2SPHome}/samples/artifacts/0020/ClusterSimulator.json file to {WSO2_SP_Home}/wso2/worker/deployment/simulation-configs

3. Copy {WSO2SPHome}/samples/artifacts/0020/ClusterTest.csv file to {WSO2_SP_Home}/wso2/worker/deployment/csv-files

4. Start the worker using ./{WSO2SPHome}/bin/worker.sh

5. Run the following curl command to simulate clustering with events in ClusterTest.csv
   curl -X POST http://localhost:9090/simulation/feed/ClusterSimulator/?action=run

6. See the output in the WSO2SP terminal

