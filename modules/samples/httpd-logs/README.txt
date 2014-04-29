
===============================Prerequisites==============================
	
You need a Java Development Kit / JRE version 1.6.x or later and Apache Ant 1.7.0 or later, at a minimum, to try out the samples. Apache Ant can be downloaded from here: http://ant.apache.org. BAM analytics framework depends on Apache Hadoop and Hadoop requires Cygwin in order to run in Windows. So first make sure that you have at least installed basic and security related Cygwin packages if you are working on Windows before using BAM.


Please Follow below instructions to run the HTTPD Logs sample
===================================================================

1. Start the WSO2 BAM Server
2. Go to $WSO2_BAM_HOME/samples/httpd-logs directory via console
3. Type 'ant' from the console
 (This will read the access.log from the $WSO2_BAM_HOME/samples/httpd-logs/resources Directory and send each log line as event )
4. Go to Management console of WSO2 BAM server.
5. Go to Main -> BAM ToolBox -> Add. Select "HTTPD Logs Analysis Toolbox" and click on Install button.
6. Go to Main -> BAM Toolbox -> List. Wait until the 'HTTPD Logs Analysis Toolbox' toolbox status changes to Installed.
7. Wait for some time, until script complete the first run after publishing the data (Script will run in each minute).
8. Click on Main -> Gadgets -> View portal to see the populated gadgets which shows a summary of no of requests coming from different countries.

Note :- Toolbox comes with this sample is using embedded H2 database to persist summarized data, also out of the box it only works for default bam installation. Therefore if you changed the default settings(port offset and h2 database), you have to change hive script accordingly. 

