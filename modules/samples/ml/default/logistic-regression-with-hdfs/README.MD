WSO2 Machine Learner - Sample Model Generation Flow with HDFS
=============================================================

Note : This sample only works in Unix environment

Algorithm: Logistic-Regression
==============================

Before Running
==============

* Start WSO2 Machine Learner product.
* Make sure jq is installed. (To install jq http://stedolan.github.io/jq/download/)
* Upload the IndiansDiabetes.csv file to your Hadoop server into a */ml* directory.
* Configure the {ML_HOME}/repository/conf/machine-learner.xml 's */MachineLearner/HdfsURL* element's value as it points to your Hadoop server.

How to Run?
===========

* Run the model-generation.sh script file.

Outcome
=======

* Running the above script will result in a generation of logistic regression model for the provided data-set.

* Model will be stored in your HDFS at /tmp directory.
