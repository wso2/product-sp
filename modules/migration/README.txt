-----------------------------------------------------------------------------------------------
		FOLLOW THE BELOW INSTRUCTIONS TO RUN THE MIGRATION SCRIPT
-----------------------------------------------------------------------------------------------

NOTE: This migration script migrates the data in the Cassandra server used by 
BAM 2.0.0 or 2.0.1 so that it is compatible with BAM 2.2.0 or higher.

1) Make sure your cassandra server is up and running. If you are using the embedded cassandra in BAM server, then BAM server should be started and running at that time.

2) Check the cassandra configuration of the script in $BAM_HOME/migration/conf/cassandra-config.xml 
and edit the properties according to your setup. By default the embedded cassandra starts on port 9160. And if you have started the BAM with port offset and use that as cassandra server, then the cassandra port should be 9160+<port-offset>.

The following is the dafault configuration of embedded cassandra in BAM.

			<Cassandra-Cluster>
			    <clusterName>TestCluster</clusterName>
			    <nodes>localhost:9160</nodes>
			    <username>admin</username>
			    <password>admin</password>
			</Cassandra-Cluster>

2) After you complete editing and verifying the cassandra cluster information, 
Go the $BAM_HOME/migration/scripts directory via the terminal.
 
3) Execute the bam_migration.sh from terminal by typing ./bam_migration.sh.

4) Restart the BAM server.
