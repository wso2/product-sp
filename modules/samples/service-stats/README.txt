Topics
 - BAM Data Agent Usage for Service Statistics
 - Simulating BAM Service Data Agent 

=================  BAM Data Agent Usage for Service Statistics ================

1. Download the WSO2 AS server.
	If we are running both AS and BAM server parallely we should change the port number of AS by applying an offset. In order to apply an offset, in the AS product directory go to $AS_home/repository/conf and open file, carbon.xml . Then change the value in xpath Server/Ports/Offset from '0' to some other integer.
To start the server, on Linux
		sh wso2server.sh
	on MS Windows
		wso2server.bat



2. After starting the server, go to AS management console Configure→Features→Settings menu.
	Give P2 repository link either from as a URL or from a file and install BAM Service Data Agent Aggregate. Follow the instructions given in the UI. After the installation restart the server. 



3. Go to AS management console and select Configure→Service Data Publishing from side panel.

    	a) Tick on 'Enable Service stats ' and 'Enable Activity Service'.
	b) Configuring stream definition. 
		  i) Provide qualified stream name for the stream definition (eg:- bam_service_data_publisher).
		 ii) Set the version. e.g.1.0.0
		iii) Set the nickname
		 iv) Set the description
		  v) Click update.
    	c) Set the 'BAM URL' as the URL of the BAM server. e.g. : tcp://IPAddress:7611	(tcp://<bam server IP>:<thrift port>)
    	d) Set user name and password of BAM server.


4. Send some request to HelloService.
	Go to AS management console Main→Web Services→List menu.
	In web service, 'HelloService ', click 'Try this service'. 
	Send some requests.



5. Now the HelloService will send events from AS to the BAM. Data in the Cassandra database can be seen from the Cassandra Explorer. Data will persist in bam_service_data_publisher column family (stream name is set as your column family name) in EVENT_KS keyspace.

===================== Simulating BAM Service Data Agent ========================= 

 - In order to simulate service data agent pumping events to BAM type ant in command line. The bundled sample statistics agent will publish a sample statistics event set to the BAM server.









 

 




