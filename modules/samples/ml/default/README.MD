WSO2 Machine Learner - Default Samples for Model Generation Flow
===============================================================

Note : The samples only work in Unix Environment

Before Running
==============

* Start WSO2 Machine Learner product.
* Make sure jq is installed. (To install jq http://stedolan.github.io/jq/download/)

OS X users need to install gnu-sed.

How to install gnu-sed in Mac OS X;
      i. Install homebrew(http://brew.sh/)
        $ ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
      ii. Installed gnu-sed
        $ brew install gnu-sed

About Samples
=============

Each folder contains samples for ML model generation flow using different ML algorithms.
The dataset used for each sample is included as a .csv file.
Model configuration (selected algorithm, response variable, train-data fraction) is defined in the create-model-config file inside ach folder.
These default samples use the default hyper-parameter values defined in WSO2 ML. ({ML_HOME}/repository/conf/machine-learner.xml)

How to Run?
===========

* Run the model-generation.sh script file.

Outcome
=======

* Running the above script will generate a ML model for the provided data-set using the defined algorithm.
* Models will be stored in the file system at {ML_HOME}/models directory.
