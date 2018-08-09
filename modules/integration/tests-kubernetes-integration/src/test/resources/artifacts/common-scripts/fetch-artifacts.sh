#!/usr/bin/env bash

set -e

prgdir=$(dirname "$0")
script_path=$(cd "$prgdir"; cd .. ; pwd)
echo $script_path
target_path=$(cd "$script_path"; cd ../../../../../.. ; pwd)
echo $target_path

# If you don't want to get latest build from Jenkins, You can pass the distribution's local repository URL
echo "Please enter your local distribution pack URL, if you don't have the one simply hit the enter: "
read -r URL_INPUT
SP_URL=$URL_INPUT
echo "(skip this if local URL given) Do you want to get latest build from Jenkins? [y/n]: "
read -r STATUS_INPUT
STATUS=$STATUS_INPUT

#check if sp_url is null

  if [[ -z $SP_URL && $STATUS == "n" ]]
  then
    echo "Fetching pack from jenkins pre-build"
    cp $target_path/distribution/target/wso2sp-4.0.0*-SNAPSHOT.zip $script_path

  elif [[ -z $SP_URL && $STATUS == "y" ]] #|| $STATUS -z ]]
  then
    echo "You are fetching latest build distribution from Jenkins!"
    source $script_path/common-scripts/get-latest-distribution.sh

    else
    wget $SP_URL
    echo "Get downloaded..."

  fi
  sleep 10
