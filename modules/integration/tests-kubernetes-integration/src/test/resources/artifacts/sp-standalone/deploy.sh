#! /bin/bash

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

sp_port=32016
docker_server="dockerhub.private.wso2.com"
docker_user="dasintegrationtest"
docker_pw="nzraxthlg5kdzmrXkwjuhia'6sziHw"

prgdir=$(dirname "$0")
script_path=$(cd "$prgdir"; cd ..; pwd)
echo "Current location : "$script_path


# ----- ##  This is to initiate infrastucture deployment

#/bin/bash $script_path/infrastructure-automation/init.sh
#/bin/bash $script_path/../infrastructure-automation/init.sh
#sleep 30


# ----- K8s master url needs to be export from /k8s.properties

K8s_master=$(echo $(cat $script_path/../infrastructure-automation/k8s.properties))
export $K8s_master
echo "Kubernetes Master URL is Set to : "$K8s_master


#------ ## This is to initiate for docker image creation and DAS configuration

#/bin/bash $script_path/docker-files/docker-create.sh
#sleep 2


echo "Creating the K8S Pods!!!!"


#----- ## This is to create K8s svc, rc, pods and containers

# -----# To create docker registry key
#This part should be remove from here and update as onetime task form somewhere: no need to run again and again
kubectl create secret docker-registry regsecretdas --docker-server=$docker_server --docker-username=$docker_user --docker-password=$docker_pw --docker-email=$docker_user@wso2.com
echo "registry key created"

kubectl create -f $script_path/sp-standalone/sp-test-service.yaml
kubectl create -f $script_path/sp-standalone/sp-test-rc.yaml
sleep 10


#----- ## To retrieve IP addresses of relevant nodes

function getKubeNodeIP() {
    IFS=$','
    node_ip=$(kubectl get node $1 -o template --template='{{range.status.addresses}}{{if eq .type "ExternalIP"}}{{.address}}{{end}}{{end}}')
    if [ -z $node_ip ]; then
      echo $(kubectl get node $1 -o template --template='{{range.status.addresses}}{{if eq .type "InternalIP"}}{{.address}}{{end}}{{end}}')
    else
      echo $node_ip
    fi
}

kube_nodes=($(kubectl get nodes | awk '{if (NR!=1) print $1}'))
host=$(getKubeNodeIP "${kube_nodes[0]}")
echo $host
echo "Waiting SP to launch on http://${host}:${sp_port}"
sleep 10

#----- ## To check the server start success

# ----- the loop is used as a global timer. Current loop timer is 3*100 Sec.
while true
do
echo $(date)" Waiting for server startup!"
  STATUS=$(curl -s -o /dev/null -w '%{http_code}' --fail --connect-timeout 5 --header 'Authorization: Basic YWRtaW46YWRtaW4=' http://${host}:${sp_port}/siddhi-apps)
  #curl --silent --get --fail --connect-timeout 5 --max-time 10 http://192.168.58.21:32013/siddhi-apps  ## to get response body
  if [ $STATUS -eq 200 ]; then
    echo "Awesome! You can access SP server now!"
    break
  else
    echo "Got $STATUS :( Not done yet..."
  fi
  sleep 10
done


trap : 0

echo >&2 '
************
*** DONE ***
************
'

echo 'Generating The test-deployment.json!'
pods=$(kubectl get pods --output=jsonpath={.items..metadata.name})
json='['
for pod in $pods; do
         hostip=$(kubectl get pods "$pod" --output=jsonpath={.status.hostIP})
         label=$(kubectl get pods "$pod" --output=jsonpath={.metadata.labels.name})
         servicedata=$(kubectl describe svc "$label")
         json+='{"hostIP" :"'$hostip'", "label" :"'$label'", "ports" :['
         declare -a dataarray=($servicedata)
         let count=0
         for data in ${dataarray[@]}  ; do
            if [ "$data" = "NodePort:" ]; then
            IFS='/' read -a myarray <<< "${dataarray[$count+2]}"
            json+='{'
            json+='"protocol" :"'${dataarray[$count+1]}'",  "port" :"'${myarray[0]}'"'
            json+="},"
            fi

         ((count+=1))
         done
         i=$((${#json}-1))
         lastChr=${json:$i:1}

         if [ "$lastChr" = "," ]; then
         json=${json:0:${#json}-1}
         fi

         json+="]},"
done

json=${json:0:${#json}-1}
json+="]"

echo $json;

cat > test-deployment.json << EOF1
$json
EOF1
