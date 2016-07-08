WSO2 Machine Learner - Sample Model Generation Flow
===================================================

Note : This sample only works in Unix environment

Algorithm: Random Forest Classification
=======================================

Tuned hyper-parameter values
============================

Num_Trees : 5
Max_Depth : 5
Max_Bins : 100
Impurity : gini
Feature_Subset_Strategy : auto
Seed :12345

Before Running
==============

* Start WSO2 Machine Learner product.
* Make sure jq is installed. (To install jq http://stedolan.github.io/jq/download/)

How to Run?
===========

* Run the model-generation.sh script file.

Outcome
=======

* Running the above script will result in a generation of random forest model for the provided data-set.

* Model will be stored in the file system at {ML_HOME}/models directory.
