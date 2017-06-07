[1] Download the solr-6.x.x distribution from https://archive.apache.org/dist/lucene/solr/
[2] Start the Solr server in cloud mode using the command ./bin/solr -e cloud. This will create a simple solr cloud in your local machine
[2] Copy {WSO2DASHome}/samples/artifacts/0011/store-solr-test-plan.siddhi file to {WSO2DASHome}/deployment/siddhi-files.
[3] Navigate to {WSO2DASHome}/bin and start the server using ./carbon.sh
[4] Navigate to {WSO2DASHome}/samples/sample-clients/tcp-client and run the "ant" command without arguments to populate the SOLR store with data.
[5] Navigate to {WSO2DASHome}/samples/sample-clients/tcp-client and run the "ant" command with the "storeTest" argument (e.g. "ant storeTest") to send events to the receptor stream and simulate reading from the store.