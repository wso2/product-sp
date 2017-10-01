---------------------------------------------
To Test KMeansMiniBatch
---------------------------------------------
1. Copy {WSO2SPHome}/samples/artifacts/0044/steaming-kmeans-minibatch-sample.siddhi file to {WSO2_SP_Home}/wso2/worker/deployment/siddhi-files/

2. Copy {WSO2SPHome}/samples/artifacts/0044/kmeans-mini-batch-test.json file to {WSO2_SP_Home}/wso2/worker/deployment/simulation-configs

3. Copy {WSO2SPHome}/samples/artifacts/0044/testKMeansMiniBatch.csv file to {WSO2_SP_Home}/wso2/worker/deployment/csv-files.

4. Start the worker using ./{WSO2SPHome}/bin/worker.sh

5. Run the following curl command to simulate KMeansMiniBatch with events in testKMeansMiniBatch.csv
   curl -X POST http://localhost:9090/simulation/feed/kmeans-mini-batch-test/?action=run

6. See the output in the WSO2SP terminal

---------------------------------------------
To Test KMeansIncremental
---------------------------------------------
1. Copy {WSO2SPHome}/samples/artifacts/0044/steaming-kmeans-incremental-sample.siddhi file to {WSO2_SP_Home}/wso2/worker/deployment/siddhi-files/

2. Copy {WSO2SPHome}/samples/artifacts/0044/kmeans-incremental-test.json file to {WSO2_SP_Home}/wso2/worker/deployment/simulation-configs

3. Copy {WSO2SPHome}/samples/artifacts/0044/testKMeansIncremental.csv file to {WSO2_SP_Home}/wso2/worker/deployment/csv-files.

4. Start the worker using ./{WSO2SPHome}/bin/worker.sh

5. Run the following curl command to simulate KMeansMiniBatch with events in testKMeansMiniBatch.csv
   curl -X POST http://localhost:9090/simulation/feed/kmeans-incremental-test/?action=run

6. See the output in the WSO2SP terminal
