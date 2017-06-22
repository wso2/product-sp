1. Copy {WSO2DASHome}/samples/0020/ml-clustering-sample.siddhi file to {WSO2DASHome}/deployment/siddhi-files

2. Copy {WSO2DASHome}/samples/0020/ClusterSimulator.json file to {WSO2DASHome}/deployment/simulation-configs

3. Copy {WSO2DASHome}/samples/0020/ClusterTest.json file to {WSO2DASHome}/deployment/csv-files

4. Start the worker using ./{WSO2DASHome}/bin/worker.sh

5. Run the following curl command to simulate clustering with events in ClusterTest.csv
   curl -X POST http://localhost:9090/simulation/feed/ClusterSimulator/?action=run

6. See the output in the WSO2DAS terminal

7. To stop the simulation run the following curl command
   curl -X POST http://localhost:9090/simulation/feed/ClusterSimulator/?action=stop
