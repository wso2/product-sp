===============================Prerequisites==============================
	
You need a Java Development Kit / JRE version 1.6.x or later and Apache Ant 1.7.0 or later, at a minimum, to try out the samples. Apache Ant can be downloaded from here: http://ant.apache.org. BAM analytics framework depends on Apache Hadoop and Hadoop requires Cygwin in order to run in Windows. So first make sure that you have at least installed basic and security related Cygwin packages if you are working on Windows before using BAM.

=========================How to Run KPI Sample============================

1. Start the WSO2 BAM Server
2. Go to 'WSO2_BAM_HOME/samples/mobile-web-channel-monitoring/' directory
3. Type 'ant' with in the directory from the console
    (This publishes the events to BAM)
4. Go to management console and login
5. Go to Main -> BAM Toolbox -> Add. Select 'Mobile/Web Channel Monitoring' and click on Install button
6. Go to Main -> BAM Toolbox -> List. Wait until the 'Mobile_Web_Channel_Monitoring' toolbox status changes to Installed. 
7. Wait for some time (approx 3min) until the scripts run on publish data. You can also run the script manually by clicking on the Analytics -> List and select ChannelAnalysis and click on exceute.
8. Click on Main -> BAM Dashboards -> View portal to see the dashboard for Channel Analytics.

Note :- Toolbox comes with this sample is using embedded H2 database to persist summarized data, also out of the box it only works for default bam installation. Therefore if you changed the default settings(port offset and h2 database), you have to change hive script accordingly.
