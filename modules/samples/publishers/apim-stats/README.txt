
===============================Prerequisites==============================
	
You need a Java Development Kit / JRE version 1.6.x or later and Apache Ant 1.7.0 or later, at a minimum, to try out the samples. Apache Ant can be downloaded from here: http://ant.apache.org. 

Please Follow below instructions to run the APIM Statistics sample
===================================================================

1. Start the WSO2 DAS Server
2. Add below stream definition to DAS server as explained in https://docs.wso2.com/display/CEP310/Working+with+Event+Streams
{
  "name": "org.wso2.apimgt.statistics.request",
  "version": "1.0.0",
  "nickName": "API Manager Request Data",
  "description": "Request Data",
  "metaData": [
    {
      "name": "clientType",
      "type": "STRING"
    }
  ],
  "payloadData": [
    {
      "name": "consumerKey",
      "type": "STRING"
    },
    {
      "name": "context",
      "type": "STRING"
    },
    {
      "name": "api_version",
      "type": "STRING"
    },
    {
      "name": "api",
      "type": "STRING"
    },
    {
      "name": "resourcePath",
      "type": "STRING"
    },
    {
      "name": "method",
      "type": "STRING"
    },
    {
      "name": "version",
      "type": "STRING"
    },
    {
      "name": "request",
      "type": "INT"
    },
    {
      "name": "requestTime",
      "type": "LONG"
    },
    {
      "name": "userId",
      "type": "STRING"
    },
    {
      "name": "tenantDomain",
      "type": "STRING"
    },
    {
      "name": "hostName",
      "type": "STRING"
    },
    {
      "name": "apiPublisher",
      "type": "STRING"
    },
    {
      "name": "applicationName",
      "type": "STRING"
    },
    {
      "name": "applicationId",
      "type": "STRING"
    },
    {
      "name": "userAgent",
      "type": "STRING"
    },
    {
      "name": "tier",
      "type": "STRING"
    }
  ]
}
3. Create a new Event Receiver configuration for the above stream. You can create it by logging to management console
and going to Main -> Event Processor -> Event Receivers and create an event receiver for wso2event type and org.wso2.apimgt.statistics.request_1.0.0 stream.
Or you can copy the below xml content and paste it under DAS_HOME/repository/deployment/server/eventreceivers directory with file name 'EventReceiver_request.xml'.

        <?xml version="1.0" encoding="UTF-8"?>
        <eventReceiver name="EventReceiver_request" statistics="disable" trace="disable" xmlns="http://wso2.org/carbon/eventreceiver">
            <from eventAdapterType="wso2event">
                <property name="events.duplicated.in.cluster">false</property>
            </from>
            <mapping customMapping="disable" type="wso2event"/>
            <to streamName="org.wso2.apimgt.statistics.request" version="1.0.0"/>
        </eventReceiver>

3. Go to $WSO2_DAS_HOME/samples/apim-stats directory via console
4. Type 'ant' from the console


