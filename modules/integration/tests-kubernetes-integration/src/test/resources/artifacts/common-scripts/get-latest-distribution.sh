#!/bin/sh
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

# This is triggered from fetch-artifacts.sh
prgdir=$(dirname "$0")
script_path=$(cd "$prgdir"; cd .. ; pwd)
product_dist=$1
custom_pack=$2
jenkins_base_url="https://wso2.org/jenkins/job/products/job/product-sp"

  if [[ $product_dist == "RELEASE" ]]
  then
    echo "$jenkins_base_url/lastRelease/api/xml?xpath=//url"
    build_url=$(curl -s -G "$jenkins_base_url/lastRelease/api/xml?xpath=//url")
    build_url=$(echo $build_url | sed -n 's:.*<url>\(.*\)</url>.*:\1:p')
    echo "Latest Successful Release : "$build_url

    # Get the relativePath of the distribution pack
    #
    disrtribution_url=$(curl -s -G $build_url"api/xml?xpath=//artifact/relativePath")
    echo $disrtribution_url

    # Extracting the relative path to get the distribution pack
    downloadable_url=$(echo $disrtribution_url | sed -n 's:.*<relativePath>\(.*\)</relativePath>.*:\1:p')

    echo "Downloadable URL : " $build_url"artifact/"$downloadable_url
    download_url=$build_url"artifact/"$downloadable_url

  elif [[ $product_dist == "SNAPSHOT" ]]
  then
    echo "SNAPSHOT"
    #Get the Latest Successful Build URL from JENKINS
    build_url=$(curl -s "$jenkins_base_url/api/xml?xpath=//lastSuccessfulBuild/url")
    echo "last success build value is :"$build_url
    #Extract the URL from the latest_successfull_build
    build_url=$(echo $build_url | sed -n 's:.*<url>\(.*\)</url>.*:\1:p')
    disrtribution_url=$(curl -s -G $build_url"org.wso2.sp\$product/api/xml" -d xpath=\(/mavenBuild//relativePath\)[2])
    downloadable_url=$(echo $disrtribution_url | sed -n 's:.*<relativePath>\(.*\)</relativePath>.*:\1:p')
    echo $downloadable_url

    echo "Downloadable URL : " $build_url"org.wso2.sp\$product/artifact/"$downloadable_url
    download_url=$build_url"org.wso2.sp\$product/artifact/"$downloadable_url
  fi

#download the distribution
wget $download_url -P $script_path/docker-files/tmp/files
#
##Unzip the dist to the correct location
unzip -q $script_path/docker-files/tmp/files/*.zip -d $script_path/docker-files/tmp/files/
rm $script_path/docker-files/tmp/files/*.zip
