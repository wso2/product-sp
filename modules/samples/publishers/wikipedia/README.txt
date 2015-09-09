
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
6. Set Java system properties, "path" and "count" to point to Wikipedia article XML dump file, and count to the number of
   articles to be published as events respectively, count=-1 to publish all articles
7. Type 'ant <params>' from the console, e.g. "ant -Dpath=/home/laf/Downloads/enwiki-20150805-pages-articles.xml -Dcount=1000"
8. You may use the Data Explorer or the Analytics Dashboard in the DAS Management Console to browse published sample events,
   and also the Batch Analytics -> Console (Spark SQL) to execute any queries

NOTE:-

The wikipedia dataset is transferred as a single article in single event, thus an event will be relatively large (~300KB). So the server configuration tuning,
specially the queue sizes available in "data-bridge-config.xml" for data receiving and "analytics-eventsink-config.xml" for persistence, and also the 
the publisher "data-agent-conf.xml", should be done. And, the target database server should be able to handle around maximum 20MB batch inserts, for example, 
the default Cassandra batch limits are not enough, so the settings "batch_size_warn_threshold_in_kb" and "batch_size_fail_threshold_in_kb" to around "51200".

Sample Spark SQL Queries
========================

* Create the wiki table:-

CREATE TEMPORARY TABLE wiki USING CarbonAnalytics  OPTIONS (tableName "org_wso2_das_sample_wikipedia_data");

* Pages done by each contributor:-

SELECT COUNT(*) as page_count, contributor_username FROM wiki GROUP BY contributor_username;

* Average Wikipedia article length:-

SELECT AVG(length) as avg_article_length FROM wiki;

* Total number of characters in all Wikipedia articles:-

SELECT SUM(length) as total_article_chars FROM wiki;

* Total number of pages in Wikipedia:-

SELECT COUNT(*) as total_pages FROM wiki;
