
===============================Prerequisites==============================
	
You need a Java Development Kit / JRE version 1.6.x or later and Apache Ant 1.7.0 or later, at a minimum, to try out the samples. Apache Ant can be downloaded from here: http://ant.apache.org. 

Please Follow below instructions to run the HTTPD Logs sample
===================================================================

1. Start the WSO2 DAS Server
2. Add below stream definition to DAS server as explained in https://docs.wso2.com/display/CEP310/Working+with+Event+Streams
{
                      'name':'org.wso2.sample.httpd.logs', 
                      'version':'1.0.0',
                      'nickName': 'Httpd_Log_Stream', 
                      'description': 'Sample of Httpd logs', 
                      'metaData':[ 
                              {'name':'clientType','type':'STRING'} 
                      ], 
                      'payloadData':[ 
                              {'name':'remoteIp','type':'STRING'},
                              {'name':'requestDate','type':'STRING'},
                              {'name':'request','type':'STRING'},
                              {'name':'httpcode','type':'STRING'},
                              {'name':'length','type':'STRING'}
                      ] 
}
3. Create a new Event Receiver configuration for the above stream. You can create it by logging to management console
and going to Main -> Event Processor -> Event Receivers and create an event receiver for wso2event type and org.wso2.sample.httpd.logs_1.0.0 stream.
Or you can copy the below xml content and paste it under DAS_HOME/repository/deployment/server/eventreceivers directory with file name 'logstream.xml'.

        <?xml version="1.0" encoding="UTF-8"?>
        <eventReceiver name="logstream" statistics="disable" trace="disable" xmlns="http://wso2.org/carbon/eventreceiver">
            <from eventAdapterType="wso2event"/>
            <mapping customMapping="disable" type="wso2event"/>
            <to streamName="org.wso2.sample.httpd.logs" version="1.0.0"/>
        </eventReceiver>

3. Go to $WSO2_DAS_HOME/samples/httpd-logs directory via console
4. Type 'ant' from the console
 (This will read the access.log from the $WSO2_DAS_HOME/samples/httpd-logs/resources Directory and send each log line as event )


