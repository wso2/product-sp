#!/usr/bin/env bash

set -e

docker_server="dockerhub.private.wso2.com"
docker_user="dasintegrationtest"

prgdir=$(dirname "$0")
script_path=$(cd "$prgdir"; cd ..; pwd)
echo "script path "$script_path


#------## This is to download latest pack from jenkins
echo "fetching latest DAS distribution pack and deploying..."
#/bin/bash $script_path/common-scripts/fetch-artifacts.sh
#sleep 2

#----- Extract the distribution to the temporary location and move it to the distribution directory

mkdir -p $script_path/docker-files/tmp
cp $script_path/wso2sp-4*.zip $script_path/docker-files/tmp/wso2sp-4.0.0.zip
unzip -q $script_path/docker-files/tmp/wso2sp-4.0.0.zip -d $script_path/docker-files/tmp/dist/
sleep 5
echo "Distribution pack copied to temporary directory and waiting for image launch..."

#------- to copy downloaded distribution to DAS image
#echo "Copying files from the temp directory to distribution directory"
#cp -r tmp/*/* ${das_home}/distribution/
#sudo docker cp $script_path/tmp/* $containerid:/home/

if [ -z $PASSWORD ];
then
    sudo docker login $docker_server -u $docker_user
else
    sudo docker login $docker_server -u $docker_user -p $PASSWORD
fi
sudo docker build $script_path/docker-files/ -t $docker_server/$docker_user-spintegrtestm16-ubuntu:1.3
echo "Image build is success"

#----- ## to push updated image to online registry

#sudo docker commit $containerid $new_image:4.0.0
sudo docker push $docker_server/$docker_user-spintegrtestm16-ubuntu:1.3
sleep 2

#----- ## To remove container and image from local
sudo rm -rf $script_path/docker-files/tmp/dist/
#sudo docker stop $containerid
#sudo docker rm $containerid
#sudo docker rm $(sudo docker ps -a -q)
#sudo docker rmi -f $(sudo docker images -a -q)
#sleep 10
#sudo docker rmi -f $image_id



