WSO2 Machine Learner - Sample Model Generation Flow
===================================================

Note : This sample only works in Unix environment

Algorithm: K Means Anomaly Detection with Labeled Data
========================

Tuned hyper-parameter values
============================

Max_Iterations : 100
Num_of_Normal_Clusters : 5

Before Running
==============

* Start WSO2 Machine Learner product.
* Make sure jq is installed. (To install jq http://stedolan.github.io/jq/download/)

How to Run?
===========

* Run the model-generation.sh script file.

Outcome
=======

* Running the above script will result in a generation of k means anomaly detection with labeled data model for the provided data-set.

* Model will be stored in the file system at {ML_HOME}/models directory.
