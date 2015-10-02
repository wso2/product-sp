WSO2 Data Analytics Server, v3.0.0
==================================

@buildNumber@

Welcome to the WSO2 Data Analytics Server, v3.0.0 release.

The WSO2 DAS version 3.0.0 is the first version of WSO2 DAS, which is complete rewrite of the analytics solution based on the latest technologies. It contains the following new features and enhancements.

WSO2 DAS is powered by WSO2 Carbon, the SOA middleware component platform. 

New Features
============

	- Data abstraction layer for analytics

	- Apache Spark SQL-based analytics query execution

	- Data publisher rewrite

	- RDBMS datasource support

	- REST APIs for analytics data service

	- CLI like user interface for Spark integration


Also, WSO2 DAS contains the following major new technological changes/substitutions when compared to its predecessor, which is WSO2 BAM. 

	- Introduction of a generic data store that can mount RDBMS, HBase, Cassandra or any other data store, instead of supporting Cassandra as the one and only event store.

	- Replaced Hadoop by Apache Spark, and Hive by Spark SQL.

	- Introduced indexing on stream persistence using Apache Solr, instead of the Casandra secondary and custom index based indexing.

	- Replaced the Gadget Server and Gadget generating tool with WSO2 UES-based dashboards and its new gadget generating tool.

	- Integration of WSO2 CEP 4.0.0 based features, instead of WSO2 CEP 3.x.

	- Introduced CAR file-based artifact deployment for WSO2 BAM toolbox support.

	- Removed WSO2 BAM report generation.


Features
========

*Data aggregation	
    Receives data from event sources through Java agents (Thrift, Kafka, JMS), JavaScript clients (Web Sockets, REST), to IoT (MQTT), and also from WSO2 Enterprise Service Bus Connectors.
    Publishes events to one API for real-time, batch or interactive processing.
    Ability to access the analytics service via comprehensive REST API.

*Integrated, real-time, and batch analytics
    Analyses both persisted and realtime data using a single product.
    Executes batch programs faster than Hadoop MapReduce in memory using Apache Spark.
    Detects patterns (fraud detection) by correlating events from multiple data sources in real time using the high performing, open source WSO2 CEP engine powered by WSO2 Siddhi.

*Interactive analytics and edge analytics
    Searches for full text, complex query lookup, distributed indexing support using Apache Lucene for interactive analytics.
    Correlates/filters events at the edge for edge analytics.

*High level language and data storage
    Use of a structured easy to learn SQL-like query language.
    Develops complex real-time queries using SQL-like Siddhi query language.
    Scalable analytic querying using Spark SQL.
    Support for RDBMS (MSSQL, Oracle, MySQL) as data storages for low to medium scale enterprise deployments.
    Support for HBase and Cassandra as NoSQL storage for Big Data enterprise deployments.

*Extensibility using C-Apps
    Industry/domain-specific toolboxes to extend the product for business use cases such as fraud detection, GIS data monitoring, activity monitoring etc.
    Ability to install C-Apps for each WSO2 middleware product, including the analytics functionality available with WSO2 API Manager.

*Communication
    Customizable dashboards that provide an at-a-glance view as well as an detail view.
    Detects conditions and generate realtime alerts and notifications (email, SMS, push notifications, physical sensor alarms etc.)
    Exposes event tables as an API via WSO2 API Manager and WSO2 Data Services Server.


Issues Fixed in This Release
============================
* WSO2 Data Analytics Server related components of the WSO2 Carbon Platform - https://wso2.org/jira/issues/?filter=12425

Known Issues in This Release
============================

* All known issues have been recorded at https://wso2.org/jira/issues/?filter=12426

Installation & Running
======================
1. Extract the downloaded zip file
2. Run the wso2server.sh or wso2server.bat file in the bin directory
3. Once the server starts, point your Web browser to
   https://localhost:9443/carbon/

For more details, see the Installation Guide

System Requirements
===================

1. Minimum memory - 2 GB
2. The Management Console requires full Javascript enablement of the Web browser

For more details see the Installation guide or,
https://docs.wso2.com/display/DAS300/Installation+Prerequisites

Including External Dependencies
===============================
For a complete guide on adding external dependencies to WSO2 Data Analytics Server & other carbon related products refer to the article:
http://wso2.org/library/knowledgebase/add-external-jar-libraries-wso2-carbon-based-products

