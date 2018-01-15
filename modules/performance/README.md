Notes :
* The siddh apps that we used for the experiments can be found at the /artifacts folder.
* Extension used for the experiments can be found at /extension folder.
* Sample clients can be found at /sample clients
* You can send any number of attributes through the sample client but the first attribute should be always timestamp
  since we are using it for latency calculation

How to run
==========

1. Build the "siddhi-execution-performance" extension using "mvn clean install" command.
   * After the successful build, you can find the siddhi-execution-performance-4.0.4-SNAPSHOT.jar file with all the
     dependencies in the target folder
2. Copy the jar file to <SP_HOME>/lib folder
3. Navigate to <SP_HOME>/wso2/worker/deployment/siddhi-files
4. Save the siddhi app with .siddhi file extension
5. Navigate to <SP_HOME>/bin
6. Issue the command as ./worker.sh
    * Once WSO2 SP server is successfully started, a log similar to the following is printed in the Terminal.
        * WSO2 Stream Processor started in x sec
7. Start the TCP-Client using "ant" command
    * If the client starts successfully, the following messages appear on the terminal.
        * TCP client connected.
    * If the client sends all the events successfully, the following message appear on the terminal.
        * TCP client finished sending events