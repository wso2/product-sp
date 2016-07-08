WSO2 Machine Learner - Sample Model Generation Flow
===================================================

Note : This sample only works in Unix environment

Algorithm: Logistic-Regression
==============================

Tuned hyper-parameter values
============================

Iterations : 5000
Learning_Rate : 0.01
Reg_Parameter : 0.001
Reg_Type : L2
SGD_Data_Fraction : 1

Before Running
==============

* Start WSO2 Machine Learner product.
* Make sure jq is installed. (To install jq http://stedolan.github.io/jq/download/)

How to Run?
===========

* Run the model-generation.sh script file.

Outcome
=======

* Running the above script will result in a generation of logistic regression sgd model for the provided data-set.

* Model will be stored in the file system at {ML_HOME}/models directory.
