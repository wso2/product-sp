#!bin/sh

set -e

prgdir=$(dirname "$0")
script_path=$(cd "$prgdir"; pwd)

echo "Your working directory is : "$script_path

# --- to exit from script if an error occured
abort()
{
    echo >&2 '
***************
*** ABORTED ***
***************
'
    echo "An error occurred. Exiting..." >&2
    exit 1
}

trap 'abort' 0


#----- ## initiating the process

source $script_path/init.sh > $script_path/clus_create.log
sleep 60
echo "successfully deployed K8s cluster"

trap : 0

echo >&2 '
************
*** DONE ***
************
'

#sleep 10

#------ ## Waiting for Test suite executed and destroy the cluster

#echo "cluster will be destroying"
#source $script_path/cluster-destroy.sh > $script_path/clus_destroy.log
#sleep 120

#rm -rf terraform.tfstate
#rm -rf terraform.tfstate.backup
#sleep 10

#echo "successfully destroyed the cluster"

