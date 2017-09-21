[1] Copy {WSO2SPHome}/samples/artifacts/0007/store-test-plan.siddhi file to {WSO2_SP_Home}/wso2/worker/deployment/siddhi-files/.

[*] The OSGi-fied H2 JDBC client should be added to the SP classpath. This can be downloaded from the WSO2 Nexus at:
http://maven.wso2.org/nexus/content/groups/wso2-public/org/wso2/orbit/com/h2database/h2/1.4.191.wso2v1/h2-1.4.191.wso2v1.jar

[2] Navigate to {WSO2SPHome}/bin and start the server using ./worker.sh
[3] Navigate to {WSO2SPHome}/samples/sample-clients/tcp-client and run the "ant" command without arguments to populate the RDBMS store with data.
[4] Navigate to {WSO2SPHome}/samples/sample-clients/tcp-client and run the "ant" command with the "storeTest" argument (e.g. "ant storeTest") to send events to the receptor stream and simulate reading from the store.
