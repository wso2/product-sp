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
        
Note:
 Performance is measured from start, inorder to repeat another test, the WSO2 Stream Processor runtime should be restarted. 

How to build performance extension
==================================

1. Navigate to modules/performance/extension/siddhi-execution-performance/component
2. Build using "mvn clean install" command.
    * After the successful build, you can find the jar file with all the
     dependencies inside siddhi-execution-performance/component/target folder
3. Convert that jar to OSGI bundled jar using <SP_HOME>/bin/jartobundle.sh <source><destination> 
    * After the successful build, you can find the OSGI bundled jar file inside 
      destination folder
4. Copy the converted jar file in <destination> to <SP_HOME>/lib folder
5. Copy the original jar file to <SP_HOME>/samples/sample-clients/lib folder.
6. Start WSO2 SP server and run your siddhi app with the performance extension.
7. Navigate to <SP_HOME>/wso2/worker/performance-results directory.
   You can see the performance results of your siddhi app as log files there

How to use the Parameters for Performance Extension
===================================================

When calling the extension only at once in the application parameters of iijtimestamp and type are mandatory. The parameters windowSize and ID are optional.

Eg-:

1. The below stream uses the default windowSize and id of 1000 and “1” respectively.

From inputStream#throughput:throughput(iijtimestamp,"throughput")
select ip, totalAccessCount, (unauthorizedCount + forbiddenCount)/totalAccessCount as accessPercentage
insert into outputStream;

2. The below stream will  use the windowSize of 120 and the default id of ‘1’ .

From inputStream#throughput:throughput(iijtimestamp,"throughput",120)
select ip, totalAccessCount, (unauthorizedCount + forbiddenCount)/totalAccessCount as accessPercentage
insert into outputStream;

3. The below stream will  use the windowSize of 120 and the id of “mid” .

From inputStream#throughput:throughput(iijtimestamp,"throughput"120,”mid”)
select ip, totalAccessCount, (unauthorizedCount + forbiddenCount)/totalAccessCount as accessPercentage
insert into outputStream;


If the extension is used more than once in the Siddhi application then you need to use  all the parameters to enable collecting performance results to different results files.

Eg-:

Here the metrics of first extension call will be written to  a file named “output-$siddhiAppname-call1-$sequenceNumber” and the second extension call will be written to a file named “output-$siddhiAppname-call2-$sequenceNumber” .

From inputStream#throughput:throughput(iijtimestamp,"throughput"120,”call1”)
select ip, totalAccessCount, (unauthorizedCount + forbiddenCount)/totalAccessCount as accessPercentage
insert into outputStream;

From inputStream#throughput:throughput(iijtimestamp,"throughput"120,”call2”)
select ip
insert into outputStream;

If the id is not provided as a parameter in this scenario then the metrics of both the extension calls will be written to the same file of  “output-$SiddhiAppname-1-$sequenceNumber” .



