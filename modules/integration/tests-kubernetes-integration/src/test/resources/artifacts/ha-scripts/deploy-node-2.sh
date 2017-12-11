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

sp_port_two=32016
prgdir=$(dirname "$0")
script_path=$(cd "$prgdir"; cd ..; pwd)

# ----- K8s master url needs to be export from /k8s.properties

K8s_master=$(echo $(cat $script_path/../infrastructure-automation/k8s.properties))
export $K8s_master
echo "Kubernetes Master URL is Set to : "$K8s_master

echo "Creating the SP Instance Node 2!"
kubectl create -f $script_path/ha-scripts/sp-ha-node-1-service.yaml
kubectl create -f $script_path/ha-scripts/sp-ha-node-1-rc.yaml
sleep 5

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
host=$(getKubeNodeIP "${kube_nodes[2]}")
echo "Waiting for Pods to startup"
sleep 10

#----- ## To check the server start success

# ----- the loop is used as a global timer. Current loop timer is 3*100 Sec.
while true
do
echo $(date)" Checking for Node 2 status on http://${host}:${sp_port_two}"
  STATUS=$(curl -s -o /dev/null -w '%{http_code}' --fail --connect-timeout 5 --header 'Authorization: Basic YWRtaW46YWRtaW4=' http://${host}:${sp_port_two}/siddhi-apps)
  #curl --silent --get --fail --connect-timeout 5 --max-time 10 http://192.168.58.21:32013/siddhi-apps  ## to get response body
  if [ $STATUS -eq 200 ]; then
    echo "Node 2 successfully started."
    break
  else
    echo "Got $STATUS. Node 2 not started properly. "
  fi
  sleep 10
done

trap : 0

echo >&2 '
************
*** DONE ***
************
'