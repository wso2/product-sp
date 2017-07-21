#!/usr/bin/env bash

set -e

docker_server="dockerhub.private.wso2.com"
docker_user="dasintegrationtest"
docker_pw="nzraxthlg5kdzmrXkwjuhia'6sziHw"

prgdir=$(dirname "$0")
script_path=$(cd "$prgdir"; cd ..; pwd)
echo "script path "$script_path


#------## This is to download latest pack from jenkins
echo "fetching latest DAS distribution pack and deploying..."
#sh $script_path/common-scripts/get-latest-distribution.sh
#sleep 2

#----- Extract the distribution to the temporary location and move it to the distribution directory

mkdir $script_path/docker-files/tmp
cp product-4.0.0-SNAPSHOT.zip $script_path/docker-files/tmp
#unzip -q product-4.0.0-SNAPSHOT.zip -d $script_path/docker-files/tmp/
sleep 5
echo "Distribution pack copied to temporary directory and waiting for image launch..."

#------- to copy downloaded distribution to DAS image
#echo "Copying files from the temp directory to distribution directory"
#cp -r tmp/*/* ${das_home}/distribution/
#sudo docker cp $script_path/tmp/* $containerid:/home/

sudo docker login $docker_server
sudo docker build $script_path/docker-files/ -t $docker_server/$docker_user-spintegrationtest-ubuntu:14.04
echo "Image build is success"
echo "Deleting the temp directory!!"
rm -rf tmp

#----- ## to push updated image to online registry

#sudo docker commit $containerid $new_image:4.0.0
sudo docker push $docker_server/$docker_user-spintegrationtest-ubuntu:14.04
sleep 2

#----- ## To remove container and image from local

#sudo docker stop $containerid
#sudo docker rm $containerid
#sudo docker rm $(sudo docker ps -a -q)
#sudo docker rmi -f $(sudo docker images -a -q)
#sleep 10
#sudo docker rmi -f $image_id



