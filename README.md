<!--
  ~  Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
  ~
  ~  WSO2 Inc. licenses this file to you under the Apache License,
  ~  Version 2.0 (the "License"); you may not use this file except
  ~  in compliance with the License.
  ~  You may obtain a copy of the License at
  ~
  ~    http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~  Unless required by applicable law or agreed to in writing,
  ~  software distributed under the License is distributed on an
  ~  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~  KIND, either express or implied.  See the License for the
  ~  specific language governing permissions and limitations
  ~  under the License.
  -->

WSO2 Data Analytics Server
==========================

---

|  Branch | Build Status |
| :------------ |:-------------
| master      | [![Build Status](https://wso2.org/jenkins/job/product-das/badge/icon)](https://wso2.org/jenkins/job/product-das) |


---

## Introducing WSO2 Data Analytics Server ##

WSO2 Data Analytics Server 3.1.0 combines real-time, batch, interactive, and predictive (via machine learning) analysis of data into one integrated platform to support the multiple demands of Internet of Things (IoT) solutions, as well as mobile and Web apps.

As a part of WSO2’s Analytics Platform, WSO2 DAS introduces a single solution with the ability to build systems and applications that collect and analyze both batch and realtime data to communicate results. It is designed to treat millions of events per second, and is therefore capable to handle Big Data volumes and Internet of Things projects.


[![DAS Overview](http://b.content.wso2.com/sites/all/product-pages/images/das-overview.png)](http://wso2.com/products/data-analytics-server/#Capabilities)


WSO2 Data Analytics Server is an evolution of WSO2 Business Activity Monitor, built to better serve the needs of today’s connected enterprise.

WSO2 DAS is powered by [WSO2 Carbon](http://wso2.com/products/carbon/), the SOA middleware component platform. An open source product, WSO2 Carbon is available under the [Apache Software License (v2.0)](http://www.apache.org/licenses/LICENSE-2.0.html).

Download the WSO2 DAS 3.0.0 distribution from the [WSO2 DAS Product Page](http://wso2.com/products/data-analytics-server) and give it a try!


#### Key Features of WSO2 DAS ####


- Data aggregation
- Integrated, real-time, and batch analytics
- Interactive analytics and edge analytics
- High level language and data storage
- Extensibility using C-Apps
- Communication

#### System Requirements ####


1. Memory   - 2 GB minimum
2. Disk     - 1 GB minimum for unpacking the distribution
3. Java(TM) - Oracle Java SE Development Kit 1.7 or later

For a full list, please see [WSO2 DAS Installation Prerequisites](https://docs.wso2.com/display/DAS300/Installation+Prerequisites).


#### How to Run ####

1. Extract the downloaded zip
2. Go to the bin directory in the extracted folder
3. Run the wso2server.sh or wso2server.bat as appropriate
4. Point your browser to the URL https://localhost:9443/carbon
5. Use "admin", "admin" as the username and password to login as an admin
6. If you need to start the OSGi console with the server use the property -DosgiConsole when starting the server. The INSTALL.txt file found on the installation directory will give you a comprehensive set of options and properties that can be passed into the startup script
7. Samples are available pre-packaged with the distribution at \<DAS_HOME\>/samples. Please have a look at the [samples guide](https://docs.wso2.com/display/DAS300/Samples) for more information.


#### Documentation ####

Please see the [WSO2 Documentation site for WSO2 DAS](https://docs.wso2.com/display/DAS300/WSO2+Data+Analytics+Server+Documentation)


#### What's New in the Latest Release (DAS 3.1.0) ####

This release fixes the following bugs over Data Analytics Server 3.0.1 release.
https://wso2.org/jira/issues/?filter=12622

#### Support ####

We are committed to ensuring that your enterprise middleware deployment is completely supported from evaluation to production. Our unique approach ensures that all support leverages our open development methodology and is provided by the very same engineers who build the technology.

For more details and to take advantage of this unique opportunity please visit http://wso2.com/support.


#### Reporting Issues  ####

We encourage you to report issues, documentation faults and feature requests regarding WSO2 DAS through the public [DAS JIRA](https://wso2.org/jira/browse/DAS).

You can use the [Carbon JIRA](https://wso2.org/jira/browse/CARBON) to report any issues related to the Carbon base framework or associated Carbon components.


#### Crypto Notice ####

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

   Apache Rampart   : http://ws.apache.org/rampart/
   Apache WSS4J     : http://ws.apache.org/wss4j/
   Apache Santuario : http://santuario.apache.org/
   Bouncycastle     : http://www.bouncycastle.org/

(c) Copyright 2015 WSO2 Inc.
