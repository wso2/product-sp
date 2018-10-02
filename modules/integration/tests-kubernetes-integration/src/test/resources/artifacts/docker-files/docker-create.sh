#!/usr/bin/env bash
################################################################################
#   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved
#
#   Licensed under the Apache License, Version 2.0 (the \"License\");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an \"AS IS\" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
################################################################################

set -e
prgdir=$(dirname "$0")
script_path=$(cd "$prgdir"; cd ..; pwd)
echo "script path "$script_path

# Read the input parameters to the script.
#
distribution_mode=$1          #The mode can be snapshot,release or custom
jdk=$2                        #The jdk of the docker images
database=$3                   #The database used by product
download_distribution=$4      #if the pack if provided or needs to be downloaded

# Read the docker registry values from properties file
#
docker_server=$(grep -r "docker_server" ${script_path}/docker-files/docker-registry.properties  | sed -e 's/docker_server=//' | tr -d '"')
docker_user=$(grep -r "docker_user" ${script_path}/docker-files/docker-registry.properties  | sed -e 's/docker_user=//' | tr -d '"')
password=$(grep -r "docker_pw" ${script_path}/docker-files/docker-registry.properties  | sed -e 's/docker_pw=//' | tr -d '"')

# Downloading the docker-sp resources.
#
if [[ ${download_distribution} == true ]]
then
    # This is to download latest pack from jenkins
    echo "fetching latest SP distribution pack !"
    source ${script_path}/common-scripts/get-latest-distribution.sh ${distribution_mode}
    sleep 2
fi

# Change memory params-
#
sed -i 's/-Xms256m -Xmx1024m/-Xmx2G -Xms2G/g' ${script_path}/docker-files/tmp/files/wso2*/wso2/worker/bin/carbon.sh
echo "Update the Java heap space "

#--------------------------------------------Important!-------------------------------#
#place other required resources inside the docker-files/tmp/files folder
#

# Convert values to lowercase
#
jdk_tag=$(echo "$jdk" | sed 's/.*/\L&/')
db_tag=$(echo "$database" | sed 's/.*/\L&/')

# Docker login
#
if [ -z ${password} ];
then
    docker login ${docker_server} -u ${docker_user}
else
    docker login ${docker_server} -u ${docker_user} -p ${password}
fi

# Update the product-version.
#
product_version=$(basename $(realpath $script_path/docker-files/tmp/files/wso2sp*) | cut -d'-' -f2-)
echo "product version: " ${product_version}

# Switch the base image according to the jdk type
#
if [[ $jdk == "OPEN_JDK8" ]]
then
  base_image="$docker_server"/openjdk8-8-ubuntu
else
  base_image="$docker_server"/oraclejdk8-8-ubuntu
fi

# Build the docker image
#
docker build -t wso2sp-intg-worker:"$jdk_tag"-"$db_tag"-sp-"$product_version"\
 --build-arg JDK_BASE="$base_image" --build-arg WSO2_SERVER_VERSION="$product_version" .
cd ${script_path}/docker-files

# Tag docker image with repository
#
docker tag wso2sp-intg-worker:"$jdk_tag"-"$db_tag"-sp-"$product_version"\
 ${docker_server}/wso2sp-intg-worker:"$jdk_tag"-"$db_tag"-sp-"$product_version"

docker push ${docker_server}/wso2sp-intg-worker:"$jdk_tag"-"$db_tag"-sp-"$product_version"

# Update the image reference in replication-controllers.
#
sed -i "s/        image: .*/        image: $docker_server\/wso2sp-intg-worker:$jdk_tag-$db_tag-sp-$product_version/g"\
 ${script_path}/ha-scripts/sp-ha-node-1-rc.yaml
sed -i "s/        image: .*/        image: $docker_server\/wso2sp-intg-worker:$jdk_tag-$db_tag-sp-$product_version/g"\
 ${script_path}/ha-scripts/sp-ha-node-2-rc.yaml
sed -i "s/        image: .*/        image: $docker_server\/wso2sp-intg-worker:$jdk_tag-$db_tag-sp-$product_version/g"\
 ${script_path}/sp-standalone/sp-test-rc.yaml

# Clear docker image resources
#
rm -rf ${script_path}/docker-files/tmp/files/*
