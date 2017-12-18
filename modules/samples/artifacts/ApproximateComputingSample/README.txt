---------------------------------------------
To Test distinctCountEver
---------------------------------------------
1. Copy {WSO2SPHome}/samples/artifacts/0051/approximate-distinctCountEver-sample.siddhi file to {WSO2_SP_Home}/wso2/worker/deployment/siddhi-files/

2. Copy {WSO2SPHome}/samples/artifacts/0051/Test-approximate-distinctCountEver.json file to {WSO2_SP_Home}/wso2/worker/deployment/simulation-configs

3. Copy {WSO2SPHome}/samples/artifacts/0051/approximate-distinctCountEver.csv file to {WSO2_SP_Home}/wso2/worker/deployment/csv-files.

4. Start the worker using ./{WSO2SPHome}/bin/worker.sh

5. Run the following curl command to simulate distinctCountEver with events in approximate-distinctCountEver.csv
   curl -X POST http://localhost:9090/simulation/feed/Test-approximate-distinctCountEver/?action=run
   -u admin:admin \

6. See the output in the WSO2SP terminal

---------------------------------------------
To Test count
---------------------------------------------
1. Copy {WSO2SPHome}/samples/artifacts/0051/approximate-count-sample.siddhi file to {WSO2_SP_Home}/wso2/worker/deployment/siddhi-files/

2. Copy {WSO2SPHome}/samples/artifacts/0051/Test-approximate-count.json file to {WSO2_SP_Home}/wso2/worker/deployment/simulation-configs

3. Copy {WSO2SPHome}/samples/artifacts/0051/approximate-count.csv file to {WSO2_SP_Home}/wso2/worker/deployment/csv-files.

4. Start the worker using ./{WSO2SPHome}/bin/worker.sh

5. Run the following curl command to simulate KMeansMiniBatch with events in testKMeansMiniBatch.csv
   curl -X POST http://localhost:9090/simulation/feed/Test-approximate-count/?action=run
   -u admin:admin \

6. See the output in the WSO2SP terminal

NOTE: User credentials used in the curl commands are default values.

