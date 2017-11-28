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

kube_pods=($(kubectl get po | awk '{print $1}'))
for pod in "${kube_pods[@]}"
do
   :
   if [[ $pod == *"sp-ha-node-1"* ]];
   then
   echo "----------------Printing Error Log Node 1-------------" >> error.log
        #Getting the process id of the Worker
        echo $(kubectl logs $pod) >> error.log
   fi
   if [[ $pod == *"sp-ha-node-2"* ]];
   then
      echo "----------------Printing Error Log Node 2-------------" >> error.log
        #Getting the process id of the Worker
        echo $(kubectl logs $pod) >> error.log
   fi
done