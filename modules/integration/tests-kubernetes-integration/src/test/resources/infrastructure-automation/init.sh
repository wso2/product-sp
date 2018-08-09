#! /bin/bash
LOG_FILE_LOCATION="/home/$USER/terraform/sp/logs"

# Log Message should be parsed $1
log(){
 TIME=`date`
 #echo "$TIME : $1" >> "$LOG_FILE_LOCATION"
 echo "$TIME : $1"
 return
}

prgdir=$(dirname "$0")
script_path=$(cd "$prgdir"; pwd)

echo "You are initiated the process now... .."
echo "Your working directory is : "$script_path

#----- ## To pass the openstack authentication

#source $script_path/dev-openrc-2.sh
#source $script_path/qa-openrc-2.sh

log "===The Jenkins Main Script Logs===="
log "Checking the Environment variables;"
if [ -z $OS_TENANT_ID ]; then
 log "OS_TENANT_ID is not set as a environment variable"
 exit 1;
fi

if [ -z $OS_TENANT_NAME ]; then
 log "OS_TENANT_ID is not set as a environment variable"
 exit 1;
fi

if [ -z $OS_USERNAME ]; then
 log "OS_TENANT_ID is not set as a environment variable"
 exit 1;
fi

if [ -z $OS_PASSWORD ]; then
 log "OS_TENANT_ID is not set	 as a environment variable"
 exit 1;
fi

# If Jenkins is not picking the Path variables from the system, the workaround is to setting the path
#TERRA_HOME=/etc/terraform
#export PATH=$TERRA_HOME:$PATH


#----- ## To trigger the Ansible Scripts to do the kubernetes cluster

source $script_path/cluster-create.sh

# Check the cluster health
if [ -z $KUBERNETES_MASTER ]; then
 log "KUBERNETES_MASTER is not set as a environment variable"
 exit 1;
fi

#STRING_TO_TEST="k8s-master   Ready"
#OUTPUT=$(kubectl get nodes)
#if [[ $OUTPUT != *"${STRING_TO_TEST}"* ]]; then
#  log "Seems Kubenetes cluster is not setup properly, Hence Exiting"
#  exit 1;
#fi

log "This is awesome!! Infrastructure Execution is Success :) :)"
