
===============================Prerequisites==============================
	
You need a Java Development Kit / JRE version 1.6.x or later and Apache Ant 1.7.0 or later, at a minimum, to try out the samples. Apache Ant can be downloaded from here: http://ant.apache.org. 

Please Follow below instructions to run the HTTPD Logs sample
===================================================================

1. Start the WSO2 DAS Server
2. Add below stream defintion to DAS server as explained in https://docs.wso2.com/display/CEP310/Working+with+Event+Streams
{
                      'name':'org.wso2.sample.httpd.logs', 
                      'version':'1.0.0',
                      'nickName': 'Httpd_Log_Stream', 
                      'description': 'Sample of Httpd logs', 
                      'metaData':[ 
                              {'name':'clientType','type':'STRING'} 
                      ], 
                      'payloadData':[ 
                              {'name':'log','type':'STRING'} 
                      ] 
}
3. Go to $WSO2_DAS_HOME/samples/httpd-logs directory via console
4. Type 'ant' from the console
 (This will read the access.log from the $WSO2_DAS_HOME/samples/httpd-logs/resources Directory and send each log line as event )


