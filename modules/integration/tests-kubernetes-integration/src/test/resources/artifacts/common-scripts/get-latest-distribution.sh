#!/bin/sh
# Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# me ---------- this has changed according to Stream Proccessor jenkins build
# This is triggered from fetch-artifacts.sh  
#jenkins_base_url="http://localhost:9090/job/helloworld"
jenkins_base_url="https://wso2.org/jenkins/job/products/job/product-sp"

#Get the Latest Successful Build URL from JENKINS
latest_successfull_build=$(curl -s "$jenkins_base_url/api/xml?xpath=//lastSuccessfulBuild/url")
#echo "last success build value is :"$latest_successfull_build

#Extract the URL from the latest_successfull_build
latest_successfull_build_url=$(echo $latest_successfull_build | sed -n 's:.*<url>\(.*\)</url>.*:\1:p')
echo "Latest Successful Build : "$latest_successfull_build_url

# Get the relativePath of the distribution pack
#dirtribution_url=$(curl -s -G $latest_successfull_build_url"org.ballerinalang.tools\$ballerina-tools/api/xml" -d xpath=\(/mavenBuild//relativePath\)[2])
disrtribution_url=$(curl -s -G $latest_successfull_build_url"org.wso2.sp\$product/api/xml" -d xpath=\(/mavenBuild//relativePath\)[2])
echo $disrtribution_url

# org.wso2.sp/product/4.0.0-SNAPSHOT/product-4.0.0-SNAPSHOT.zip

# Extracting the relative path to the distribution pack
downloadable_url=$(echo $disrtribution_url | sed -n 's:.*<relativePath>\(.*\)</relativePath>.*:\1:p')
echo $downloadable_url

echo "Downloadable URL : " $latest_successfull_build_url"org.wso2.sp\$product/artifact/"$downloadable_url
wget $latest_successfull_build_url"org.wso2.sp\$product/artifact/"$downloadable_url
