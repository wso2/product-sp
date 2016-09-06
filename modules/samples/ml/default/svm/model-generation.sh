#!/bin/bash

# check performance test mode
mode="$1"

echo "testing SVM workflow"

# server IP source
. ../../server.conf

# Die on any error:
set -e

DIR="${BASH_SOURCE%/*}"; if [ ! -d "$DIR" ]; then DIR="$PWD"; fi; . "$DIR/../../base.sh"

echo "#create a dataset"
path=$(pwd)
curl -X POST -b cookies  https://$SEVER_IP:$SERVER_PORT/api/datasets -H "Authorization: Basic YWRtaW46YWRtaW4=" -H "Content-Type: multipart/form-data" -F datasetName='indiansDiabetes-svm-dataset' -F version='1.0.0' -F description='Pima Indians Diabetes Dataset' -F sourceType='file' -F destination='file' -F dataFormat='CSV' -F containsHeader='true' -F file=@'/'$path'/IndiansDiabetes.csv' -k
sleep 5

# creating a project
echo "#creating a project"
curl -X POST -d @'create-project' -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://$SEVER_IP:$SERVER_PORT/api/projects -k
sleep 2

#getting the project
echo "#getting the project"
project=$(curl -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://$SEVER_IP:$SERVER_PORT/api/projects/wso2-ml-svm-sample-project -k)
sleep 2

#update the json file with retrieved values
projectId=$(echo "$project"|jq '.id')
datasetId=$(echo "$project"|jq '.datasetId')
${SED} -i 's/^\("projectId":"\)[^"]*/\1'$projectId/ create-analysis;
sleep 2

#creating an analysis
echo "creating an analysis"
curl -X POST -d @'create-analysis' -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://$SEVER_IP:$SERVER_PORT/api/analyses -k
sleep 2

#getting analysis id
echo "getting analysis id"
analysis=$(curl -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://$SEVER_IP:$SERVER_PORT/api/projects/${projectId}/analyses/wso2-ml-svm-sample-analysis -k)
sleep 2

analysisId=$(echo "$analysis"|jq '.id')

#setting model configs
echo "#setting model configs"
curl -X POST -d @'create-model-config' -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://$SEVER_IP:$SERVER_PORT/api/analyses/${analysisId}/configurations -k -v
sleep 2

echo "#adding default features with customized options"
curl -X POST -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://$SEVER_IP:$SERVER_PORT/api/analyses/${analysisId}/features/defaults -k -v -d @'customized-features'
sleep 2

echo "#setting default hyper params"
curl -X POST -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://$SEVER_IP:$SERVER_PORT/api/analyses/${analysisId}/hyperParams/defaults -k -v
sleep 2

echo "#getting dataset version"
datasetVersions=$(curl -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://$SEVER_IP:$SERVER_PORT/api/datasets/${datasetId}/versions -k)
sleep 2

#update the json file
datasetVersionId=$(echo "$datasetVersions"|jq '.[0] .id')
${SED} -i 's/^\("analysisId":"\)[^"]*/\1'$analysisId/ create-model;
sleep 2
${SED} -i 's/^\("versionSetId":"\)[^"]*/\1'$datasetVersionId/ create-model;
sleep 2

# build only one model for default case and warm-tests
# build three models for performance tests
modelCount=1
if [ "$mode" = "perf" ]; then
	modelCount=3
fi

for i in `seq $modelCount`; do
	echo "#create model"
	model=$(curl -X POST -d @'create-model' -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://$SEVER_IP:$SERVER_PORT/api/models -k)
	sleep 2

	echo "#getting model"
	modelName=$(echo "$model"|jq -r '.name')
	model=$(curl -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://$SEVER_IP:$SERVER_PORT/api/models/${modelName} -k)
	sleep 2
	modelId=$(echo "$model"|jq '.id')

	echo "#building the model"
	curl -X POST -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://$SEVER_IP:$SERVER_PORT/api/models/${modelId} -k -v

	while [ 1 ]
        do
        model=$(curl -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://$SEVER_IP:$SERVER_PORT/api/models/${modelName} -k)
        sleep 2
        model_status=$(echo "$model"|jq '.status')
        if [[ $model_status == *"Complete"* ]]
        then
           echo "Model building has completed."
           break
        fi
        sleep 10
        done

	echo "#exporting model to pmml"
	curl -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://$SEVER_IP:$SERVER_PORT/api/models/${modelId}/export?mode=pmml -k
done

	echo "#predict using model"
        curl -X POST -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://$SEVER_IP:$SERVER_PORT/api/models/${modelId}/predict -k -v -d @'prediction-test'

# delete project and dataset when running warm-up tests
if [ "$mode" = "wmp" ]; then
	curl -s -X DELETE -H "Authorization: Basic YWRtaW46YWRtaW4=" https://$SEVER_IP:$SERVER_PORT/api/projects/${projectId} -k
	curl -s -X DELETE -H "Authorization: Basic YWRtaW46YWRtaW4=" https://$SEVER_IP:$SERVER_PORT/api/datasets/${datasetId} -k
fi
