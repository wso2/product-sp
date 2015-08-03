
===============================Prerequisites==============================
	
You need a Java Development Kit / JRE version 1.7.x or later and Apache Ant 1.7.0 or later, at a minimum, to try out
the samples. Apache Ant can be downloaded at http://ant.apache.org.

Please Follow below instructions to run the Smart Home sample
===================================================================

1. Start the WSO2 DAS Server
2. Add below stream definition to DAS server as explained in https://docs.wso2.com/display/CEP310/Working+with+Event+Streams

{
  'name': 'org.wso2.sample.smart.home.data',
  'version': '1.0.0',
  'nickName': 'Smart_Home_Data',
  'description': 'Data from devices in a Smart Home',
  'metaData': [
    { 'name': 'publisher','type': 'STRING' }
  ],
  'payloadData': [
    { 'name': 'id' , 'type': 'STRING' },
    { 'name': 'value' , 'type': 'FLOAT' },
    { 'name': 'is_certified' , 'type': 'BOOL' },
    { 'name': 'device_id' , 'type': 'INT' },
    { 'name': 'household_id' , 'type': 'INT' },
    { 'name': 'house_id' , 'type': 'INT' }
  ]
}

3. Create a new Event Receiver configuration for the above stream. You can create it by logging in to the management console
and going to Main -> Event Processor -> Event Receivers and create an event receiver for wso2event type and org.wso2.sample.smart.home.data_1.0.0 stream.
Alternatively, you can copy the below xml content and paste it under <DAS_HOME>/repository/deployment/server/eventreceivers directory with file name 'smarthomestream.xml'.

        <?xml version="1.0" encoding="UTF-8"?>
        <eventReceiver name="smarthomestream" statistics="disable" trace="disable" xmlns="http://wso2.org/carbon/eventreceiver">
            <from eventAdapterType="wso2event"/>
            <mapping customMapping="disable" type="wso2event"/>
            <to streamName="org.wso2.sample.smart.home.data" version="1.0.0"/>
        </eventReceiver>

3. Go to <DAS_HOME>/samples/smart-home directory via console
4. Type 'ant' from the console
 (This will create arbitrary values for each parameter in the stream and send as an event )




