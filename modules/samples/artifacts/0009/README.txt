[1] Copy {WSO2SPHome}/samples/artifacts/0009/MongoDBStoreTestPlan.siddhi file to {WSO2_SP_Home}/wso2/worker/deployment/siddhi-files/.
[2] Navigate to {WSO2SPHome}/bin and start the server using ./worker.sh
[3] Navigate to {WSO2SPHome}/samples/sample-clients/tcp-client and run the "ant" command without arguments to populate the MongoDB store with data.
[4] Navigate to {WSO2SPHome}/samples/sample-clients/tcp-client and run the "ant" command with the "storeTest" argument (e.g. "ant storeTest") to send events to the receptor stream and simulate reading from the store.
