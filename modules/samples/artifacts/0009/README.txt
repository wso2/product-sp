[1] Install MongoDB by following the instructions found here. (https://docs.mongodb.com/manual/administration/install-community/)

[2] Create necessary data store and user with access privileges.

[3] Replace the 'mongodb.uri' parameter value in the sample with the uri pointing the newly created data store.

[4] Copy {WSO2SPHome}/samples/artifacts/0009/MongoDBStoreTestPlan.siddhi file to {WSO2_SP_Home}/wso2/worker/deployment/siddhi-files/.

[5] Navigate to {WSO2SPHome}/bin and start the server using ./worker.sh

[6] Navigate to {WSO2SPHome}/samples/sample-clients/tcp-client and run the "ant" command without arguments to populate the MongoDB store with data. The data can be viewed in the data store using the mongo bash shell.

[4] Navigate to {WSO2SPHome}/samples/sample-clients/tcp-client and run the "ant" command with the "storeTest" argument (e.g. "ant storeTest") to send events to the receptor stream and simulate reading from the store.

The events read from the store should be printed in the SP console.