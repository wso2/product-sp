
===============================Prerequisites==============================
	
You need a Java Development Kit / JRE version 1.7.x or later and Apache Ant 1.7.0 or later, at a minimum, to try out the samples. Apache Ant can be downloaded from here: http://ant.apache.org.

Please Follow below instructions to run the HTTPD Logs sample
===================================================================

1. Please copy the UDF library which is required for this sample from DAS_HOME/samples/udfs/org.wso2.das.samples.geoip-<version>.jar to
DAS_HOME/repository/components/dropins directory.

2. Add below entry to the DAS_HOME/repository/conf/analytics/spark/spark-udf-config.xml

<udf-configuration>
    <custom-udf-classes>
    .........
        <class-name>org.wso2.das.samples.geoip.IPCountryNameUDF</class-name>
        <class-name>org.wso2.das.samples.geoip.IPCountryCodeUDF</class-name>
    .........
     </custom-udf-classes>
 </udf-configuration>

3. Start the WSO2 DAS Server.
4. Go to Main -> Carbon Applications -> Add in DAS management console and upload the .car file located in DAS_HOME/samples/capps/Httpd_Log_Analytics.car
5. Go to $WSO2_DAS_HOME/samples/httpd-logs directory via console
6. Type 'ant' from the console
 (This will read the access.log from the $WSO2_DAS_HOME/samples/httpd-logs/resources Directory and send each log line as event )
7. You will see the spark script is executing in the beginning of every minute.
8.And after one successful execution of spark script, you can check the dashbord. Go to Main -> Dashboard -> Analytics Dashboard  and then login.
9. Go to Dashboards -> HTTPD Log Analysis Dashboard and click on view.


