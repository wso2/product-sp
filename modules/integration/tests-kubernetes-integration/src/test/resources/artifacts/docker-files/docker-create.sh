#!/usr/bin/env bash

set -e

prgdir=$(dirname "$0")
script_path=$(cd "$prgdir"; cd ..; pwd)
echo "script path "$script_path

#The mode can be snapshot,release or custom
DISTRIBUTION_MODE=$1
#The jdk of the docker images
JDK=$2
#The database used by product
DB=$3

DIST_PROVIDED=$4

#Read the docker registry values from properties file
docker_server=$(grep -r "docker_server" $script_path/docker-files/docker-registry.properties  | sed -e 's/docker_server=//' | tr -d '"')
docker_user=$(grep -r "docker_user" $script_path/docker-files/docker-registry.properties  | sed -e 's/docker_user=//' | tr -d '"')
PASSWORD=$(grep -r "docker_pw" $script_path/docker-files/docker-registry.properties  | sed -e 's/docker_pw=//' | tr -d '"')

#------## Downloading the docker-sp resources

if [[ $DIST_PROVIDED == false ]]
then
    #------## This is to download latest pack from jenkins
    echo "fetching latest SP distribution pack !"
    source $script_path/common-scripts/get-latest-distribution.sh $DISTRIBUTION_MODE $CUSTOM_PACK
    sleep 2
fi

#change memory params
sed -i 's/-Xms256m -Xmx1024m/-Xmx2G -Xms2G/g' $script_path/docker-files/tmp/files/wso2*/wso2/worker/bin/carbon.sh
echo "Update the Java heap space "

#--------------------------------------------Important!--------------------------------------------------------------
#place other required resources inside the docker-files/tmp/files folder
#
jdk_tag=$(echo "$JDK" | sed 's/.*/\L&/')
db_tag=$(echo "$DB" | sed 's/.*/\L&/')

if [ -z $PASSWORD ];
then
    docker login $docker_server -u $docker_user
else
    docker login $docker_server -u $docker_user -p $PASSWORD
fi

#update the product-version
product_version=$(basename $(realpath $script_path/docker-files/tmp/files/wso2sp*) | cut -d'-' -f2-)
echo "product version: " $product_version
#mysql jar will be copied by default

##switch the base image according to the jdk type

if [[ $product_dist == "OPEN_JDK8" ]]
then
  base_image=openjdk8-8-ubuntu
else
  base_image=oraclejdk8-8-ubuntu
fi
echo "$base_image"
docker -v

docker build -t wso2sp-intg-worker:"$jdk_tag"-"$db_tag"-sp-"$product_version" --build-arg JDK_BASE="$base_image" .
cd $script_path/docker-files

##tag docker image with repository
docker tag wso2sp-intg-worker:"$jdk_tag"-"$db_tag"-sp-"$product_version" $docker_server/wso2sp-intg-worker:"$jdk_tag"-"$db_tag"-sp-"$product_version"
docker push $docker_server/wso2sp-intg-worker:"$jdk_tag"-"$db_tag"-sp-"$product_version"

#update the image reference in replication-controllers
sed -i "s/        image: .*/        image: $docker_server\/wso2sp-intg-worker:$jdk_tag-$db_tag-sp-$product_version/g" $script_path/ha-scripts/sp-ha-node-1-rc.yaml
sed -i "s/        image: .*/        image: $docker_server\/wso2sp-intg-worker:$jdk_tag-$db_tag-sp-$product_version/g" $script_path/ha-scripts/sp-ha-node-2-rc.yaml
sed -i "s/        image: .*/        image: $docker_server\/wso2sp-intg-worker:$jdk_tag-$db_tag-sp-$product_version/g" $script_path/sp-standalone/sp-test-rc.yaml

#clear docker-sp files from worksapce
rm -rf $script_path/docker-files/tmp/files/*
