
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
   articles to be published as events respectively, count=-1 to publish all articles.
   e.g. "ant -Dpath=/home/laf/Downloads/enwiki-20150805-pages-articles.xml -Dcount=1000"
7. Type 'ant' from the console (This will create arbitrary values for each parameter in the stream and send as an event)
8. You may use the Data Explorer or the Analytics Dashboard in the DAS Management Console to browse published sample events

Sample Spark SQL Queries
========================

* Create the wiki table:-

CREATE TEMPORARY TABLE wiki  USING CarbonAnalytics  OPTIONS (tableName "org_wso2_das_sample_wikipedia_data", schema "title string, contributor_username string, contributor_id LONG, format string, text string, length integer, model string, format string, comment string, revision_ts long", primaryKeys "sha1");

* Pages done by each contributor:-

SELECT COUNT(*) as page_count, contributor_username FROM wiki GROUP BY contributor_username;

* Average Wikipedia article length:-

SELECT AVG(length) as avg_article_length FROM wiki;

* Total number of characters in all Wikipedia articles:-

SELECT SUM(length) as total_article_chars FROM wiki;

* Total number of pages in Wikipedia:-

SELECT COUNT(*) as total_pages FROM wiki;