WSO2 Data Analytics Server Binary Distribution Directory Structure
======================================================
     CARBON_HOME
        |-- bin <directory>
        |-- dbscripts <directory>
        |-- lib <directory>
        |-- samples <directory>
        |   |-- toolboxes <directory>
        |-- repository <directory>
        |   |-- carbonapps <directory>
        |   |-- components <directory>
        |   |-- conf <directory>
        |   |-- data <directory>
        |   |-- database <directory>
        |   |-- deployment <directory>
        |   |-- logs <directory>
        |   |-- resources <directory>
        |   |   `-- security <directory>
        |   `-- tenants <directory>
        |-- tmp <directory>
	    |-- webapp-mode <directory>
        |-- LICENSE.txt <file>
        |-- README.txt <file>
        |-- INSTALL.txt <file>
        `-- release-notes.html <file>

    - bin
      Contains various scripts .sh & .bat scripts.

    - dbscripts
      Contains the database creation & seed data population SQL scripts for
      various supported databases.

    - lib
      Contains the basic set of libraries required to startup Application Server
      in standalone mode

    - repository
      The repository where Carbon artifacts & Axis2 services and
      modules deployed in WSO2 Carbon are stored.
      In addition to this other custom deployers such as
      dataservices and axis1services are also stored.

        - carbonapps
          Carbon Application hot deployment directory.

    	- components
          Contains all OSGi related libraries and configurations.

        - conf
          Contains server configuration files. Ex: axis2.xml, carbon.xml

        - data
          Contains internal LDAP related data.

        - database
          Contains the WSO2 Registry & User Manager database.

        - deployment
          Contains server side and client side Axis2 repositories.
	      All deployment artifacts should go into this directory.

        - logs
          Contains all log files created during execution.

        - resources
          Contains additional resources that may be required.

	- tenants
	  Directory will contain relevant tenant artifacts
	  in the case of a multitenant deployment.

	- samples
          Contains the samples which describes the usage and fetures of 
	  WSO2 Data Analytics Server. This includes four samples: 

		- toolboxes
		  Complete toolboxes containing artifacts for the use cases of DAS

    - tmp
      Used for storing temporary files, and is pointed to by the
      java.io.tmpdir System property.

    - webapp-mode
      The user has the option of running WSO2 Carbon in webapp mode (hosted as a web-app in an application server).
      This directory contains files required to run Carbon in webapp mode.

    - LICENSE.txt
      Apache License 2.0 under which WSO2 Carbon is distributed.

    - README.txt
      This document.

    - INSTALL.txt
      This document contains information on installing WSO2 Application Server.

    - release-notes.html
      Release information for WSO2 Data Analytics Server 2.5.0

Secure sensitive information in carbon configuration files
==========================================================

There are sensitive information such as passwords in the carbon configuration.
You can secure them by using secure vault. Please go through following steps to
secure them with default mode.

1. Configure secure vault with default configurations by running ciphertool
	script from bin directory.

> ciphertool.sh -Dconfigure   (in UNIX)

This script would do following configurations that you need to do by manually

(i) Replaces sensitive elements in configuration files,  that have been defined in
		 cipher-tool.properties, with alias token values.
(ii) Encrypts plain text password which is defined in cipher-text.properties file.
(iii) Updates secret-conf.properties file with default keystore and callback class.

cipher-tool.properties, cipher-text.properties and secret-conf.properties files
			can be found at repository/conf/security directory.

2. Start server by running wso2server script from bin directory

> wso2server.sh   (in UNIX)

By default mode, it would ask you to enter the master password
(By default, master password is the password of carbon keystore and private key)

3. Change any password by running ciphertool script from bin directory.

> ciphertool -Dchange  (in UNIX)

For more details see
http://docs.wso2.org/display/Carbon420/WSO2+Carbon+Secure+Vault


Training
========

WSO2 Inc. offers a variety of professional Training Programs, including
training on general Web services as well as WSO2 Data Analytics Server and number of 
other products.

For additional support information please refer to
http://wso2.com/training/


Support
=======

We are committed to ensuring that your enterprise middleware deployment is completely supported
from evaluation to production. Our unique approach ensures that all support leverages our open
development methodology and is provided by the very same engineers who build the technology.

For additional support information please refer to http://wso2.com/support/

For more information on WSO2 Application Server, visit the WSO2 Oxygen Tank (http://wso2.org)

Crypto Notice
=============

   This distribution includes cryptographic software.  The country in
   which you currently reside may have restrictions on the import,
   possession, use, and/or re-export to another country, of
   encryption software.  BEFORE using any encryption software, please
   check your country's laws, regulations and policies concerning the
   import, possession, or use, and re-export of encryption software, to
   see if this is permitted.  See <http://www.wassenaar.org/> for more
   information.

   The U.S. Government Department of Commerce, Bureau of Industry and
   Security (BIS), has classified this software as Export Commodity
   Control Number (ECCN) 5D002.C.1, which includes information security
   software using or performing cryptographic functions with asymmetric
   algorithms.  The form and manner of this Apache Software Foundation
   distribution makes it eligible for export under the License Exception
   ENC Technology Software Unrestricted (TSU) exception (see the BIS
   Export Administration Regulations, Section 740.13) for both object
   code and source code.

   The following provides more details on the included cryptographic
   software:

   Apacge Rampart   : http://ws.apache.org/rampart/
   Apache WSS4J     : http://ws.apache.org/wss4j/
   Apache Santuario : http://santuario.apache.org/
   Bouncycastle     : http://www.bouncycastle.org/

For further details, see the WSO2 Carbon documentation at
http://docs.wso2.org/display/Carbon420/WSO2+Carbon+Documentation

---------------------------------------------------------------------------
(c)  @copyright.year@2014, WSO2 Inc.

