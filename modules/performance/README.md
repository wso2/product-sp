Notes :
* The siddh apps that we used for the experiments can be found at the /artifacts folder.
* Extension used for the experiments can be found at /extension folder.
* Sample clients can be found at /sample clients
* You can send any number of attributes through the sample client but the first attribute should be always timestamp
  since we are using it for latency calculation

How to run
==========

1. Navigate to modules/performance
2. Change all the pom file parent versions manually according to product-sp version
3. Build product-sp using "mvn clean install" command.
   * After the successful build, you can find the jar file with all the
     dependencies inside siddhi-execution-performance target folder
4. Copy the jar file to <SP_HOME>/lib folder
5. Navigate to <SP_HOME>/wso2/worker/deployment/siddhi-files
6. Save the siddhi app with .siddhi file extension
    * siddhi applications that are used for the tests can be found inside modules/performance/artifacts
7. Sample client that are used for the tests can be found inside modules/performance/sample-clients
8. Copy that TCP Client to <SP_HOME>/samples
9. Navigate to <SP_HOME>/bin
10. Issue the command as ./worker.sh
    * Once WSO2 SP server is successfully started, a log similar to the following is printed in the Terminal.
        * WSO2 Stream Processor started in x sec
11. Start the TCP-Client using "ant" command
    * If the client starts successfully, the following messages appear on the terminal.
        * TCP client connected.
    * If the client sends all the events successfully, the following message appear on the terminal.
        * TCP client finished sending events