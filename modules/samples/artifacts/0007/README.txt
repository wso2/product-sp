1. Copy {WSO2DASHome}/samples/0007/store-test-plan.siddhi file to {WSO2DASHome}/deployment/siddhi-files

The MySQL JDBC client should be added to the DAS classpath.

2. Navigate to {WSO2DASHome}/bin and start using ./carbon.sh
3. Navigate to {WSO2DASHome}/samples/sample-clients/tcp-client and run ant command without arguments to populate the RDBMS store with data
4. Navigate to {WSO2DASHome}/samples/sample-clientstcp-client and run ant command with the "storeTest" arguments to simulate reading from the store.
