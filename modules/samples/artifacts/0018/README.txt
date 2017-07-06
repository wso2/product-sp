1. Copy {WSO2DASHome}/samples/artifacts/0018/WSO2EventProcessorSiddhiApp.siddhi file to {WSO2DASHome}/deployment/siddhi-files

2. Add relevant siddhi-io-wso2event and siddhi-map-wso2event jars to the {WSO2DASHome}/lib folder if not exist

3. Navigate to {WSO2DASHome}/samples/sample-clients/wso2event-server and run ant command without arguments

2. Start the worker using ./{WSO2DASHome}/bin/worker.sh

3. Navigate to {WSO2DASHome}/samples/sample-clients/wso2event-client and run ant command without arguments

4. See the output in the test server terminal
