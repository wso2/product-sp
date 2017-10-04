[1] Copy {WSO2SPHome}/samples/artifacts/0050/hbase-store-test-plan.siddhi file to {WSO2_SP_Home}/wso2/worker/deployment/siddhi-files/.

[3] The OSGi-fied shaded HBase client and Apache HTrace core bundles should be added to the WSO2 SP classpath:
  [3.1] Download the following files:
    [3.1.1] HBase shaded client: http://central.maven.org/maven2/org/apache/hbase/hbase-shaded-client/1.3.1/hbase-shaded-client-1.3.1.jar
    [3.1.2] Apache HTrace core: http://central.maven.org/maven2/org/apache/htrace/htrace-core/3.1.0-incubating/htrace-core-3.1.0-incubating.jar
  [3.2] Use the "jartobundle" tool in {WSO2_SP_Home}/bin to convert the above jars into OSGi bundles.
  [3.3] Copy over the converted bundles to the WSO2 SP classpath.

[4] Navigate to {WSO2SPHome}/bin and start the server using ./worker.sh
[5] Navigate to {WSO2SPHome}/samples/sample-clients/tcp-client and run the "ant" command without arguments to populate the HBase store with data.
[6] Navigate to {WSO2SPHome}/samples/sample-clients/tcp-client and run the "ant" command with the "storeTest" argument (e.g. "ant storeTest") to send events to the receptor stream and simulate reading from the store.