
===============================Prerequisites==============================
	
You need a Java Development Kit / JRE version 1.7.x or later and Apache Ant 1.7.0 or later, at a minimum, to try out
the samples. Apache Ant can be downloaded at http://ant.apache.org.

Please Follow below instructions to run the Smart Home sample
===================================================================

1. Download Wikipedia data dump from https://meta.wikimedia.org/wiki/Data_dump_torrents#enwiki, 
   and extract the compressed XML articles dump file
2. Start the WSO2 DAS Server
3. In the DAS Management Console, navigate to Carbon Applications -> Add
4. Install the DAS Composite Application for the Wikipedia sample from <DAS_HOME>/samples/capps/Wikipedia.car
5. Go to <DAS_HOME>/samples/wikipedia directory via console
6. Edit build.xml file's path property to point to Wikipedia article XML dump file, and count to the number of
   articles to be published as events, -1 for all
7. Type 'ant' from the console (This will create arbitrary values for each parameter in the stream and send as an event)
8. You may use the Data Explorer or the Analytics Dashboard in the DAS Management Console to browse published sample events




